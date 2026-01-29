package tradeBook;

import tradeBook.pricing.Price;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

/**
 * ProductBookSide manages one side of the book (BUY or SELL) for a single product.
 * Stores Tradable objects at price levels and provides book summaries/top-of-book info.
 *
 * Just wrote the outline -> Still need to go in later and fill out the method logic
 * Maybe use Java's Comparator class for the different TreeMap price orderings (Ascending/Descending)?
 */
public class ProductBookSide {

    // ------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------
    /** Which side this book represents (BUY or SELL). */
    private final BookSide side;

    /** Price level -> For each price on the side, have a FIFO list of Tradables stored at that exact price. */
    private final TreeMap<Price, ArrayList<Tradable>> bookEntries;


    // ------------------------------------------------------------
    // CONSTRUCTOR
    //  - Validates inputs using private set() helpers
    //  - Initializes all fields AFTER validation
    // ------------------------------------------------------------
    /**
     * Constructs a ProductBookSide for the specified side (BUY or SELL).
     *
     * @param side The side this book represents.
     */
    public ProductBookSide(BookSide side) throws IllegalArgumentException {
        this.side = setSide(side);
        this.bookEntries = setEntriesForSide(this.side);
    }


    // ------------------------------------------------------------
    // PRIVATE set() METHODS
    //  Designate the proper Side (Buy or Sell)
    //  Create a Tree map that has the proper ordering (Descend or Ascend)
    // ------------------------------------------------------------

    /**
     * Validates and returns the BookSide.
     */
    private static BookSide setSide(BookSide s) throws IllegalArgumentException {
        if (s == null) {
            throw new IllegalArgumentException("BookSide cannot be null.");
        }
        return s;
    }

    /**
     * Builds the TreeMap used to store price levels.
     * BUY side -> highest prices first (reverse order).
     * SELL side -> lowest prices first (natural order).
     */
    private static TreeMap<Price, ArrayList<Tradable>> setEntriesForSide(BookSide s) {
        // BUY side: Order Prices in reverse (descending) order
        if (s == BookSide.BUY) {
            return new TreeMap<>(Comparator.reverseOrder());
        }
        // SELL side: Order Prices in natural (ascending) order
        return new TreeMap<>();
    }


    // ------------------------------------------------------------
    // PUBLIC API Methods
    // ------------------------------------------------------------

    /**
     * Adds a Tradable to this side of the book at its price level.
     *  - Validate non-null input and matching side.
     *  - Create price slot if needed.
     *  - Append to the list to preserve FIFO at that price.
     */
        // Does the price already exist in the tree map?
            //Yes, then add it the end of the existing array of price objects at that price
            //No, then create a slot for it in the Tree map, based off the proper tree map ordering
                //Create an Array at that new slot in the treemap and then place the price at the front of the array (should eb the only element)
    public TradableDTO add(Tradable o) throws IllegalArgumentException, DataValidationException {
        //Error Checks via the helper method
        validateSide(o);

        // Print **ADD:** for QuoteSide tradables only
            //Order tradables are already printed in ProductBook.add
//        if (o instanceof QuoteSide) {
//            System.out.println("**ADD: " + o);
//        }

        Price priceKey = o.getPrice();

        // Obtain existing slot or create a new one for this price level (TreeMap keeps prices sorted)
            //slotFor() will create the new slot if needed
        ArrayList<Tradable> slot = slotFor(priceKey);

        // Append teh price to maintain FIFO at this price level
        slot.add(o);

        // Mirror to UserManager so the user's ledger reflects this new tradable
        TradableDTO dtoAdd = o.makeTradableDTO();
        UserManager.getInstance().updateTradable(o.getUser(), dtoAdd);
        return dtoAdd;
    }

    /**
     * Removes a Tradable from the book by its unique tradableID.
     *  - Find the price slot, remove the tradable.
     *  - Print a message signaling that the price was removed
     *  - Update cancelled and remaining values
     *  - Remove (Prune) the price level slot if it becomes empty after
     *  - Return via DTO
     */
    public TradableDTO cancel(String tradableId) throws IllegalArgumentException, DataValidationException {
        if (tradableId == null || tradableId.isEmpty()) {
            throw new IllegalArgumentException("Tradable ID cannot be null or empty.");
        }

        // Defer map (TreeMap) key removal until AFTER we finish iterating keySet()
        Price priceKeyToPrune = null;   // record which price key to prune after loops finish
        TradableDTO result = null;      // hold the DTO to return after deferred pruning
        boolean found = false;          // signal to break both loops cleanly once located

        // Iterate through all price levels (slots)
        for (Price priceKey : bookEntries.keySet()) {
            ArrayList<Tradable> slot = bookEntries.get(priceKey);
            if (slot == null) continue;

            // Search within this slot for a tradable with the matching ID
            for (int i = 0; i < slot.size(); i++) {
                Tradable t = slot.get(i);
                if (t.getTradableId().equals(tradableId)) {

                    // Print the cancellation message
//                    System.out.println("**CANCEL: " + t);

                    // Update the tradable’s volumes
                        //Add what was left to the cancelled volume
                        //Set the remaining volume to 0
                    t.setCancelledVolume(t.getCancelledVolume() + t.getRemainingVolume());
                    t.setRemainingVolume(0);

                    // Remove the tradable from its slot
                    slot.remove(i);

                    // If the slot is now empty after that removal, remove the entire price entry
                        //This means it was the last tradable left at that price
                    result = t.makeTradableDTO();

                    // Record the key for deferred pruning; do NOT remove from bookEntries here
                        // because we're still inside a keySet() iteration.
                    if (slot.isEmpty()) {
                        priceKeyToPrune = priceKey;
                    }

                    found = true;
                    break;
                }
            }
            if (found) {
                // Break the outer loop as well; the target tradable has been processed.
                break;
            }
        }
        // Perform deferred removal outside of the keySet iteration to avoid concurrent modification
        if (priceKeyToPrune != null) {
            bookEntries.remove(priceKeyToPrune);
        }

        // Return the DTO captured earlier (after pruning), ensuring caller gets the cancelled snapshot.
        // Update the user's view with the cancelled state snapshot
        if (result != null) {
            UserManager.getInstance().updateTradable(result.user(), result);
            return result;
        }

        // If no tradable matching that ID is found
        return null;
    }

    /**
     * Removes a user's quote (QuoteSide) from this side of the book.
     * Searches all slots at each Price key for a QuoteSide whose userName matches the provided userName.
     *  Once the username is found:
     *  - Calls cancel(tradableId) to perform the proper cancellation steps
     *  - Prunes the slot if it becomes empty
     *  - Returns the TradableDTO produced by cancel()
     *
     * @param userName The 3-letter user code whose quote should be removed.
     */
    // ProductBook will have to call this on BOTH sides to fully remove a user's quote
    // I wonder if only one quote per user per side is expected; so I should stop after first match?
        //How come we don't use the specific Tradable ID?
    public TradableDTO removeQuotesForUser(String userName) throws IllegalArgumentException, DataValidationException {
        if (userName == null || userName.isBlank() || !userName.matches("[A-Za-z]{3}")) {
            throw new IllegalArgumentException("User code must be exactly 3 letters (A–Z).");
        }

        // Defer pruning until after iteration to avoid modifying the map while iterating its keySet()
        Price priceKeyToPrune = null;   // which price level to prune after loops
        TradableDTO result = null;      // DTO returned from cancel()
        boolean found = false;          // used to break out of nested loops once the quote is handled

        // Iterate over all price levels (slots) to find a QuoteSide object for this user
        //Arrays store Tradables (Orders, QuoteSides)
        //This method will have to be called on both sides
        for (Price priceKey : bookEntries.keySet()) {
            ArrayList<Tradable> slot = bookEntries.get(priceKey);
            if (slot == null || slot.isEmpty()) {
                continue;
            }

            for (int i = 0; i < slot.size(); i++) {
                Tradable t = slot.get(i);

                // This method is for Quotes; ensure we only target QuoteSide objects for this user
                //Should I be using instanceof() -> He said in class not to?

                //If it's a QuoteSide Object and matches the Username:
                if ((t instanceof QuoteSide) && userName.equals(t.getUser())) {

                    // Delegate the actual removal and volume adjustments to the cancel() method
                    //Returns a DTO object so save it as one
                    TradableDTO dto = cancel(t.getTradableId());
                    result = dto; // keep to return after safe pruning

                    // If the slot is empty after removing the quote, record the key for deferred pruning.
                    // Do NOT remove from bookEntries here (I'm still in the in keySet() iteration).
                    ArrayList<Tradable> currentSlot = bookEntries.get(priceKey);
                    if (currentSlot != null && currentSlot.isEmpty()) {
                        priceKeyToPrune = priceKey;
                    }

                    // Only one quote per user per side is expected; stop after first match?
                    found = true; // we found and handled the user's quote on this side
                    break;        // break inner loop
                }
            }
            if (found) {
                // Break the outer loop as well; we've handled the user's quote on this side.
                break;
            }
        }

        // Perform deferred removal now that iteration is complete
        // No matching quote found for this user on this side of the book
        if (priceKeyToPrune != null) {
            bookEntries.remove(priceKeyToPrune);
        }

        // If we cancelled a quote, return its DTO; otherwise return null.
        // Update the user's view for this removed quote (if any)
        if (result != null) {
            UserManager.getInstance().updateTradable(result.user(), result);
            return result;
        }

        return null;
    }

    /**
     * Returns the best/top price on this side of the book, or null if empty.
     */
    //If the book is empty --> return null
    //Otherwise, just return the top item, should be properly sorted
    public Price topOfBookPrice() {
        if (bookEntries.isEmpty()){
            return null;
        }
        return bookEntries.firstKey();
    }

    /**
     * Returns the total remaining volume at the top-of-book price (0 if none).
     * - If the ProductBookSide is the BUY side, return total of all tradable volumes at the highest price in the bookEntries TreeMap.
     * - If the ProductBookSide is the SELL side, return total of all tradable volumes at the lowest price in the bookEntries TreeMap.
     */
    //Don't have to do a null check?
    public int topOfBookVolume() {
        //If it's empty, the volume is 0
        if (bookEntries.isEmpty()){
            return 0;
        }

        // Get the top price (firstKey accounts for both the ordering of BUY and SELL sides)
        Price topPrice = bookEntries.firstKey();
        return sumVolumeAt(topPrice);

    }

    /**
     * Returns a depth snapshot for this side of the book as a list of TradableDTOs,
     * ordered by price for this side (TreeMap ordering) and FIFO within each price slot.
     *  - This returns DTOs (safe copies) rather than live Tradable references.
     *
     *  "Giving me all the orders or quotes that exist on this side right now, grouped and ordered by price"
     *
     * @return List of TradableDTO entries representing the current book depth.
     */
    public List<TradableDTO> bookDepth() {
        // If empty, return an empty list (no nulls)
        if (bookEntries.isEmpty()) {
            return new ArrayList<>();
        }

        ArrayList<TradableDTO> depth = new ArrayList<>();

        // Iterate TreeMap in side-correct order; each price key has a FIFO slot of Tradables
        for (Price priceKey : bookEntries.keySet()) {
            ArrayList<Tradable> slot = bookEntries.get(priceKey);
            if (slot == null || slot.isEmpty()) {
                continue;
            }

            // For each Tradable at this price, add a DTO snapshot (no live references)
            for (Tradable t : slot) {
                depth.add(t.makeTradableDTO());
            }
        }

        return depth;
    }



    /**
     * getOrdersWithPrice(Price p)
     *  - PDF spec: public List<TradableDTO> getOrdersWithPrice(Price p)
     *  - Return DTOs for all Tradables at the specified price level (slot)
     *  - Never expose live Tradable references
     *  - If price is null or the slot is empty/missing, return an empty list
     *
     * @param p The price level to inspect.
     * @return A list of TradableDTOs at that price, or an empty list if none.
     */
    public List<TradableDTO> getOrdersWithPrice(Price p) {
        if (p == null) {
            return new ArrayList<>();
        }
        ArrayList<Tradable> slot = bookEntries.get(p);
        if (slot == null || slot.isEmpty()) {
            return new ArrayList<>();
        }

        //Iterate through the Price slot and make a DTO for each Tradable object at that price
        ArrayList<TradableDTO> dtos = new ArrayList<>();
        for (Tradable t : slot) {
            dtos.add(t.makeTradableDTO());
        }

        //Return the array list of DTO objects
        return dtos;
    }


    /**
     * hasPriceLevel(Price p)
     *  - Return true if a price level exists AND its slot is non-empty
     *  - Null price returns false
     *
     * @param p The price level to check.
     * @return true if the slot exists and contains at least one Tradable; false otherwise.
     */
    public boolean hasPriceLevel(Price p) {
        if (p == null) {
            return false;
        }
        ArrayList<Tradable> slot = bookEntries.get(p);
        return slot != null && !slot.isEmpty();
    }


    /**
     * sideIsEmpty()
     * Returns true if there are no non-empty slots on this side
     *  - If the TreeMap itself is empty → returns true.
     *  - Otherwise it scans each price slot and returns false as soon as it finds one that’s non-empty.
     *  - If it finishes the scan without finding anything → returns true.
     */
    public boolean sideIsEmpty() {
        if (bookEntries.isEmpty()) {
            return true;
        }
        for (Price p : bookEntries.keySet()) {
            ArrayList<Tradable> slot = bookEntries.get(p);
            if (slot != null && !slot.isEmpty()) {
                return false;
            }
        }
        return true;
    }




    /**
     * Trades out volume from the top of this book side at prices that are
     * at-or-better than the threshold {@code price}, up to {@code vol} units.
     *  - BUY side: a price is tradable if topPrice.compareTo(price) = 1 or 0
     *  - SELL side: a price is tradable if topPrice.compareTo(price) = 0 or -1
     *      - To Trade BUY >= SELL -> We can trade at the SELL PRICE
     *      - To Trade SELL <= BUY -> We can trade at the SELL PRICE
     *      - To Trade BUY = SELL -> We can trade at either price (They are the same)
     *
     * @param price The threshold Price (at-or-better).
     * @param vol   The maximum volume to trade out (must be greater than 0).
     *
     * I changed the variable names remainder and rv to ones that were more intuitive to me
     */


    //Must balance both the amount that is requested to be filled by the trade as well as..
        //The amount that we actually have at that specific price to trade
        // --> Can't trade more than we have
    public void tradeOut(Price price, int vol) throws IllegalArgumentException, DataValidationException {
        if (price == null) {
            throw new IllegalArgumentException("Price cannot be null.");
        }
        if (vol <= 0) {
            throw new IllegalArgumentException("Volume to trade must be > 0.");
        }

        int volToTrade = vol;
        // Tag used in fill prints to match expected output format
        final String sideTag = (this.side == BookSide.BUY) ? "BUY" : "SELL";

        // ------------------------------------------------------------
        // See if prices are compatible for trading
        // ------------------------------------------------------------

        // Loop while we still have volume to trade and the top price is tradable vs the threshold
        while (volToTrade > 0) {
            // Get the current top-of-book price
            Price topPrice = topOfBookPrice();
            if (topPrice == null) {
                return; // nothing at this side
            }

            // Check "at or better than" condition based on side
            boolean tradable;

            //If we are comparing a BUY price to a SELL price
                //Buy price is topPrice
                //Sell price is parameter: price
            if (this.side == BookSide.BUY) {
                tradable = topPrice.compareTo(price) >= 0;

            //If we are comparing a BUY price to a SELL price
                //Sell price is topPrice
                //Buy price is parameter: price
            } else {
                tradable = topPrice.compareTo(price) <= 0;
            }
            if (!tradable) {
                return; // top is not tradable vs threshold; stop
            }

            // ------------------------------------------------------------
            // Execute the trade
            // ------------------------------------------------------------

            // Get the slot (FIFO list) for the top price
            ArrayList<Tradable> slot = bookEntries.get(topPrice);
            if (slot == null || slot.isEmpty()) {
                pruneIfEmpty(topPrice);
                continue;// refresh top slot and try again
            }

            // Compute total remaining volume at this top price
            int totalVolAtPrice = sumVolumeAt(topPrice);

            if (totalVolAtPrice <= 0) {
                // Nothing left at that price; prune the price and continue
                pruneIfEmpty(topPrice);
                continue;
            }

            // ------------------------------------------------------------
            // FULL FULL of top price level (volToTrade >= totalVolAtPrice)
            //      - Entire top price slot is emptied (meaning each tradable at that price slot becomes fully filled too), the slot gets pruned.
            //      - Remaining request volume could be left (only if volToTrade > totalVolAtPrice); we subtract the slot total.
            //      - We remain inside tradeOut(): the loop continues at the next best price on this side until the threshold stops it
            // ------------------------------------------------------------
            if (volToTrade >= totalVolAtPrice) {
                for (int i = 0; i < slot.size(); ) {
                    Tradable t = slot.get(i);
                    int tradableVolLeft = t.getRemainingVolume(); // tradable's remaining volume

                    // Empty out the entire tradable's remaining volume
                    // Print using the proper side tag
                    if (tradableVolLeft > 0) {
                        t.setFilledVolume(t.getFilledVolume() + tradableVolLeft);
                        t.setRemainingVolume(0);
                        System.out.println("\tFULL FILL: (" + sideTag + " " + tradableVolLeft + ") " + t);
                    }
                    // Update UserManager for this tradable's new (fully filled) state
                    UserManager.getInstance().updateTradable(t.getUser(), t.makeTradableDTO());
                    // remove from slot when tradable's remaining is zero
                    if (t.getRemainingVolume() == 0) {
                        slot.remove(i);
                    } else {
                        i++;
                    }
                }

                // Prune empty slot
                pruneIfEmpty(topPrice);
                volToTrade -= totalVolAtPrice; // continue to the next price if still have volume left in the trade request
                continue;
            }

            // ------------------------------------------------------------
            // PARTIAL FILL path : distribute the volToTrade proportionally across the current price level
            //      - There will be volume left over that will be needed to be carried over to the next Tradable (iteration)
            //      - There should be no volume left in the trade request at the end
            //      - Iterate FIFO over each Tradable in the one price slot at the top of book
            // PRO-RATA:
            //  - Denominator is the fixed total remaining at this price BEFORE allocation (slotTotalVol).
            //  - For each tradable, compute a tentative share as ceil(volToTrade * (rv_i / slotTotalVol)).
            //  - Apply fills FIFO, capping by each tradable's remaining and the running remainder (orderVolLeft).
            //  - Any leftover remainder is NOT discarded; it is carried to the next iteration/price via volToTrade.
            // ------------------------------------------------------------

            // (orderVolLeft) tracks how much of the external order is still left to distribute
            int orderVolLeft = volToTrade;

            // The total volume that this price level can offer
            final int slotTotalVol = totalVolAtPrice;

            for (int i = 0; i < slot.size() && orderVolLeft > 0; ) {
                Tradable t = slot.get(i);

                // (tradableVolLeft) is how much volume this Tradable still has available to sell/buy.
                    //Remember multiple tradable's per price slot
                int tradableVolLeft = t.getRemainingVolume(); // tradable's remaining volume
                if (tradableVolLeft <= 0) {
                    slot.remove(i);
                    continue;
                }

                // PRO RATA: ratio-based allocation with ceiling
                    // Compute allocation using the ORIGINAL request for this price level,
                    // not the shrinking remainder, so rounding/caps can be redistributed to later entries.
                double ratio = (double) tradableVolLeft / (double) slotTotalVol;

                // Should give a reasonable proportion that each tradable should get
                int toTrade = (int) Math.ceil(volToTrade * ratio);


                // Cap #1 — by the live remainder (orderVolLeft) -> Don’t allocate more than what’s left to trade overall
                    // Even if the pro-rata gave a reasonable number, ensure that we still have that much left
                    // This usually means that the last tradable may get a little less than it deserves proportionally
                if (toTrade > orderVolLeft) {
                    toTrade = orderVolLeft;
                }

                // Cap #2 — by the tradable’s own remaining (tradableVolLeft) -> Don’t allocate more than what the tradable actually has
                if (toTrade > tradableVolLeft) {
                    toTrade = tradableVolLeft;
                }

                // If there's still more of the trade request to fill, keep going
                if (toTrade <= 0) {
                    i++;
                    continue;
                }

                // Apply the fill
                t.setFilledVolume(t.getFilledVolume() + toTrade);
                t.setRemainingVolume(tradableVolLeft - toTrade);

                // Should only print partial fill, but signal how much of the tradable was filled
                if (t.getRemainingVolume() == 0) {
                    System.out.println("\tFULL FILL: (" + sideTag + " " + toTrade + ") " + t);
                    // Update UserManager for this tradable's updated (partial/full) state
                    UserManager.getInstance().updateTradable(t.getUser(), t.makeTradableDTO());
                    slot.remove(i);
                } else {
                    System.out.println("\tPARTIAL FILL: (" + sideTag + " " + toTrade + ") " + t);
                    // Update UserManager for this tradable's updated (partial/full) state
                    UserManager.getInstance().updateTradable(t.getUser(), t.makeTradableDTO());
                    i++;
                }

                // Subtract what we just allocated so that the next tradable sees a smaller remaining total for that price
                orderVolLeft -= toTrade;
            }

            // After proportional distribution, carry the leftover (if any) to the outer loop.
                // If earlier tradables were capped by their remaining volume, orderVolLeft may be > 0 and
                // should be applied to the same price (if volume remains there) or the next best price.
            volToTrade = orderVolLeft;

            // Prune empty slot if fully consumed, then let the outer while loop continue.
            pruneIfEmpty(topPrice);
            continue;
        }
    }

    /**
     * Returns a formatted string summary of this ProductBookSide.
     * - Lists each price level (in order) and the Tradables at that level.
     * - Uses the Tradable’s toString() to display details per line.
     * - If the book side is empty, displays “Empty" in braces
     *
     * @return A formatted string representing this ProductBookSide’s contents.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Header section for side label
        sb.append("Side: ").append(side).append("\n");

        // If this side has no tradables, show "<Empty>"
        if (sideIsEmpty()) {
            sb.append("\t<Empty>\n");
            return sb.toString();
        }

        // Iterate through all price levels (TreeMap is already ordered)
        for (Price priceKey : bookEntries.keySet()) {
            ArrayList<Tradable> slot = bookEntries.get(priceKey);
            if (slot == null || slot.isEmpty()) {
                continue;
            }

            // Print formatted price header for this level
            sb.append("\t").append(priceKey.toString()).append(":\n");

            // Print each Tradable’s string representation (Order or QuoteSide)
            for (Tradable t : slot) {
                sb.append("\t\t").append(t.toString()).append("\n");
            }
        }

        // Return the assembled multi-line summary
        return sb.toString();
    }

    // ------------------------------------------------------------
    // PRIVATE HELPERS (THESE NO USED/IMPLEMENTED
    // ------------------------------------------------------------
//    /**
//     * Returns a depth summary for this side, ordered by price for this side.
//     *  - Each entry should be a safe snapshot (DTOs), not live references.
//     */
//    public List<TradableDTO> bookDepth() {
//        // TODO: implement
//        return new ArrayList<>();
//    }
//
//    /**
//     * Returns safe DTOs for all Tradables at a specific price (empty list if none).
//     */
//    public List<TradableDTO> getOrdersWithPrice(Price p) {
//        // TODO: implement
//        return new ArrayList<>();
//    }
//
//    /**
//     * Returns true if a price level exists and is non-empty.
//     */
//    public boolean hasPriceLevel(Price p) {
//        // TODO: implement
//        return false;
//    }
//
//    /**
//     * Returns true if this side has no price levels or all are empty.
//     */
//    public boolean isEmpty() {
//        // TODO: implement
//        return true;
//    }
//
//    /**
//     * (For matching later) Consume volume starting from the top-of-book price,
//     * honoring FIFO within price levels. Returns the actually consumed volume.
//     */
//    public int consumeFromTop(int desiredVolume) {
//        // TODO: implement
//        return 0;
//    }


    // ------------------------------------------------------------
    // PRIVATE HELPERS only used in functions within this class
    // ------------------------------------------------------------

    /**
     * Returns (or creates) the FIFO list for a given price level.
     */
    private ArrayList<Tradable> slotFor(Price p) throws IllegalArgumentException {
        if (p == null) {
            throw new IllegalArgumentException("Price cannot be null.");
        }

        // Obtain existing slot
        ArrayList<Tradable> slot = bookEntries.get(p);
        if (slot == null) {

            // Or create a new one for this price level if there wasn't one before (TreeMap keeps prices sorted)
            slot = new ArrayList<>();
            bookEntries.put(p, slot);
        }
        return slot;
    }

    /**
     * Removes the price level if its list becomes empty.
     */
    private void pruneIfEmpty(Price p) {
        if (p == null) {
            return;
        }
        ArrayList<Tradable> slot = bookEntries.get(p);
        if (slot != null && slot.isEmpty()) {
            bookEntries.remove(p);
        }
    }

    /**
     * Returns the first non-empty price entry according to side ordering, or null if none.
     */
    private Price firstNonEmptyPrice() {
        for (Price p : bookEntries.keySet()) {
            ArrayList<Tradable> slot = bookEntries.get(p);
            if (slot != null && !slot.isEmpty()) {
                return p;
            }
        }
        return null;
    }

    /**
     * Sums remaining volume across all Tradables at the given price level.
     *  - Iterates through the price objects, aggregating a total colume
     */
    private int sumVolumeAt(Price p) {
        if (p == null) {
            return 0;
        }
        ArrayList<Tradable> slot = bookEntries.get(p);
        if (slot == null || slot.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (Tradable t : slot) {
            total += t.getRemainingVolume();
        }
        return total;
    }

    /**
     * Validates that the Tradable is non-null and belongs to this side.
     */
    private void validateSide(Tradable t) throws IllegalArgumentException {
        if (t == null) {
            throw new IllegalArgumentException("Tradable cannot be null.");
        }
        if (t.getPrice() == null) {
            throw new IllegalArgumentException("Tradable price cannot be null.");
        }
        if (t.getSide() != this.side) {
            throw new IllegalArgumentException("Tradable side does not match this ProductBookSide.");
        }
    }
}