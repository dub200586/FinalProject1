package Model;

public class Transaction {
    private final String name;
    private final String  accountNumberFrom;
    private final String  accountNumberTo;
    private final double transferAmount;

    public Transaction(String name, String accountNumberFrom, String accountNumberOn, double transferAmount) {
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

    public double getTransferAmount() {
        return transferAmount;
    }
}
