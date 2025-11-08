package Model;

import java.time.LocalDateTime;

public class TransactionReport {
    String transactionName;
    boolean isSuccessful;
    String stateText;
    Transaction transaction;
    LocalDateTime operationDateTime;

    public String getTransactionName() {
        return transactionName;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public String getStateText() {
        return stateText;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public LocalDateTime getOperationDateTime() {
        return operationDateTime;
    }

    public TransactionReport(String transactionName, Transaction transaction, boolean isSuccessful,
                             LocalDateTime operationDateTime, String stateText) {
        this.isSuccessful = isSuccessful;
        this.transaction = transaction;
        this.transactionName = transactionName;
        this.operationDateTime = operationDateTime;
        this.stateText = stateText;
    }

    @Override
    public String toString() {
        String accountNumberFrom = transaction.getAccountNumberFrom();
        String accountNumberOn = transaction.getAccountNumberOn();

        return operationDateTime.toString() + " | " + transactionName + " | перевод с "
                + (accountNumberFrom != null ? accountNumberFrom : "XXXXX") + " на "
                + (accountNumberOn != null ? accountNumberOn : "XXXXX") + " "
                + transaction.getTransferAmount() + " | "
                + (isSuccessful ? "успешно обработан" : stateText);
    }
}
