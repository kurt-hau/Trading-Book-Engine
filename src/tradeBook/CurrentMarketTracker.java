package tradeBook;

import tradeBook.pricing.InvalidPriceException;

//Needed for the Price object construction below
import tradeBook.pricing.Price;
import tradeBook.pricing.PriceFactory;

/**
 * Singleton that receives Current Market updates from the ProductBooks, and sends the information on to the CurrentMarketPublisher.
 *  - One method:
 *      updateMarket()
 */

public class CurrentMarketTracker {

    // ------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------

    //Will need the PB to extract the PBsides from

    //Will need the private static instance variable
    private static CurrentMarketTracker instance;


    // ------------------------------------------------------------
    // CONSTRUCTOR
    // ------------------------------------------------------------
    /**
     * Should be empty because the Singlton construction will come from the getInstance() method
     *  - private so only getInstance() methods can be called
     */
    private CurrentMarketTracker() {
        //Will be called by getInstance() to ensure we are only grabbing the single instance of this variable
        //The actual Singleton enforcement happens in getInstance(), not here.
    }

    // ------------------------------------------------------------
    // SINGLETON METHOD
    // ------------------------------------------------------------
    /**
     *  Returns the single CurrentMarketTracker instance.
     *  Singleton pattern, creating the instance on the first call and returning the same instance anytime after
     */
    public static CurrentMarketTracker getInstance(){
        if (instance == null){
            instance = new CurrentMarketTracker();
        }

        return instance;
    }

    // ------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------


    /**
     * Receives the top-of-book buy and sell side information for a given stock symbol and prepares the Current Market snapshot.
     *
     *  - Calculate the market width as the difference in price between the sellPrice and the buyPrice.
     *    - Note, if either the sellPrice or the buyPrice are null, the width should be considered 0.
     *  - Create a CurrentMarketSide object for the buy side using the buyPrice and buyVolume.
     *  - Create a CurrentMarketSide object for the sell side using the sellPrice and sellVolume.
     *  - Printing the formatted Current Market display
     *  - Forward the snapshot to the CurrentMarketPublisher
     *
     * @param symbol    The stock symbol whose Current Market is being updated.
     * @param buyPrice  The top-of-book buy side Price (may be null if no buy orders).
     * @param buyVolume The total volume available at the top-of-book buy price.
     * @param sellPrice The top-of-book sell side Price (may be null if no sell orders).
     * @param sellVolume The total volume available at the top-of-book sell price.
     */
    public void updateMarket(String symbol, Price buyPrice, int buyVolume, Price sellPrice, int sellVolume) throws InvalidPriceException, IllegalArgumentException {

        /**
         * STEP 1: Determine the market width
         */
        //Declare Price object variable for the market width
        Price width;

        // If either buyPrice or sellPrice is null, the width should be treated as $0.00.
            //Can use the string constructor for this
        if (buyPrice == null || sellPrice == null) {
            width = PriceFactory.makePrice("0.00");
        } else {
            // Otherwise, compute the width as sellPrice - buyPrice using the Price subtract() method
            width = sellPrice.subtract(buyPrice);
        }

        /**
         * STEP 2: Create CurrentMarketSide objects for BUY and SELL
         */
        // Use the provided buyPrice and buyVolume to construct the buy-side CurrentMarketSide.
        CurrentMarketSide buySide;

        //If there is no valid buy price OR the buy volume is 0, create the null-object representation for the BUY side
        if (buyPrice == null || buyVolume == 0) {
            //CMS Null Object Representation
            buySide = new CurrentMarketSide(PriceFactory.makePrice("0.00"), 0);
        } else {
            //Otherwise, construct a normal CurrentMarketSide using the top-of-book buy data
            buySide = new CurrentMarketSide(buyPrice, buyVolume);
        }

        // Use the provided sellPrice and sellVolume to construct the sell-side CurrentMarketSide.
        CurrentMarketSide sellSide;

        //If there is no valid sell price OR the sell volume is 0, create the null-object representation for the SELL side
        if (sellPrice == null || sellVolume == 0) {
            //CMS Null Object Representation
            sellSide = new CurrentMarketSide(PriceFactory.makePrice("0.00"), 0);
        } else {
            //Otherwise, construct a normal CurrentMarketSide using the top-of-book sell data
            sellSide = new CurrentMarketSide(sellPrice, sellVolume);
        }

        /**
         * STEP 3: Print the Current Market banner like the PDF says
         */
        //Print the formatted Current Market display
            //Use the toString() methods of CurrentMarketSide for buySide and sellSide
        System.out.println("*********** Current Market ***********");
        System.out.println("* " + symbol + " " + buySide.toString() + " - " + sellSide.toString() + " [" + width.toString() + "]");
        System.out.println("**************************************");



        /**
         * STEP 4: Publish the Current Market snapshot
         */
        // Call CurrentMarketPublisher.getInstance().acceptCurrentMarket(...)
            // passing the symbol, and the buy-side and sell-side CurrentMarketSide objects.
        CurrentMarketPublisher.getInstance().acceptCurrentMarket(symbol, buySide, sellSide);
    }

}
