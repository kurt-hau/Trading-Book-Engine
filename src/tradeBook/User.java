package tradeBook;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;


/**
 * “The User is like a personal portfolio or ledger that tracks every trade action that belongs to that specific trader"
 * - Models a specific trader (a User)
 * - Each trader has a bunch of different “Tradable” items that they own
 * - These are kept in a User specific hashmap where the Tradable ID maps to a DTO of that Tradable
 * - Tradable is an Order or QuoteSide for a specific product within a ProductBook
 */
public class User implements CurrentMarketObserver {

    // ---------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------

    // User Id: immutable 3-letter code (e.g., ANN, BOB)
    private final String userId;

    // HashMap of tradables this user has submitted and owns.
        //   Keys: tradable ids (Strings)
        //   Values: (TradableDTO) snapshot for that tradable tradableID
            //Calling new HashMap<> automatically initializes this and attaches a HashMap to the user
                //Is this good or should I wait until the constructor is called to initialize a HashMap?
    //private final HashMap<String, TradableDTO> tradables = new HashMap<>();

    // Using LinkedHashMap to preserve insertion order when printing tradables
    private final LinkedHashMap<String, TradableDTO> tradables = new LinkedHashMap<>();

    //HashMap to represent the <products, their current markets[BUY,SELL]>
    private HashMap <String, CurrentMarketSide[]> currentMarkets = new HashMap<>();

    // ---------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------
    /**
     * Public constructor accepts and sets the 3-character user tradableID.
     *  - Validates the tradableID with setUserId()
     *  - Need to be able to call this elsewhere to create a 'User'
     */
    public User(String userId) throws IllegalArgumentException {
        this.userId = setUserId(userId);
        //Initialize the HashMap for this User? --> Done above during declaration
    }

    // ---------------------------------------------------------------------
    // private helper set() methods called by constructor for field validation
    // ---------------------------------------------------------------------
    /**
     * Ensures user tradableID is exactly 3 alphabetic letters. Returns normalized tradableID.
     */
    private String setUserId(String in) throws IllegalArgumentException {
        if (in == null) {
            throw new IllegalArgumentException("User tradableID cannot be null");
        }

        //Get rid of any trailing spaces
        String s = in.trim();

        //Ensure length is 3 after trimming
        if (s.length() != 3) {
            throw new IllegalArgumentException("User tradableID must be exactly 3 letters: '" + in + "'");
        }

        // Only alphabetic letters, no digits/spaces/symbols
        if (!s.matches("[A-Za-z]{3}")) {
            throw new IllegalArgumentException("User tradableID must contain only letters A-Z: '" + in + "'");
        }

        // Do I need to normalize to uppercase to match examples (ANN, BOB, CAT)?
        return s.toUpperCase();
    }

    /**
     * Returns this User's 3-letter userId.
     *
     * @return The User's ID as a String.
     */
    public String getUserId() {
        return this.userId;
    }


    // ---------------------------------------------------------------------
    // Methods
    // ---------------------------------------------------------------------
    /**
     * Add/replace the incoming TradableDTO to the user's tradables HashMap.
     * If the TradableDTO is null, do nothing.
     */
    public void updateTradable(TradableDTO dto) {
        if (dto == null) {
            return; // no-op on null input per spec
        }

        // Ensure dto has an tradableID (the assignment implies it does)
            //Remember DTOs automatically have their own get() methods that are just the field name with ()
            //dto.tradableID() will return the DTO ID of that specific tradable

        String id = dto.tradableId();
        if (id == null) {
            return; // ignore any DTOs without an tradableID
        }

        //Place that tradable into the HashMap
        tradables.put(id, dto);
    }


    /**
     * Receives the BUY and SELL side CurrentMarketSide objects for the given stock symbol and stores them in this User's currentMarkets map.
     *
     * The method constructs a 2-element array as follows:
     *   index 0 → BUY side CurrentMarketSide
     *   index 1 → SELL side CurrentMarketSide
     *
     * The array is then placed into the currentMarkets HashMap using the stock symbol as the key.
     *  - Allows the User to track the most recent Current Market snapshot for each subscribed product.
     *
     * @param symbol   The stock symbol whose Current Market was updated.
     * @param buySide  The top-of-book BUY side CurrentMarketSide.
     * @param sellSide The top-of-book SELL side CurrentMarketSide.
     */
    public void updateCurrentMarket(String symbol, CurrentMarketSide buySide, CurrentMarketSide sellSide) {
        // Create a 2-element array of CMS objects to store the BUY and SELL sides

        CurrentMarketSide[] sides = new CurrentMarketSide[2];
        sides[0] = buySide;
        sides[1] = sellSide;

        // using the symbol passed in, store this array in the currentMarkets HashMap
        currentMarkets.put(symbol, sides);
    }

    /**
     * Generates and returns a formatted String containing this User's most recent Current Market snapshots for all subscribed products.
     *  - One line should be produced per stock symbol stored in the currentMarkets HashMap.
     *
     * @return A multi-line String summarizing the current market
     *         for each product tracked by this User.
     */
    public String getCurrentMarkets() {
        // If there are no current markets stored for this User, return an empty String
        if (currentMarkets.isEmpty()) {
            return "";
        }

        // Use a StringBuilder to accumulate one line per stock symbol
        StringBuilder sb = new StringBuilder();

        // Implementation will iterate over the currentMarkets map,
            //For each key/value "entry" : in the entire HashMap "entrySet"
        for (HashMap.Entry<String, CurrentMarketSide[]> entry : currentMarkets.entrySet()) {

            //Extract the key
            String symbol = entry.getKey();

            //Extract the value
            CurrentMarketSide[] sides = entry.getValue();

            // Ensure the array has at least two elements
            if (sides == null || sides.length < 2) {
                continue;
            }

            //Extract the buy and sell sides
            CurrentMarketSide buySide = sides[0];   // BUY side
            CurrentMarketSide sellSide = sides[1];  // SELL side

            // Append a line in the format:
                // Symbol TopBuyPrice x TopBuyVolume - TopSellPrice x TopSellVolume
                //give a new line after each product
            sb.append(symbol)
                    .append(" ")
                    .append(buySide.toString())
                    .append(" - ")
                    .append(sellSide.toString())
                    .append('\n');
        }

        // Return the accumulated String. Trailing newline is acceptable per spec.
        return sb.toString();
    }


    /**
     * I need to format this just like the PDF says to
     */
    @Override
    public String toString() {

        //Begin building a string to represent this user
            //List their ID at the top
        StringBuilder sb = new StringBuilder();

        // Add two leading spaces before the User Id label to match expected output formatting
        sb.append("  User Id: ").append(userId).append('\n');

        //Now loop through all of the tradables in that User's hashmap and print all of their DTOs in the required format
        for (TradableDTO dto : tradables.values()) {

            // Each DTO prints in the expected assignment format on a new line for each
                // One tab before each tradable line to match expected indentation
            sb.append('\t').append(dto.toString()).append('\n');
        }

        return sb.toString();
    }
}
