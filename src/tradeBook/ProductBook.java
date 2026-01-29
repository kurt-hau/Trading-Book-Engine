package tradeBook;

import tradeBook.pricing.InvalidPriceException;
import tradeBook.pricing.Price;

/**
 * ProductBook coordinates the BUY and SELL sides for a single product symbol.
 *  - Essentially a Facade?, uses only domain language for methods (add orders/quotes, cancel, remove quotes, top-of-book)
 *      - Not 100% sure of this but I think thats right
 *  - Delegates all more complex per-side mechanics to ProductBookSide instances.
 */
public class ProductBook {

    // ------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------
    /** The product symbol this book represents (e.g., "TGT"). */
    private final String product;

    /** The BUY side of the book (manages BUY price levels and Tradables). */
    private final ProductBookSide buySide;

    /** The SELL side of the book (manages SELL price levels and Tradables). */
    private final ProductBookSide sellSide;


    // ------------------------------------------------------------
    // CONSTRUCTOR
    // ------------------------------------------------------------
    /**
     * Constructs a ProductBook for the specified product symbol.
     * Validates input and initializes the BUY and SELL sides.
     *
     * @param product The product symbol (e.g., "TGT").
     * @throws IllegalArgumentException if product is null or blank.
     */
    public ProductBook(String product) throws IllegalArgumentException {
        this.product = setProduct(product);
        this.buySide  = new ProductBookSide(BookSide.BUY);
        this.sellSide = new ProductBookSide(BookSide.SELL);
    }


    // ------------------------------------------------------------
    // PRIVATE set() methods
    //  - Used for parameter validation and field setting
    // ------------------------------------------------------------
    /**
     * Validates the product symbol for this book.
     *  - Must mathc specified format, I think I can use regex() again like I used in the previous classes
     */
    private static String setProduct(String p) throws IllegalArgumentException {
        if (p == null || p.isBlank()) {
            throw new IllegalArgumentException("Product symbol cannot be null or blank.");
        }

        // Validate format: 1 to 5 alpha/numeric chars, optionally with one '.' somewhere inside
        if (!p.matches("^[A-Za-z0-9]{1,4}(\\.[A-Za-z0-9])?$") && !p.matches("^[A-Za-z0-9]{1,5}$")) {
            throw new IllegalArgumentException("Invalid product symbol: must be 1–5 letters/numbers, optionally with one '.' (e.g., AKO.A).");
        }
        return p;
    }


    // ------------------------------------------------------------
    // PUBLIC Functions for the client
    // ------------------------------------------------------------

    /**
     * Adds a Tradable (Order or QuoteSide) to the appropriate side of the book.
     * Routes by tradable side
     *  - Uses ProductBookSide's add() method to return a DTO snapshot of the added tradable.
     *
     * @param o The tradable to add.
     * @return TradableDTO representing the added tradable.
     * @throws IllegalArgumentException if o is null.
     */
    public TradableDTO add(Tradable o) throws IllegalArgumentException, DataValidationException, InvalidPriceException {
        //Check in the Tradable is null
        if (o == null) {
            throw new IllegalArgumentException("Tradable cannot be null.");
        }

        //Printed message that indicates you're adding a tradeable
//        System.out.println("**ADD: " + o);

        //Check if the Tradable's side is null
        BookSide s = o.getSide();
        if (s == null) {
            throw new IllegalArgumentException("Tradable side cannot be null.");
        }

        // Route by side to the appropriate ProductBookSide and hold onto the DTO snapshot
        TradableDTO dto = (s == BookSide.BUY) ? buySide.add(o) : sellSide.add(o);

        //Call tryTrade() to trigger matching right after you add new Tradable
        tryTrade();

        //Once trading has occurred, update the market accordingly
        updateMarket();

        //Return the DTO after trading is attempted
            //This DTO may be out of date after trading, but it will display the contents at insertion time correctly
        return dto;

        // Should never reach here with a valid enum; defensive fallback
        //throw new IllegalArgumentException("Unrecognized BookSide for tradable.");
    }

    /**
     * Adds a two-sided Quote into the book:
     *  - first removing any existing quotes from that user on both sides
     *  - Then adding each QuoteSide to its side.
     *
     * Returns both resulting DTOs in an array: index 0 = BUY, index 1 = SELL.
     *
     * @param qte The quote to add.
     * @return An array of TradableDTOs { buyDto, sellDto }.
     * @throws IllegalArgumentException if qte is null.
     */
    public TradableDTO[] add(Quote qte) throws IllegalArgumentException, DataValidationException, InvalidPriceException {
        if (qte == null) {
            throw new IllegalArgumentException("Quote cannot be null.");
        }

        // First, remove any existing quotes for this user on BOTH sides
            // using Quote's getuser() method
        removeQuotesForUser(qte.getUser());

        // Add BUY side of the quote and capture DTO
        TradableDTO buyDto = buySide.add(qte.getQuoteSide(BookSide.BUY));

        // Add SELL side of the quote and capture DTO
        TradableDTO sellDto = sellSide.add(qte.getQuoteSide(BookSide.SELL));

        // Attempt to trade after adding both sides
        tryTrade();

        // Return both DTOs as specified in an array (BUY at index 0, SELL at index 1)
            //These DTOs may be out of date after trading, but it will display the contents at insertion time correctly
        return new TradableDTO[] { buyDto, sellDto };

        //Do I need to return a modified DTO after tryTrade() is called?
    }

    /**
     * Cancels a tradable (Order) on the specified side by tradableID.
     *
     * @param side BUY or SELL.
     * @param orderId The tradable tradableID to cancel.
     * @return The resulting TradableDTO after cancellation.
     * @throws IllegalArgumentException if inputs are invalid.
     */
    public TradableDTO cancel(BookSide side, String orderId) throws IllegalArgumentException, DataValidationException, InvalidPriceException {
        if (side == null) {
            throw new IllegalArgumentException("BookSide cannot be null.");
        }
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be null or blank.");
        }

        // Route to the appropriate ProductBookSide
            //Use the ProductBookSide cancel() method
            //Return the resulting DTO after the cancel is made
        TradableDTO dto;
        if (side == BookSide.BUY) {
            dto = buySide.cancel(orderId);

        } else if (side == BookSide.SELL) {
            dto = sellSide.cancel(orderId);

        } else {
            // Defensive fallback (should not happen if I did my enum correctly)
            throw new IllegalArgumentException("Unrecognized BookSide for cancel operation.");
        }

        //Once order cancelling from the market has occurred, update the market accordingly
        updateMarket();

        return dto;
    }

    /**
     * Removes a user's quotes from both sides of the book.
     *  - Used above when a User adds a new quote to the book
     *  - Calls removeQuotesForUser() on each ProductBookSide and returns both resulting DTOs.
     *  - BUY side DTO at index 0, SELL side DTO at index 1.
     *
     * @param userName The 3-letter user code.
     * @return An array of TradableDTOs { buyDto, sellDto }.
     * @throws IllegalArgumentException if userName is null or blank.
     */
    public TradableDTO[] removeQuotesForUser(String userName) throws IllegalArgumentException, DataValidationException, InvalidPriceException {
        if (userName == null || userName.isBlank()) {
            throw new IllegalArgumentException("User name cannot be null or blank.");
        }

        // Call ProductBookSide's removeQuotesForUser() method on both sides of the book
        TradableDTO buyDto = buySide.removeQuotesForUser(userName);
        TradableDTO sellDto = sellSide.removeQuotesForUser(userName);

        //Once a User's quotes have been removed from the market, update the market accordingly
        updateMarket();

        // Return both DTOs in array form (BUY = index 0, SELL = index 1)
        return new TradableDTO[] { buyDto, sellDto };
    }


    /**
     * Returns a human-readable string for the top-of-book price for a given side
     *  -I gotta make sure it mathces PDF form exactly: "Price: $101.00 Volume: 250" or "N/A" if the side is empty.
     *
     * @param side BUY or SELL.
     * @return A string representing the top-of-book for the requested side.
     * @throws IllegalArgumentException if side is null.
     */
    public String getTopOfBookString(BookSide side) {//throws IllegalArgumentException{
//        // Validate side parameter
//        if (side == null) {
//            throw new IllegalArgumentException("BookSide cannot be null.");
//        }

        // Choose which ProductBookSide to inspect
        ProductBookSide bookSide = (side == BookSide.BUY) ? buySide : sellSide;

        // If the side is empty, return a formatted message indicating so
        if (bookSide.sideIsEmpty()) {
            return "Top of " + side + " book: $0.00 x 0";
        }

        // Get the top-of-book price and volume from the chosen side
        Price topPrice;
        topPrice = bookSide.topOfBookPrice();
        int topVol = bookSide.topOfBookVolume();

        // Handle null or zero conditions
        if (topPrice == null || topVol <= 0) {
            return "Top of " + side + " book: $0.00 x 0";
        }

        // Format and return the summary string
            // Example: "Top of BUY book: $122.50 x 75"
            // Java automatically calls topPrice.toString() when you concatenate --> Will have $XX.XX format
        return "Top of " + side + " book: " + topPrice + " x " + topVol;
    }


    /**
     * updateMarket()
     * Report the current market (top-of-book price & volume for the buy & sell sides).
     *  - Get top-of-book Price and volume for both the buy and sell sides.
     *  - Call the CurrentMarketTracker’s updateMarket method, passing in...
     *      the String stock symbol,
     *      the buy-side top-of-book Price
     *      the buy-side top-of-book volume
     *      the sell-side top-of-book Price
     *      the sell-side top-of-book volume.
     */
    private void updateMarket() throws InvalidPriceException, IllegalArgumentException {
        //Extract top price for buy side
        //Extract top price for sell side
            //Use ProductBookSide's topOfBookPrice() method for this
        Price topPriceBuy = this.buySide.topOfBookPrice();
        Price topPriceSell = this.sellSide.topOfBookPrice();


        //Extract top volume for buy side
        //Extract top volume for sell side
            //Use ProductBookSide's topOfBookVolume() method to do this
        int topVolBuy = this.buySide.topOfBookVolume();
        int topVolSell = this.sellSide.topOfBookVolume();

        //Update the Market for the Product
            // Need to make sure we grab the one singleton instance
            //"symbol" is called "product" here
        CurrentMarketTracker.getInstance().updateMarket(this.product, topPriceBuy,topVolBuy, topPriceSell, topVolSell);

    }


    /**
     *  tryTrade()
     * Attempts to match BUY and SELL sides at the top of book.
     * Follows the Appendix A flow:
     *  1) Read best BUY and SELL prices;
     *  2) If best SELL > best BUY -> Can't trade
     *  3) Compute totalToTrade as max(top BUY vol, top SELL vol).
     *  4) While there is still volume to fill from the request (totalToTrade > 0), repeatedly trade
     *       - Re-check the best prices; stop if either side becomes empty or the Sell price gets too high
     *       - Trade only he min of the two top volumes, up to totalToTrade = max(...)
     *              - Can only trade what you have in both
     *       - Delegates per-side fills to ProductBookSide.tradeOut(...)
     *              - If volume in either top is exhausted --> continue trading at any other prices that suffice
     */
    private void tryTrade() throws IllegalArgumentException, DataValidationException {

        // 1) Get the top BUY and SELL prices
        Price bestBuyPrice  = buySide.topOfBookPrice();
        Price bestSellPrice = sellSide.topOfBookPrice();

        // 2) If either side is empty, nothing to do
        if (bestBuyPrice == null || bestSellPrice == null) {
            return;
        }

        // 3) Can't trade if the top SELL price is greater than the top BUY price
        if (bestSellPrice.compareTo(bestBuyPrice) > 0) {
            return;
        }

        // 4) Compute totalToTrade as the max of the two top-of-book volumes
            // This sets an upper bound for how many units might be traded during this session.
            // The loop will continue until one side runs out of tradable volume or prices no longer suffuce
            //  - If one side exhausts the entire volume at a certain price:
            //      - The side that has some left over will continue trying to trade with the next best price until there aren't any suitable prices left

        int buyVolAtTop  = buySide.topOfBookVolume();
        int sellVolAtTop = sellSide.topOfBookVolume();
        int totalToTrade = Math.max(buyVolAtTop, sellVolAtTop);

        // 5) Loop while there is still volume to trade
        while (totalToTrade > 0) {
            // Re-check current best prices each iteration (book may have changed)
            bestBuyPrice  = buySide.topOfBookPrice();
            bestSellPrice = sellSide.topOfBookPrice();

            // Stop if either side of the book is now empty
            if (bestBuyPrice == null || bestSellPrice == null) {
                return;
            }

            // Stop if SELL Price became higher than BUY price
            if (bestSellPrice.compareTo(bestBuyPrice) > 0) {
                return;
            }

            // Determine how much to trade this round (min of the two tops)
                //Can only trade as much as both sides have
                //Ex: BUY has 20, SELL has 12 --> BUY will trade with the 12 SELL and have 8 left over for next iteration at next best SELL price
            buyVolAtTop  = buySide.topOfBookVolume();
            sellVolAtTop = sellSide.topOfBookVolume();
            int toTrade = Math.min(buyVolAtTop, sellVolAtTop);

            //Once the top price is exhausted, this round can end
            if (toTrade <= 0) {
                return; // defensive: no meaningful volume at tops
            }

            // Execute on both sides:

                // BUY side may trade out any of your tradables that are priced at or greater than this bestSellPrice
            buySide.tradeOut(bestSellPrice, toTrade);

                // SELL side may trade out any of its tradables that are priced at or less than this bestBuyPrice.
            sellSide.tradeOut(bestBuyPrice, toTrade);


            // Subtract the traded amount from the running total for that specific request
            totalToTrade -= toTrade;
        }
    }

    /**
     * Multi-line summary of this ProductBook, combining BUY and SELL sides
     *  - Allows ProductBookSide to use it's toString() within
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        //These make the output easy to compare to the expected output test doc side by side
        sb.append("--------------------------------------------\n");

        // Header with product symbol
        sb.append("Product Book: ").append(product).append("\n");

        // Append BUY side summary (ProductBookSide toString() will handle its own formatting)
        sb.append(buySide.toString()).append("\n");

        // Append SELL side summary also using the ProductBookSide toString() method
        sb.append(sellSide.toString());

        //These make the output easy to compare to the expected output test doc side by side
        sb.append("--------------------------------------------\n");

        // Trim any trailing whitespace/newlines
        return sb.toString().trim();
    }
}
