package Service;

import Model.Transaction;
import Model.TransactionReport;
import Exception.FileProcessingException;
import Exception.ReportNotFoundException;
import Repository.ReportRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ReportService {
    private final String fileExtension;
    private final String pathFilePath;
    private final FileService fileService;
    private final ReportRepository reportRepository;

    public ReportService(String fileExtension, String pathFilePath,
                         FileService fileService, ReportRepository reportRepository) {
        this.fileExtension = fileExtension;
        this.pathFilePath = pathFilePath;
        this.fileService = fileService;
        this.reportRepository = reportRepository;
    }

    public List<TransactionReport> convertToTransactionReports(List<Transaction> transactions) {
        return transactions.stream().map(t -> {
            try {
                String stateText = ValidationService.validateTransaction(t);
                return new TransactionReport(t.getName(), t, stateText.isEmpty(),
                        LocalDateTime.now(), stateText);
            } catch (Exception _) {}
            return null;
        }).collect(Collectors.toList());
    }

    public void saveReport(List<TransactionReport> reports, String saveDirectory) throws FileProcessingException {
        if (reports == null || reports.isEmpty()) {
            throw new FileProcessingException("Нет данных для сохранения отчета");
        }

        reportRepository.saveAll(reports);

        String reportContent = reports.stream()
                .map(TransactionReport::toString)
                .collect(Collectors.joining("\n"));

        String fileName = "\\TransactionReport" + fileExtension;
        String fullPath = saveDirectory + fileName;

        fileService.appendToFile(reportContent, fullPath);
        saveReportPath(fullPath);

        System.out.println("Отчет успешно создан: " + fullPath);
        System.out.println("Отчет сохранен в БД");
    }

    public String getReportFromDB() throws ReportNotFoundException, FileProcessingException {
        List<TransactionReport> reports = reportRepository.findAll();

        if (reports.isEmpty()) {
            throw new ReportNotFoundException("Отчеты не найдены в репозитории");
        }

        return reports.stream()
                .map(TransactionReport::toString)
                .collect(Collectors.joining("\n"));
    }

    public String getReport() throws ReportNotFoundException, FileProcessingException {
        if (!fileService.isFileValid(new java.io.File(pathFilePath))) {
            throw new ReportNotFoundException("Файл отчета не найден");
        }

        String reportPath = fileService.readFileContent(new java.io.File(pathFilePath));
        java.io.File reportFile = new java.io.File(reportPath);

        if (!fileService.isFileValid(reportFile)) {
            throw new ReportNotFoundException("Файл отчета не найден");
        }

        return fileService.readFileContent(reportFile);
    }

    public String filterReportByInterval(String fullReport, LocalDateTime startDate, LocalDateTime endDate) {
        if (fullReport == null || fullReport.trim().isEmpty()) {
            return "";
        }

        String[] lines = fullReport.split("\n");
        List<String> filteredLines = new java.util.ArrayList<>();

        for (String line : lines) {
            LocalDateTime lineDate = LocalDateTime.parse(line.substring(0, line.indexOf("|")).trim());
            if (!lineDate.isBefore(startDate) && !lineDate.isAfter(endDate)) {
                filteredLines.add(line);
            }
        }

        return String.join("\n", filteredLines);
    }

    private void saveReportPath(String path) throws FileProcessingException {
        fileService.saveToFile(path, pathFilePath);
    }
}
