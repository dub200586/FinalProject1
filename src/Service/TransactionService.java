package Service;

import Model.Transaction;
import Exception.FileProcessingException;
import Exception.TransactionValidationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransactionService {
    private final Pattern accountPattern = Pattern.compile("\\d{5}-\\d{5}");
    private final Pattern amountPattern = Pattern.compile("-?\\d+");

    public Transaction parseTransaction(File textFile) throws IOException, FileProcessingException, TransactionValidationException {
        try (BufferedReader reader = new BufferedReader(new FileReader(textFile))) {
            String line;
            String accountNumberFrom = null;
            String accountNumberTo = null;
            int transferAmount = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                try {
                    if (line.contains("номер счета с") && accountNumberFrom == null) {
                        accountNumberFrom = extractPattern(accountPattern, line, "номер счета отправителя");
                    } else if (line.contains("номер счета на") && accountNumberTo == null) {
                        accountNumberTo = extractPattern(accountPattern, line, "номер счета получателя");
                    } else if (line.contains("сумма для перевода")) {
                        String amountStr = extractPattern(amountPattern, line, "сумма перевода");
                        transferAmount = Integer.parseInt(amountStr);
                    }
                } catch (Exception e) {
                    throw new FileProcessingException("Ошибка обработки строки в файле " + textFile.getName() + ": " + line, e);
                }
            }

            return new Transaction(textFile.getName(), accountNumberFrom, accountNumberTo, transferAmount);
        }
    }

    private String extractPattern(Pattern pattern, String line, String fieldName) throws FileProcessingException {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group();
        }
        throw new FileProcessingException("Не удалось извлечь " + fieldName + " из строки: " + line);
    }
}
