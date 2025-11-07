package app.exception;

public class ConversionWarningException extends GenericException {
    /**
     * Constructs a new ConversionErrorException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public ConversionWarningException(final String message) {
        super(message);
    }
}
