package tradeBook;
/**
 Extends Exception, NOT Runtime Exception
 */
public class DataValidationException extends Exception {

    //Just throw the exception and attach the proper message to it
    public DataValidationException(String message) {
        super(message);
    }
}

