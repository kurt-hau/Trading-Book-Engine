package tradeBook;
/**
 Extends Exception, NOT Runtime Exception
 */
public class UserNotFoundException extends Exception {

    //Just throw the exception and attach the proper message to it
    public UserNotFoundException(String message) {

        //Have parent class (Exception) handle storing and managing the message for me since it already knows how
        super(message);
    }
}
