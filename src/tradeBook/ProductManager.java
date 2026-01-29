package tradeBook;

//Why not use TreeMap for a collection ordered alphabetically by IDs?
    //TreeMap does have worse lookup time
import tradeBook.pricing.InvalidPriceException;

import java.util.HashMap;

/**
 * ProductManager class serves as a Singleton + Facade access point for every ProductBook in the system.
 * Responsibilities:
 *  - Own and manage a ProductBook for each product symbol
 *  - Provide a single access point (Singleton) for adding/canceling tradables and quotes
 *      - Through its methods --> delegates product-specific operations to the appropriate ProductBook methods
 *
 * - ProductBook objects are mutable, so as you update them the
 */
public class ProductManager {

    // ---------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------

    // HashMap of product symbol -> ProductBook (created/owned by this manager)
        // Key: A String representing the 3 letter Product ID
        // Value: A ProductBook object corresponding to that Product ID
            //private so that it can not be accessed outside of this facade

    private HashMap<String, ProductBook> productBooks;

    // Singleton instance of ProductManager
        // Static means there's exactly one shared variable for the class --> Even through there will only be one instance using it

    private static ProductManager INSTANCE;

    // ---------------------------------------------------------------------
    // Constructor (Singleton pattern)
    // ---------------------------------------------------------------------
    /**
     * Private constructor to prevent external instantiation.
     *  - Initialize the HashMap --> start empty
     *  - When the ProductManager is created, make a brand-new, empty HashMap to hold all of the ProductBook objects that this manager will later control.”
     */
    private ProductManager() {
        //Creates a new HashMap object called productBooks that is owned by the single class instance of ProductManager
        productBooks = new HashMap<>();
    }

    // ---------------------------------------------------------------------
    // Single
    // ---------------------------------------------------------------------
    /**
     * Returns the single ProductManager instance
     *  - If we have never created one before --> Call constructor to create the first/only one and save it to the INSTANCE variable
     */
    public static ProductManager getInstance() {
        // Check if we have already created an INSTANCE of this class (HashMap)
        //Both the INSTANCE field and getInstance() need to be static so that they can deal with each other and can be called without an instance of the class already existing
            //If not, route a call to the constructor to make the first/only one
        if (INSTANCE == null){
            INSTANCE = new ProductManager();
        }
        //Don't do this.INSTANCE because that implies that each object has their own instance, which is wrong
        return INSTANCE;
    }

    // ---------------------------------------------------------------------
    // API Methods
    // ---------------------------------------------------------------------
    /**
     * Adds a new product symbol and creates its ProductBook.
     * - This method should create a new ProductBook object for the stock symbol passed in
     * - add the key/value pair to the HashMap of all ProductBook objects.
     * - If the symbol is null or it does not match symbol requirements (back in Part 2), throw a DataValidationException.
     */
    public void addProduct(String symbol) throws DataValidationException, IllegalArgumentException {

        // If symbol is null throw a DataValidationException
        if (symbol == null) {
            throw new DataValidationException("Product symbol cannot be null");
        }

        // Save the symbol to a local variable
        String sym = symbol.trim().toUpperCase();

        // Validate format: 1 to 5 alpha/numeric chars, optionally with one '.' somewhere inside
        if (!sym.matches("^[A-Za-z0-9]{1,4}(\\.[A-Za-z0-9])?$") && !sym.matches("^[A-Za-z0-9]{1,5}$")) {
            throw new DataValidationException("Invalid product symbol: '" + symbol + "' (must be 1 to 5 alpha/numeric chars, optionally with one '.' somewhere within)");
        }

        // Construct the ProductBook for this symbol; constructor may also validate internally
            // Save the result in a local variable prodBook
            // Will throw the Illegal Argument exception if it doesn't pass the logic
        ProductBook prodBook = new ProductBook(sym);

        //Insert a new entry with <symbol, prodBook>
            // If a ProductBook already exists for this symbol, we simply overwrite it with the new one
        productBooks.put(sym, prodBook);
    }

    /**
     * ProductBook getProductBook(String symbol):
     * - Return the ProductBook using the String symbol passed in.
     * - If the product does not exist, throw a DataValidationException.
     */
    public ProductBook getProductBook(String symbol) throws DataValidationException {
        // Same symbol validation as above

        // If symbol is null or empty throw a DataValidationException
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new DataValidationException("Product symbol cannot be null");
        }

        // Normalize format (trim + uppercase) -> Save in a local variable
        String sym = symbol.trim().toUpperCase();

        // Validate format: 1 to 5 alpha/numeric chars, optionally with one '.' somewhere inside
        if (!sym.matches("^[A-Za-z0-9]{1,4}(\\.[A-Za-z0-9])?$") && !sym.matches("^[A-Za-z0-9]{1,5}$")) {
            throw new DataValidationException("Invalid product symbol: '" + symbol + "' (must be 1 to 5 alpha/numeric chars, optionally with one '.' somewhere within)");
        }

        // Look up the ProductBook
        ProductBook book = productBooks.get(sym);

        // If it doesn't exist, throw an exception
        if (book == null) {
            throw new DataValidationException("No ProductBook exists for symbol: " + sym);
        }

        //Simply return a direct reference to the ProductBook associated with this symbol
            // ProductBook objects are mutable as the trades occur, but we are allowing to break info hiding for now
        return book;

    }

    /**
     * String getRandomProduct():
     *  - Return a randomly selected product symbol (String) from the collection of all ProductBook objects
     *  - If no products exist yet (empty HashMap), throw a DataValidationException.
     */
    public String getRandomProduct() throws DataValidationException {
        // If no products exist, throw a DataValidationException
        if (productBooks.isEmpty()) {
            throw new DataValidationException("No products exist to select from.");
        }

        // Place the keys (symbols) into an array for random indexing
        Object[] symbols = productBooks.keySet().toArray();

        // Generate a random index (int) between 0 and HashMap.length - 1
        int randIndex = (int) (Math.random() * symbols.length);

        // Return the randomly chosen symbol as a String
            // (can cast the int to String)
        return (String) symbols[randIndex];
    }

    /**
     * TradableDTO addTradable(Tradable o):
     * Adds a Tradable (order or quote side) to the appropriate ProductBook and returns its DTO.
     *  - (using the String product symbol from the Tradable object to determine which ProductBook it goes to)
     *
     * Then call the UserManager’s updateTradable method, passing it the Tradable’s use tradableID and a new TradableDTO created using the Tradable passed in.
     *  - Return the TradableDTO you receive back from the ProductBook.
     *
     *  If the Tradable passed in is null, throw a DataValidationException.
     */
    public TradableDTO addTradable(Tradable o) throws DataValidationException, IllegalArgumentException, InvalidPriceException {
        //Check if the Tradable passed in is null --> throw exception
        if (o == null) {
            throw new DataValidationException("Tradable cannot be null");
        }

        //Extract the product symbol from the tradable
            // Tradable.getproduct() --> Save in a local variable
            // Normalize to match how productBooks are keyed (trim + uppercase)
            // Tradable.getProduct() will return the Product symbol
        String sym = o.getProduct().trim().toUpperCase();

        // getProductBook() method from above will validate existence (and symbol format) and return the correct book given that symbol
        ProductBook book = getProductBook(sym);

        // UPDATE ON PRODUCT SIDE
        // Delegate to the ProductBook's add() method to add this tradable and capture the resulting DTO
            //add(Tradable) method will return a DTO
        TradableDTO dto = book.add(o);

        // UPDATE ON USER SIDE
        // Call the UserManager’s updateTradable() method
            //Passing it the Tradable’s user tradableID and the updated DTO that we just got from the add() method
            //Now we know we have proper info on both the USER and PRODUCT side of things
        UserManager.getInstance().updateTradable(o.getUser(), dto);

        // Return the TradableDTO you receive back from the ProductBook
        return dto;


    }

    /**
     *
     * TradableDTO[] addQuote(Quote q):
     * Adds a Quote (both BUY and SELL sides) to the appropriate ProductBook.
     *
     *  - Get the ProductBook (using the symbol in the Quote object)
     *  - Call removeQuotesForUser(passing the String user from the Quote object).
     *  - Call addTradable passing the BUY ProductBookSide from the Quote passed in (save the TradableDTO returned).
     *  - Call addTradable passing the SELL ProductBookSide from the Quote passed in (save the TradableDTO returned).
     *
     *  - Return the BUY and SELL TradableDTO’s in a 2-element array of TradableDTOs.
     *
     *  - If the quote passed in is null, throw a DataValidationException.
     */
    public TradableDTO[] addQuote(Quote q) throws DataValidationException, IllegalArgumentException, InvalidPriceException {
        //If the quote passed in is null, throw a DataValidationException.
        if (q == null) {
            throw new DataValidationException("Quote cannot be null");
        }

        //Extract the product symbol for this quote
        String sym = q.getProduct().trim().toUpperCase();

        //Get the ProductBook (using the symbol in the Quote object)
        ProductBook book = getProductBook(sym); // reuses symbol validation & existence checks

        //Call TradeBook's removeQuotesForUser (passing the String user from the Quote object --> q.getUser()).
        book.removeQuotesForUser(q.getUser());

        //Call addTradable() passing the BUY ProductBookSide from the Quote passed in (save the TradableDTO returned).
        TradableDTO buyDto = addTradable(q.getQuoteSide(BookSide.BUY));

        //Call addTradable() passing the SELL ProductBookSide from the Quote passed in (save the TradableDTO returned).
        TradableDTO sellDto = addTradable(q.getQuoteSide(BookSide.SELL));

        //Return the BUY and SELL TradableDTO’s in a 2-element array of TradableDTOs.
        return new TradableDTO[] { buyDto, sellDto };
    }

    /**
     *
     * TradableDTO cancel(TradableDTO o):
     * Cancels a specific tradable represented by its DTO and returns the resulting DTO.
     *
     *  - Using the String product symbol from the TradableDTO passed in, find the ProductBook.
     *  - Call that ProductBook’s “cancel” method passing it the side and tradable tradableID from the TradableDTO passed in.
     *      - If successful, return the TradableDTO returned from the ProductBook’s “cancel” method.
     *      - If the cancel attempt fails, print a message indicating the failure to cancel, and return a null.
     *
     *  - If the TradableDRO passed in is null, throw a DataValidationException.
     */
    public TradableDTO cancel(TradableDTO o) throws DataValidationException, IllegalArgumentException, InvalidPriceException {
        //If the TradableDRO passed in is null, throw a DataValidationException.
        if (o == null){
            throw new DataValidationException("Tradable DTO cannot be null");
        }

        // Extract the String symbol for this Tradable
        String sym = o.product().trim().toUpperCase();

        // Find the ProductBook using the string representation of the product
        ProductBook book = getProductBook(sym);

        //PRODUCT SIDE//////////////
        //Call that ProductBook’s “cancel” method passing it the proper book side (o.side()) and tradable tradableID (o.Id()) from the TradableDTO passed in
            //IllegalArgumentException comes from cancel()
        TradableDTO dto = book.cancel(o.side(), o.tradableId() );

        // If the cancel attempt fails, print a message indicating the failure to cancel, and return null
        if (dto == null) {
            System.out.println("Cancel failed for tradableID=" + o.tradableId() + ", product=" + sym + ", side=" + o.side());
            return null;
        }

        //USER SIDE//////////////
        // Update the User side to reflect the canceled state (if successful)
        UserManager.getInstance().updateTradable(dto.user(), dto);

        // If cancel() is successful, return the new TradableDTO returned from the ProductBook’s cancel method
        return dto;
    }

    /**
     * TradableDTO[] cancelQuote(String symbol, String user):
     *  Cancels a user's quote (both sides) for a given symbol.
     *
     *  - Using the String symbol, get the ProductBook using the String symbol passed in
     *  - Call the ProductBook's removeQuotesForUser passing it the String user.
     *  - Return the TradableDTO array that comes back from removeQuotesForUser.
     *
     *  - If the symbol passed in is null, throw a DataValidationException.
     *  - If the user if passed in is null, throw a DataValidationException.
     *  - If the product does not exist for the specified symbol, throw a DataValidationException.
     */
    public TradableDTO[] cancelQuote(String symbol, String user) throws DataValidationException, IllegalArgumentException, InvalidPriceException {
        //If the symbol passed in is null, throw a DataValidationException
        if (symbol == null){
            throw new DataValidationException("symbol cannot be null");
        }

        //If the user passed in is null, throw a DataValidationException
        if (user == null){
            throw new DataValidationException("user cannot be null");
        }

        // Normalize inputs
        String sym = symbol.trim().toUpperCase();
        String usr = user.trim().toUpperCase();

        //Get the ProductBook using the String symbol passed in
            //If the product does not exist for the specified symbol --> DataValidationException will be thrown from getProductBook()
        ProductBook book = getProductBook(sym);

        //PRODUCT SIDE//////////////
        //Cancel the Quotes belonging to that User--> call the ProductBook's removeQuotesForUser passing it the String user.
        TradableDTO [] dtoArray = book.removeQuotesForUser(usr);

        //USER SIDE//////////////
        // Update each Tradable on the User side to reflect canceled quote sides
        for (TradableDTO d : dtoArray) {
            if (d != null) {
                UserManager.getInstance().updateTradable(d.user(), d);
            }
        }

        //Return the TradableDTO array that comes back from removeQuotesForUser
        return dtoArray;

    }

    /**
     * Returns a textual summary of all ProductBooks. Implementation will append each book's toString().
     *  - Override the toString method to generate a String containing a summary of all ProductBooks
     *      - Be sure to let the ProductBook objects generate their part of the String
     */
    @Override
    public String toString() {
        // If no ProductBooks exist, indicate that clearly
        if (productBooks.isEmpty()) {
            return "No ProductBooks currently exist.";
        }

        StringBuilder sb = new StringBuilder();

        // Generate a string containing a summary of all ProductBooks
            // Each ProductBook handles its own internal formatting via its own toString() --> book.toString()
        for (ProductBook book : productBooks.values()) {
            sb.append(book.toString());
            sb.append("\n"); // Separate each ProductBook’s summary with a blank line
        }

        // Trim trailing newlines for clean formatting
        return sb.toString().trim();
    }
}
