package tradeBook.pricing;

import java.util.Objects;

// ////////////////////////////////////
//CONSTRUCTOR:
// ////////////////////////////////////
    //Price class uses comparable
    //final -> prevents subclassing, which could interfere with caching
        //since subclassed Price objects might not be the same logical value
public final class Price implements Comparable<Price> {

    //cents that come from an int representation
    //FIELD: private so that it cannot be changed or accessed directly.
    private final int cents;

    //Negative, Positive, and Zero prices are legitimate
        // All Price creation must go through PriceFactory, not new Price(...) which is why we have...
    // OBJECT: package-private so that the constructor can only be called by classes in the same package (PriceFactory)

    Price (int cents){
        this.cents = cents;
    }



// ////////////////////////////////////
//METHODS:
    // Should always create and return new Price objects, never modifying the existing one
// ////////////////////////////////////
    //Doesn't need a parameter Price because it is acting on the known 'cents' field
        //Is not comparing with another object
    public boolean isNegative(){
        return this.cents < 0;
    }

    public Price add(Price p) throws InvalidPriceException {
        //Check if p is null, if so throw invalidPriceException
        if (p == null){
            throw new InvalidPriceException("InvalidPriceException: Cannot add null to a Price object, try again");
        }

        //Get the cents values of each objec to use for the addition
            //Can easily access an objects field with the syntax used below
        int value = this.cents + p.cents;

        // Create and return via PriceFactory to reuse cached instance
        return PriceFactory.makePrice(value);
    }

    public Price subtract(Price p) throws InvalidPriceException {
        //Check if p is null, if so throw invalidPriceException
        if (p == null){
            throw new InvalidPriceException("InvalidPriceException: Cannot subtract null from a Price object, try again");
        }

        //New Price object from the result of the subtraction
        int value = this.cents - p.cents;

        // // Create and return by routing through PriceFactory to reuse cached instance
        return PriceFactory.makePrice(value);
    }

    public Price multiply(int n){
        //Doesn't throw any exception, any integer passed in is valid
        int value = this.cents * n;

        // Create and return via PriceFactory to reuse cached instance
        return PriceFactory.makePrice(value);
    }

    public boolean greaterOrEqual(Price p) throws InvalidPriceException {
        if (p == null){
            throw new InvalidPriceException("InvalidPriceException: Cannot check greater than or equal with a null, try again");
        }
        //Can do comparison on their cent value because we are in the Price class, not the main
        return this.cents >= p.cents;
    }

    public boolean lessOrEqual(Price p ) throws InvalidPriceException {
        if (p == null){
            throw new InvalidPriceException("InvalidPriceException: TCannot check less than or equal with a null, try again");
        }
        return this.cents <= p.cents;
    }

    public boolean greaterThan(Price p) throws InvalidPriceException {
        if (p == null){
            throw new InvalidPriceException("InvalidPriceException: TCannot check greater than with a null, try again");
        }

        return this.cents > p.cents;
    }

    public boolean lessThan (Price p) throws InvalidPriceException {
        if (p == null){
            throw new InvalidPriceException("InvalidPriceException: Cannot check less than with a null, try again");
        }

        return this.cents < p.cents;
    }

    @Override
    public boolean equals(Object o) {
        //If o is null OR a different class type, the two objects can't be equal
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        //Cast o to be a price object now that you are sure its class matches
        Price price = (Price) o;
        return this.cents == price.cents;
    }

    @Override
    public int hashCode() {
        //return Integer.hashcode(this.cents) --> Use integer hashcode specific method?
        return Objects.hashCode(this.cents);
    }

    @Override
    /*
    - Return the difference in cents between the current price object and the price object passed in.
    - This method cannot propagate any exceptions.
    */
    public int compareTo(Price p) {
        // Check if Price p is null
            //If the other price object is null, then there can't be any difference between the objects
            //So you must return -1 I'm assuming? NOt sure because that would signify a difference?
        if (p == null){
            return -1;
        }
        return this.cents - p.cents;
    }

    /*
    Return a string containing the price value formatted as $d*.cc
     */
    @Override
    public String toString() {
        //Break the value down into its components
        int absCents = Math.abs(this.cents);
        int dollars = absCents / 100;

        //Cents will be the remainder when you divide by 100
        int centsPart = absCents % 100;

        // Add thousands separators
        String dollarsStr = String.format("%,d", dollars);

        // Dollar sign first, then minus if negative, then the cents portions
            //Will add the negative at the start if needed
        return "$" + (this.cents < 0 ? "-" : "") + dollarsStr + "." + String.format("%02d", centsPart);
    }


//    //Don't need to do it this complicated for toString(), just use built in methods from Math class to make this work
//    @Override
//    public String toString() {
//        int value;
//        String result;
//
//        //Negative Values
//        if (this.cents < 0) {
//            value = this.cents * -1;
//
//            if (value < 10) { // Single-digit cents like 0.05
//                result = "-$0.0" + value;
//            } else if (value < 100) { // Double-digit cents like 0.34
//                result = "-$0." + value;
//            } else { // 100 or more cents
//                int dollars = value / 100;
//                int centsPart = value % 100;    //Remainder is the cents portion
//
//                if (centsPart < 10) {   //If something like 234.05
//                    result = "-$" + dollars + ".0" + centsPart;
//                } else {                //If something like 567.32
//                    result = "-$" + dollars + "." + centsPart;
//                }
//            }
//
//        //Same workflow but just with positive values
//        } else {
//            value = this.cents;
//
//            if (value < 10) {
//                result = "$0.0" + value;
//            } else if (value < 100) {
//                result = "$0." + value;
//            } else {
//                int dollars = value / 100;
//                int centsPart = value % 100;
//
//                if (centsPart < 10) {
//                    result = "$" + dollars + ".0" + centsPart;
//                } else {
//                    result = "$" + dollars + "." + centsPart;
//                }
//            }
//        }
//
//        return result;
//    }
}
