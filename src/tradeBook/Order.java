package tradeBook;

import tradeBook.pricing.Price;

/**
 * The Order class represents a single tradable order (buy or sell) from a user.
 * It implements the Tradable interface
 *  Delegates all shared behavior to the TradableState helper class for method logic
 */
public class Order implements Tradable {

    /** Create a variable of the intrenal delegate that handles all shared Tradable logic and state. */
    private final TradableState state;

    // ------------------------------------------------------------
    // CONSTRUCTOR
    // ------------------------------------------------------------
    /**
     * Constructs a new Order object by delegating initialization and set() validation methods to the TradableState delegate helper class.
     *
     * @param user The 3-letter user code.
     * @param product The stock symbol (1–5 characters).
     * @param price The Price object representing the order price.
     * @param side The side of the order (BUY or SELL) comes from an enum.
     * @param originalVolume The original order volume.
     *     - Must throw an exception that may come from the set() logic in TradableState
     */
    public Order(String user,
                 String product,
                 Price price,
                 int originalVolume,
                 BookSide side) throws IllegalArgumentException {
        this.state = new TradableState(user, product, price, side, originalVolume);
    }

    // ------------------------------------------------------------
    // INTERFACE METHODS (All DELEGATED TO TradableState
    // ------------------------------------------------------------

    @Override
    public String getUser() {
        return state.getUser();
    }

    @Override
    public String getProduct() {
        return state.getProduct();
    }

    @Override
    public Price getPrice() {
        return state.getPrice();
    }

    @Override
    public BookSide getSide() {
        return state.getSide();
    }

    @Override
    public int getOriginalVolume() {
        return state.getOriginalVolume();
    }

    @Override
    public int getRemainingVolume() {
        return state.getRemainingVolume();
    }

    @Override
    public void setRemainingVolume(int newVol) throws IllegalArgumentException {
        state.setRemainingVolume(newVol);
    }

    @Override
    public int getCancelledVolume() {
        return state.getCancelledVolume();
    }

    @Override
    public void setCancelledVolume(int newVol) throws IllegalArgumentException {
        state.setCancelledVolume(newVol);
    }

    @Override
    public int getFilledVolume() {
        return state.getFilledVolume();
    }

    @Override
    public void setFilledVolume(int newVol) throws IllegalArgumentException {
        state.setFilledVolume(newVol);
    }

    @Override
    public String getTradableId() {
        return state.getTradableId();
    }

    @Override
    public TradableDTO makeTradableDTO() {
        return state.makeTradableDTO();
    }

    // ------------------------------------------------------------
    // toString() --> The only unique method that Order itself owns
    // ------------------------------------------------------------
    /**
     * Returns a formatted string representation of this Order, matching the required format for Order objects
     *      Still calls the TradableState to get the data for this
     * Format:
     * USER SIDE order: PRODUCT at PRICE, Orig Vol: ORIGINAL VOLUME,
     * Rem Vol: REMAINING VOLUME, Fill Vol: FILLED VOLUME,
     * CXL Vol: CANCELLED VOLUME, ID: ID
     *
     * Example output from the Asgn PDF:
     * AXE SELL order: TGT at $134.00, Orig Vol: 50, Rem Vol: 50,
     * Fill Vol: 0, CXL Vol: 0, ID: AXETGT$134.00506492572504400
     */
    @Override
    public String toString() {
        return state.getUser() + " " + state.getSide() + " order: " +
                state.getProduct() + " at " + state.getPrice() +
                ", Orig Vol: " + state.getOriginalVolume() +
                ", Rem Vol: " + state.getRemainingVolume() +
                ", Fill Vol: " + state.getFilledVolume() +
                ", CXL Vol: " + state.getCancelledVolume() +
                ", ID: " + state.getTradableId();
    }
}
