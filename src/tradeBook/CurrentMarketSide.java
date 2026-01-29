package tradeBook;

import tradeBook.pricing.InvalidPriceException;
import tradeBook.pricing.Price;

/**
 * This class holds the top price and top volume (at that price) for one market side, It is used with current market
 * publications. The CurrentMarketSide will need the following data elements to properly represent the price
 * and volume (private):
 *
 *      - Price price ➔ The top-of-book price for one market side (set on construction, not changeable)
 *      - Int volume ➔ The top-of-book volume for one market side (set on construction, not changeable)
 *
 * The CurrentMarketSide will need a constructor that accepts and sets the price and volume values.
 * The CurrentMarketSide needs a toString that generates and returns a String as follows:
 *      public String toString() ➔ returns a String with the price and volume, as: $98.10x105
 */
public class CurrentMarketSide {

    // ------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------

    //The top-of-book price for one market side (set on construction, not changeable)
    private Price price;

    //The top-of-book volume for one market side (set on construction, not changeable)
    private int volume;

    // ------------------------------------------------------------
    // CONSTRUCTOR
    // ------------------------------------------------------------
    /**
     * Constructor that accepts and sets the price and volume values.
     *
     * @param price  The top-of-book price for this market side.
     * @param volume The top-of-book volume at the top price.
     */
    public CurrentMarketSide(Price price, int volume) throws InvalidPriceException, IllegalArgumentException {
        //Calls set() methods to validate price and volume fields
        setPrice(price);
        setVolume(volume);

    }

    // ------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------

    /**
     * Sets the price for this market side.
     *
     * @param price The top-of-book price to set.
     */
    private void setPrice(Price price) throws InvalidPriceException {
        //By the time a Price object has reached here, it has already been validated as a numeric
            //Null Check
        if (price == null){
            throw new InvalidPriceException("Price cannot be null");
        }
        this.price = price;
    }


    /**
     * Sets the volume for this market side.
     *
     * @param volume The top-of-book volume to set.
     */
    private void setVolume(int volume) throws IllegalArgumentException{
        //Primitive type so not NULL check is needed
        //Checks that volume is not negative
        if (volume < 0) {
            throw new IllegalArgumentException("Volume cannot be negative");
        }

        //If volume is 0, create the CMS null object
        if (volume == 0) {
            // CMS-level Null Object handling occurs outside this setter
            this.volume = 0;
        } else {
            this.volume = volume;
        }
    }

    /**
     * Returns a String with the price and volume formatted as $pricexvolume.
     *  $98.10x105
     * @return A formatted String representation of this CurrentMarketSide.
     */
    @Override
    public String toString() {
        //Formats as $ price x volume
            //Relies on Price.toString() already producing the dollar $##.## format
        return price.toString() + "x" + volume;
    }



}
