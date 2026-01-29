package tradeBook.pricing;

public class InvalidPriceException extends Exception {

    //Creates a new InvalidPriceException object which will just be the string message used
        //for that specific error
    public InvalidPriceException (String msg){
        super(msg);
    }
}
