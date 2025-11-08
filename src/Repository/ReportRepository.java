package Repository;

import Model.TransactionReport;
import Model.Transaction;
import Exception.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ReportRepository {
    private final BlockingQueue<Connection> connectionPool;
    private final String url;
    private final String username;
    private final String password;
    private final int poolSize;

    public ReportRepository(String url, String username, String password) {
        this(url, username, password, 5);
    }

    public ReportRepository(String url, String username, String password, int poolSize) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.poolSize = poolSize;
        this.connectionPool = new ArrayBlockingQueue<>(poolSize);
        initializePool();
        createTableIfNotExists();
    }

    private void initializePool() {
        try {
            for (int i = 0; i < poolSize; i++) {
                Connection connection = DriverManager.getConnection(url, username, password);
                connectionPool.offer(connection);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка инициализации пула соединений", e);
        }
    }

    private Connection getConnection() throws DatabaseException {
        try {
            Connection connection = connectionPool.poll(5, TimeUnit.SECONDS);
            if (connection == null) {
                throw new DatabaseException("Таймаут получения соединения из пула");
            }
            return connection;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DatabaseException("Прервано ожидание соединения", e);
        }
    }

    private void returnConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connectionPool.offer(connection);
                } else {
                    Connection newConnection = DriverManager.getConnection(url, username, password);
                    connectionPool.offer(newConnection);
                }
            } catch (SQLException e) {
                System.err.println("Ошибка возврата соединения в пул: " + e.getMessage());
            }
        }
    }

    private void createTableIfNotExists() {
        String sql = """
        CREATE TABLE IF NOT EXISTS transaction_reports (
            id BIGSERIAL PRIMARY KEY,
            transaction_name VARCHAR(255) NOT NULL,
            is_successful BOOLEAN NOT NULL,
            state_text TEXT,
            operation_datetime TIMESTAMP NOT NULL,
            account_number_from VARCHAR(20),
            account_number_to VARCHAR(20),
            transfer_amount DECIMAL(15,2) NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
        """;

        Connection conn = null;
        try {
            conn = getConnection();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка создания таблицы", e);
        } finally {
            returnConnection(conn);
        }
    }

    public List<TransactionReport> findAll() {
        Connection conn = null;
        String sql = "SELECT * FROM transaction_reports ORDER BY operation_datetime DESC";
        List<TransactionReport> reports = new ArrayList<>();

        try {
            conn = getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    TransactionReport report = mapResultSetToReport(rs);
                    reports.add(report);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка получения всех отчетов", e);
        } finally {
            returnConnection(conn);
        }
        return reports;
    }

    public void saveAll(List<TransactionReport> reports) {
        Connection conn = null;
        String sql = """
            INSERT INTO transaction_reports 
            (transaction_name, is_successful, state_text, operation_datetime, 
             account_number_from, account_number_to, transfer_amount) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (TransactionReport report : reports) {
                    setReportParameters(pstmt, report);
                    pstmt.addBatch();
                }

                pstmt.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка пакетного сохранения отчетов", e);
        } finally {
            returnConnection(conn);
        }
    }

    private void setReportParameters(PreparedStatement pstmt, TransactionReport report) throws SQLException {
        pstmt.setString(1, report.getTransactionName());
        pstmt.setBoolean(2, report.isSuccessful());
        pstmt.setString(3, report.getStateText());
        pstmt.setTimestamp(4, Timestamp.valueOf(report.getOperationDateTime()));

        Transaction transaction = report.getTransaction();
        String accountFrom = transaction.getAccountNumberFrom();
        String accountTo = transaction.getAccountNumberOn();

        if (accountFrom != null && !accountFrom.isEmpty()) {
            pstmt.setString(5, accountFrom);
        } else {
            pstmt.setNull(5, Types.VARCHAR);
        }

        if (accountTo != null && !accountTo.isEmpty()) {
            pstmt.setString(6, accountTo);
        } else {
            pstmt.setNull(6, Types.VARCHAR);
        }

        pstmt.setDouble(7, transaction.getTransferAmount());
    }

    private TransactionReport mapResultSetToReport(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction(
                rs.getString("transaction_name"),
                rs.getString("account_number_from"),
                rs.getString("account_number_to"),
                rs.getDouble("transfer_amount")
        );

        return new TransactionReport(
                rs.getString("transaction_name"),
                transaction,
                rs.getBoolean("is_successful"),
                rs.getTimestamp("operation_datetime").toLocalDateTime(),
                rs.getString("state_text")
        );
    }
}
