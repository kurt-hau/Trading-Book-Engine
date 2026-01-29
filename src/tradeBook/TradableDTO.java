package tradeBook;

import tradeBook.pricing.Price;

/**
 * Data Transfer Object for Tradable entities.
 * Encapsulates tradable details for transfer between layers.
 */
public record TradableDTO(
        //Automatically get:
        // private final fields
        //  Generates component accessors named after each field (e.g., dto.user(), dto.price()) as well as equals(), hashCode(), and toString()
        //  These are only created to be used on the DTO once the DTO has been constructed

        String user,    //UserId ("ANN", "BOB", "CAT")
        String product, //Will be the product symbol for this (1-5 alphanumeric, with optional '.')
        Price price,    //Will show errors until we bring the price class in from Part 1
        int originalVolume,
        int remainingVolume,
        int cancelledVolume,
        int filledVolume,
        BookSide side,  //Will show errors until we make the BookSide class
        String tradableId   //Will be the Tradable ID, not the user ID
) {

    // ------------------------------------------------------------
    // CONSTRUCTOR
    // ------------------------------------------------------------
    /**
     * Constructs a TradableDTO from a Tradable object (Order or QuoteSide)
     */
    public TradableDTO(Tradable tradable) {
        this(
            tradable.getUser(),
            tradable.getProduct(),
            tradable.getPrice(),
            tradable.getOriginalVolume(),
            tradable.getRemainingVolume(),
            tradable.getCancelledVolume(),
            tradable.getFilledVolume(),
            tradable.getSide(),
            tradable.getTradableId()
        );
    }

    //Need to implement for proper output
    @Override
    public String toString() {
        // User-view format required by the assignment.
        // Example:
        // Product: WMT, Price: $134.00, OriginalVolume: 88, RemainingVolume: 0,
        // CancelledVolume: 0, FilledVolume: 88, User: ANA, Side: BUY, Id: ANAWMT$134.00...
        return String.format(
                "Product: %s, Price: %s, OriginalVolume: %d, RemainingVolume: %d, " +
                        "CancelledVolume: %d, FilledVolume: %d, User: %s, Side: %s, Id: %s",
                product(),
                price(),              // Price.toString() should already be "$134.00"
                originalVolume(),
                remainingVolume(),
                cancelledVolume(),
                filledVolume(),
                user(),
                side(),
                tradableId()          // use the accessor name that matches your record field
        );
    }


}
