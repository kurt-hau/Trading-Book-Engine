package tradeBook;

/**
 * This interface is implemented by classes that want to be able to register for Current Market update.
 *      - (currently only the User class will implement this).
 *      - define the following method:
            void updateCurrentMarket(String symbol, CurrentMarketSide buySide, CurrentMarketSide sellSide);
 */
public interface CurrentMarketObserver {
    /**
     * Called by the CurrentMarketPublisher whenever a new Current Market snapshot is available for a subscribed stock symbol.
     *      - Implementers of this interface should use the values provided to store or display top-of-book data.
     *
     * @param symbol   The stock symbol whose Current Market has been updated.
     * @param buySide  The top-of-book buy side market data (price and volume).
     * @param sellSide The top-of-book sell side market data (price and volume).
     */
    void updateCurrentMarket(String symbol,
                             CurrentMarketSide buySide,
                             CurrentMarketSide sellSide);
}
