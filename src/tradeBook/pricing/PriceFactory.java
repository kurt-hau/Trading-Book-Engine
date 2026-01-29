
package tradeBook.pricing;

import java.util.HashMap;
import java.util.Map;

public final class PriceFactory {

    // Prevent outside instantiation of this class --> Can only be reached by a call from Price class
    private PriceFactory() { }

    /**
     * PRICE CACHE (HashMap) — order the Price objects by cents
     *  - Key: int cents value
     *  - Value: Price object for that cents value
     *      - We cap the size at 10,000 and purge lowest entries when over capacity
     *      - I could change the logic for purging later but will stick with this for now cuz it seems to work
     *
     *  - I think thus modification should now satisfy the flyweight design pattern
     */

    //The maximum number of Price objects that we can allow in the CACHE
    private static final int MAX_ENTRIES = 10_000; // tune as needed

    //The HashMap that will store the cents --> Price mapping
    private static final Map<Integer, Price> CACHE = new HashMap<>();

    /**
     * makePrice()
     *  - Checks if a Price object already exists for a given cents int passed in
     *  - Creates and/or Assigns the proper Price object reference fro that cents value
     *      - Think about looking into synchornized to potentially use to handle multi threading issues
     *      - Multiple traders could be attempting to create price objects at the same time
     * */
    public static Price makePrice(int value){

        //Using synchronized ensures that O=only one thread can read/write to the cache at a time
        //Locks on the object provided -->Prevents racing conditions:
            //Multiple threads try to modify the HashMap at the same time (both trying to put the same key)
        synchronized (CACHE) {

            // Check the cache for an existing Price object with this cents value.
            Price existing = CACHE.get(value);
            if (existing == null) {

                // If it doesn't exist -> Create a new Price object and store it.
                existing = new Price(value); // constructor is package-private -> it will use PriceFactory
                CACHE.put(value, existing);

                //If the creation pushes us over our threshold --> purge the Cache
                trimIfNeeded();
            }
            return existing;
        }
    }
    /**
     * trimIfNeeded()
     * - This method will purge the lowest-cents entries when the cache exceeds capacity.
     *      - Does only pull one entry at a time so not the most efficient, maybe change later?
     * */
    private static void trimIfNeeded() {

        // If our Cahce is too big, remove the lowest-cents entries until under capacity
        while (CACHE.size() > MAX_ENTRIES) {
            // Find the lowest cents key (HashMap has no ordering, so we scan once)
            Integer minKey = null;
            for (Integer k : CACHE.keySet()) {
                if (minKey == null || k < minKey) {
                    minKey = k;
                }
            }
            if (minKey == null) {
                break; // nothing to remove
            }
            CACHE.remove(minKey);
        }
    }


    //Convert string into cents
        //Get rid of dollar signs, commas, and decimals
        // Then make a price object
    public static Price makePrice(String stringValueIn) throws InvalidPriceException {
        //If String passed in is null --> thrwon exception
        if (stringValueIn == null) {
            throw new InvalidPriceException("Invalid price String value: " + stringValueIn +" (Price string cannot be null.)");
        }

        //Initialize a local String variable to hold the string as we work with it --> Trim it for any leading or trailing spaces
            //If empty string, throw exception
        String s = stringValueIn.trim();
        if (s.isEmpty()) throw new InvalidPriceException("Invalid price String value: " + stringValueIn + " (Price string cannot be empty.)");

        //Ensure all numeric Characters
            // Only allow digits, '$', '-', ',', and '.'; anything else is invalid
        if (!s.matches("[0-9$.,-]+")) {
            throw new InvalidPriceException("Invalid price String value: " + stringValueIn + " (Can't have non-numeric characters in the value)");
        }

        //Initialize a local boolean variable isNeg to false, used to keep track of the proper sign
        boolean isNeg = false;

        //Remove any "$" from the string, save it
        //Remove any "," from the string, save it
        s = s.replace("$", "").replace(",", "");

        // If the first character in the string is now a negative sign "-"
            //Change isNeg to be true
            //Remove the negative sign "-"
        if (!s.isEmpty() && s.charAt(0) == '-') {
            isNeg = true;
            s = s.substring(1);
        }

        //Split the string at any decimals, removing the decimals
        String[] parts = s.split("\\.");

        //If the result of the split returns 3 or more substrings, then throw my exception
        if (parts.length > 2) {
            throw new InvalidPriceException("Invalid price String value: " + stringValueIn + " (Can't have multiple decimal points.)");
        }

        // Normalize dollars and cents components
            //If there isn't anything in front the decimal then make it a zero representation of dollars
            //Otherwise set it equal to the first component of the split (dollars portion)
        String dollars = parts[0].isEmpty() ? "0" : parts[0];
            //If there isn't anything after the decimal then make it a zero representation of cents
            //Otherwise set it to be the second component of the split (cents portion)
        String centsPart = (parts.length == 2) ? parts[1] : "00";

        //If the last substring that results from the split is not length 2, then throw my exception
        if (parts.length == 2 && centsPart.length() != 2) {
            throw new InvalidPriceException("Invalid price String value: " + stringValueIn + " (Cents must be exactly two digits when a decimal is present.)");
        }

        //Re-join the split strings back together (removing the decimal point)
        String combined = dollars + centsPart;

        //Make and save an int from parsing that string
        int cents = Integer.parseInt(combined);

        //If it was deemed to be negative earlier, ensure that is handled
        if (isNeg == true){
            cents = -cents;
        }

        //Pass the newly created int from this string to the makePrice(int) method where the chacheing logic lives
            //Now all Price objects are made through that method
        return makePrice(cents);


    }

}
