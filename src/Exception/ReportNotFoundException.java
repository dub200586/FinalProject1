package Exception;

public class ReportNotFoundException extends RuntimeException {
    public ReportNotFoundException(String message) {
        super(message);
    }
}
