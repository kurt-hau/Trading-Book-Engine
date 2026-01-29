package tradeBook;
/**
    Extends Exception, NOT Runtime Exception
 */
public class IllegalArgumentException extends Exception {
    public IllegalArgumentException(String message) {
        super(message);
    }
}
