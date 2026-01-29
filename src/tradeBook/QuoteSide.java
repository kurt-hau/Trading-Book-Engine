package tradeBook;

import tradeBook.pricing.Price;

/**
 * QuoteSide class represents one side (buy or sell) of a Quote.
 * Implements the Tradable interface and delegates shared logic to TradableState.
 *      Essentially a sibling of Order (related via their roles --> Interface)
 */
public class QuoteSide implements Tradable {

    /** Delegate object handling all shared Tradable logic and validation. */
    private final TradableState state;

    // ------------------------------------------------------------
    // CONSTRUCTOR
    // ------------------------------------------------------------
    /**
     * Constructs a new QuoteSide by delegating validation and initialization
     * to the TradableState helper class.
     *
     * @param user The 3-letter user code.
     * @param product The product symbol.
     * @param price The Price object for this quote side.
     * @param originalVolume The original volume.
     * @param side The side of this quote (BUY or SELL).
     *  Exception will come up if any validation fails in TradableState.
     */
    public QuoteSide(String user,
                     String product,
                     Price price,
                     int originalVolume,
                     BookSide side)
            throws IllegalArgumentException {

        this.state = new TradableState(user, product, price, side, originalVolume);
    }

    // ------------------------------------------------------------
    // INTERFACE METHODS (All DELEGATED TO TradableState)
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
    // toString() --> The only unique method that QuoteSide itself owns
    // ------------------------------------------------------------
    /**
     * The main difference is the wording on the string --> "side quote for" rather than "order for"
     * QuoteSide toString() also doesn't say "at" in between the Product and Price, it uses a colon : instead
     * @return a formatted QuoteSidetoString() representation
     */
    @Override
    public String toString() {
        return state.getUser() + " " + state.getSide() + " side quote for " +
                state.getProduct() + ": " + state.getPrice() +
                ", Orig Vol: " + state.getOriginalVolume() +
                ", Rem Vol: " + state.getRemainingVolume() +
                ", Fill Vol: " + state.getFilledVolume() +
                ", CXL Vol: " + state.getCancelledVolume() +
                ", ID: " + state.getTradableId();
    }
}