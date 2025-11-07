package app.exception;

public class ConversionErrorException extends GenericException {
    /**
     * Constructs a new ConversionErrorException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public ConversionErrorException(final String message) {
        super(message);
    }
}
