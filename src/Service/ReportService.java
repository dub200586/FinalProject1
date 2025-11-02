package Service;

import Model.Transaction;
import Model.TransactionReport;
import Exception.TransactionValidationException;
import Exception.FileProcessingException;
import Exception.ReportNotFoundException;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class ReportService {
    private final String fileExtension;
    private final String pathFilePath;
    private final FileService fileService;

    public ReportService(String fileExtension, String pathFilePath, FileService fileService) {
        this.fileExtension = fileExtension;
        this.pathFilePath = pathFilePath;
        this.fileService = fileService;
    }

    public List<TransactionReport> convertToTransactionReports(List<Transaction> transactions) {
        return transactions.stream().map(t -> {
            try {
                String stateText = ValidationService.validateTransaction(t);
                return new TransactionReport(t.getName(), t, stateText.isEmpty(),
                        LocalDateTime.now(), stateText);
            } catch (TransactionValidationException e) {
                return new TransactionReport(t.getName(), t, false,
                        LocalDateTime.now(), e.getMessage());
            }
        }).collect(Collectors.toList());
    }

    public void saveReport(List<TransactionReport> reports, String saveDirectory) throws FileProcessingException {
        if (reports == null || reports.isEmpty()) {
            throw new FileProcessingException("Нет данных для сохранения отчета");
        }

        String reportContent = reports.stream()
                .map(TransactionReport::toString)
                .collect(Collectors.joining("\n"));

        String fileName = generateFileName();
        String fullPath = saveDirectory + fileName;

        fileService.saveToFile(reportContent, fullPath);
        saveReportPath(fullPath);

        System.out.println("Отчет успешно создан: " + fullPath);
    }

    public String getReport() throws ReportNotFoundException, FileProcessingException {
        if (!fileService.isFileValid(new File(pathFilePath))) {
            throw new ReportNotFoundException("Файл с путем отчета не найден: " + pathFilePath);
        }

        String reportPath = fileService.readFileContent(new File(pathFilePath));
        File reportFile = new File(reportPath);

        if (!fileService.isFileValid(reportFile)) {
            throw new ReportNotFoundException("Файл отчета не найден по пути: " + reportPath);
        }

        return fileService.readFileContent(reportFile);
    }

    private String generateFileName() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return "\\Report" + LocalDateTime.now().format(formatter) + fileExtension;
    }

    private void saveReportPath(String path) throws FileProcessingException {
        fileService.saveToFile(path, pathFilePath);
    }
}
