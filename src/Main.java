import Model.Transaction;
import Model.TransactionReport;
import Service.FileService;
import Service.ReportService;
import Service.TransactionService;
import Exception.ReportNotFoundException;
import Exception.InvalidInputException;
import Exception.TransactionValidationException;
import Exception.FileProcessingException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private final static String FILE_PATH = "./input/";
    private final static String FILE_EXTENSION = ".txt";
    private final static String FILE_TEXT_PATH = "./path" + FILE_EXTENSION;

    private final FileService fileService;
    private final TransactionService transactionService;
    private final ReportService reportService;

    public Main() {
        this.fileService = new FileService(FILE_EXTENSION);
        this.transactionService = new TransactionService();
        this.reportService = new ReportService(FILE_EXTENSION, FILE_TEXT_PATH, fileService);
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.run();
    }

    public void run() {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("Введите номер команды:");

            try {
                if (sc.hasNextInt()) {
                    int number = sc.nextInt();
                    if (number == 1) {
                        sc.nextLine();
                        saveAllTransfersList();
                    } else if (number == 2) {
                        sc.nextLine();
                        printAllTransfersList();
                        break;
                    } else {
                        throw new InvalidInputException("Неверный номер команды: " + number);
                    }
                } else {
                    throw new InvalidInputException("Вы ввели не номер: " + sc.next());
                }
            } catch (InvalidInputException e) {
                System.out.println("Ошибка ввода: " + e.getMessage());
                sc.nextLine();
            } catch (Exception e) {
                System.out.println("Неожиданная ошибка: " + e.getMessage());
            }
        }

        sc.close();
    }

    private void saveAllTransfersList() throws TransactionValidationException {
        List<File> textFiles = fileService.findTextFiles(FILE_PATH);
        List<Transaction> transactions = new ArrayList<>();

        for (File textFile : textFiles) {
            try {
                if (fileService.isFileValid(textFile)) {
                    transactions.add(transactionService.parseTransaction(textFile));
                }
            } catch (TransactionValidationException e) {
                Transaction invalidTransaction = createInvalidTransaction(textFile.getName());
                transactions.add(invalidTransaction);
                System.err.println("Предупреждение валидации: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Ошибка обработки файла " + textFile.getName() + ": " + e.getMessage());
            }
        }

        if (transactions.isEmpty()) {
            throw new FileProcessingException("Не удалось обработать ни одной транзакции");
        }

        List<TransactionReport> transactionReports = reportService.convertToTransactionReports(transactions);

        Scanner sc = new Scanner(System.in);
        System.out.println("Введите абсолютный путь сохранения отчета:");
        String savedPath = sc.nextLine();

        reportService.saveReport(transactionReports, savedPath);
    }

    private Transaction createInvalidTransaction(String fileName) {
        return new Transaction(fileName, null, null, 0);
    }

    private void printAllTransfersList() {
        try {
            String reportText = reportService.getReport();
            System.out.println(reportText);
        } catch (ReportNotFoundException e) {
            System.out.println("Отчет не найден: " + e.getMessage());
        }
    }
}
