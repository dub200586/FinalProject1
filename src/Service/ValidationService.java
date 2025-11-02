package Service;

import Model.Transaction;

public class ValidationService {

    static String validateTransaction(Transaction transaction) {
        String stateText = "";

        if (transaction.getAccountNumberFrom() == null) {
            stateText = stateText + "ошибка во время обработки, неверный счет отправителя или его отсутствие";
        }

        if (transaction.getAccountNumberOn() == null) {
            if (stateText.isEmpty()) {
                stateText = stateText + "ошибка во время обработки";
            }

            stateText = stateText + ", неверный счет получателя или его отсутствие";
        }

        if (transaction.getTransferAmount() <= 0) {
            if (stateText.isEmpty()) {
                stateText = stateText + "ошибка во время обработки";
            }

            stateText = stateText + ", неверная сумма перевода";
        }

        return stateText;
    }
}
