package tradeBook;

import tradeBook.pricing.Price;

/**
 * The Quote class represents a two-sided quote from a user.
 * It is composed of a BUY side and a SELL side, both of which are QuoteSide objects.
 * A Quote object itself is NOT Tradable — but its sides (QuoteSides) implement the Tradable interface so they can be added to the ProductBook.
 */
public class Quote {

    // ------------------------------------------------------------
    // FIELDS
    //  must be private and immutable so they can't be changed after construction
    // ------------------------------------------------------------
    /** The 3-letter user code associated with this quote. */
    private final String user;

    /** The product symbol this quote applies to. */
    private final String product;

    /** The BUY side of this quote. */
    private final QuoteSide buySide;

    /** The SELL side of this quote. */
    private final QuoteSide sellSide;


    // ------------------------------------------------------------
    // CONSTRUCTOR
    // ------------------------------------------------------------
    /**
     * Constructs a Quote consisting of a BUY and a SELL side.
     * Performs basic validation, then creates two QuoteSide objects.
     *
     * @param product The stock/product symbol.
     * @param buyPrice The Price object for the buy side.
     * @param buyVolume The volume for the buy side.
     * @param sellPrice The Price object for the sell side.
     * @param sellVolume The volume for the sell side.
     * @param user The 3-letter user code.
     */
    public Quote(String product,
                 Price buyPrice,
                 int buyVolume,
                 Price sellPrice,
                 int sellVolume,
                 String user) throws IllegalArgumentException {

        this.user = setUser(user);
        this.product = setProduct(product);

        this.buySide = setBuySide(user, product, buyPrice, buyVolume);
        this.sellSide = setSellSide(user, product, sellPrice, sellVolume);
    }


    // ------------------------------------------------------------
    // PRIVATE set() METHODS
    // Use these in the constructor to handle parameter and data field validation logic
    //  Could make static as they don't need an instance of the class to be called and belong to the class itself for object construction purposes
    // ------------------------------------------------------------

    /**
     * Validates and sets the user field.
     *  Must be in proper form and not null
     */
    private static String setUser(String u) throws IllegalArgumentException {
        if (u == null || !u.matches("[A-Za-z]{3}")) {
            throw new IllegalArgumentException("User code must be exactly 3 letters (A–Z).");
        }
        return u;
    }

    /**
     * Validates and sets the product field
     *  String passed in can't be null, and must mathc proper format
     *
     */
    private static String setProduct(String p) throws IllegalArgumentException {
                                            //First 5 are letters or numbers
                                            //OR
                                            //First 4 are letters/numbers --> a period (\\.) --> 1 letter/number
        if (p == null || !p.matches("[A-Za-z0-9]{1,5}|[A-Za-z0-9]{1,4}\\.[A-Za-z0-9]")) {
            throw new IllegalArgumentException("Product symbol must be 1–5 chars (letters/numbers, may include one '.').");
        }
        return p;
    }

    /**
     * Creates and validates the BUY side QuoteSide object.
     *  Price passed in can't be null
     *  Volume must be within specified range (0 < x < 10,000)
     */
    private static QuoteSide setBuySide(String user, String product, Price price, int volume) throws IllegalArgumentException {
        if (price == null) {
            throw new IllegalArgumentException("Buy price cannot be null.");
        }
        if (volume <= 0 || volume >= 10000) {
            throw new IllegalArgumentException("Buy volume must be > 0 and < 10,000.");
        }
        return new QuoteSide(user, product, price, volume, BookSide.BUY);
    }

    /**
     * Creates and validates the SELL side QuoteSide object.
     *  Price passed in can't be null
     *  Volume must be within specified range (0 < x < 10,000)
     */
    private static QuoteSide setSellSide(String user, String product, Price price, int volume) throws IllegalArgumentException {
        if (price == null) {
            throw new IllegalArgumentException("Sell price cannot be null.");
        }
        if (volume <= 0 || volume >= 10000) {
            throw new IllegalArgumentException("Sell volume must be > 0 and < 10,000.");
        }
        return new QuoteSide(user, product, price, volume, BookSide.SELL);
    }


    // ------------------------------------------------------------
    // Quote class METHODS
    //  These both return Strings, which are immutable, so they're safe to return directly
    // ------------------------------------------------------------

    /**
     * Returns the 3-letter user code for this quote.
     */
    public String getUser() {
        return this.user;
    }

    /**
     * Returns the product symbol for this quote.
     */
    public String getProduct() {
        return this.product;
    }

    /**
     * Returns either the BUY or SELL QuoteSide based on the specified BookSide.
     *
     * @param side The BookSide (BUY or SELL) to retrieve.
     * @return The corresponding QuoteSide object.
     * @throws IllegalArgumentException if the BookSide is null or invalid.
     */
    public QuoteSide getQuoteSide(BookSide side) throws IllegalArgumentException {
        if (side == null) {
            throw new IllegalArgumentException("BookSide cannot be null.");
        }
        if (side == BookSide.BUY) {
            return buySide;
        } else if (side == BookSide.SELL) {
            return sellSide;
        } else {
            throw new IllegalArgumentException("Invalid BookSide value.");
        }
    }


    // ------------------------------------------------------------
    // toString()
    // ------------------------------------------------------------
    /**
     * Returns a formatted string representation of this Quote,
     * including both BUY and SELL sides.
     */
    @Override
    public String toString() {
        return "Quote for " + product + " from " + user + ":\n" +
                "   BUY  --> " + buySide + "\n" +
                "   SELL --> " + sellSide;
    }
}