package Service;

import Model.Transaction;

public class ValidationService {

    public static String validateTransaction(Transaction transaction) {
        StringBuilder stateText = new StringBuilder();

        if (transaction.getAccountNumberFrom() == null) {
            stateText.append("ошибка во время обработки, неверный счет отправителя или его отсутствие");
        }

        if (transaction.getAccountNumberOn() == null) {
            if (stateText.isEmpty()) {
                stateText.append("ошибка во время обработки");
            }
            stateText.append(", неверный счет получателя или его отсутствие");
        }

        if (transaction.getTransferAmount() <= 0) {
            if (stateText.isEmpty()) {
                stateText.append("ошибка во время обработки");
            }
            stateText.append(", неверная сумма перевода");
        }

        return stateText.toString();
    }
}
