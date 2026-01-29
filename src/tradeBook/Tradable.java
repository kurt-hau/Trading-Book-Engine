package tradeBook;

import tradeBook.pricing.Price;

/**
 * The Tradable interface defines behaviors that any tradable object (Orders or QuoteSides) must implement.*/
public interface Tradable {

    /**
     * Returns the unique ID of this tradable (generated using user + product + price + nanotime).
     *  - Nanotime will be different when comparing to expected doc of course */
    String getTradableId();

    /**
     * Returns the current remaining volume (quantity) that has not yet been filled or canceled.*/
    int getRemainingVolume();

    /**
     * Sets the cancelled volume for this tradable.
     * Used when part or all of a tradable is canceled before being filled.*/
    void setCancelledVolume(int newVol) throws IllegalArgumentException;

    /**
     * Returns the total volume that has been canceled for this tradable. */
    int getCancelledVolume();

    /**
     * Sets the remaining volume for this tradable.
     * Typically used when part of the tradable is filled.*/
    void setRemainingVolume(int newVol) throws IllegalArgumentException;

    /**
     * Generates and returns a Data Transfer Object (DTO) representing this tradable’s current state.
     * This allows access to the tradable’s data without exposing the tradable object itself. */
    TradableDTO makeTradableDTO();

    /**
     * Returns the price at which this tradable is set to buy or sell. */
    Price getPrice();

    /**
     * Sets the filled volume for this tradable.
     * Used to update how much of the tradable has been completed through trades.*/
    void setFilledVolume(int newVol) throws IllegalArgumentException;

    /**
     * Returns the total volume that has been filled (completed) for this tradable.*/
    int getFilledVolume();

    /**
     * Returns which side (BUY or SELL) this tradable belongs to.*/
    BookSide getSide();

    /**
     * Returns the user code (3-letter identifier) of the trader who owns this tradable.*/
    String getUser();

    /**
     * Returns the product symbol (e.g., “WMT”, “GOOGL”, etc.) for which this tradable was created.*/
    String getProduct();

    /**
     * Returns the original volume (quantity) that was specified when the tradable was first created.*/
    int getOriginalVolume();
}