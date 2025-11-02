package Model;

public class Transaction {
    private final String name;
    private final String  accountNumberFrom;
    private final String  accountNumberTo;
    private final int transferAmount;

    public Transaction(String name, String accountNumberFrom, String accountNumberOn, int transferAmount) {
        this.name = name;
        this.accountNumberFrom = accountNumberFrom;
        this.accountNumberTo = accountNumberOn;
        this.transferAmount = transferAmount;
    }

    public String getName() {
        return name;
    }

    public String getAccountNumberFrom() {
        return accountNumberFrom;
    }

    public String getAccountNumberOn() {
        return accountNumberTo;
    }

    public int getTransferAmount() {
        return transferAmount;
    }
}
