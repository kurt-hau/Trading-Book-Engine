package tradeBook;

//Need TreeMaps for ordered sorting and searhc of the User IDs

import java.util.TreeMap;

// System-wide registry of Users
//  - Façade: external code never pokes into individual User maps; it talks to UserManager, which forwards to the right User.
//	- Singleton: exactly one UserManager exists so there’s one source of truth for users and their tradables.
//      - it’s a router/registry. Validation + delegation only here

/**
 * UserManager class
 * - Maintains a collection of all users in the system – it acts as a Façade to the Users
 *      - Domain specific methods that ususally just forward calls to the Class's method
 * - Needs to be a singleton so we can ensure that all Users are kept and updated in the same place
 * - Has a TREEMAP of three-letter UserIDs (key) that maps to a specific User Object (value)
 *      - Ensures the IDs are in sorted order
 * - Has an updateTradable() method that sends the updated trade information to the proper user
 *      - which forwards it to update() its DTO for the specific tradable that was modified
 */
public final class UserManager {

    // ---------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------

        //TreeMap to store the User IDs (Key is ID String, Value is a User object)
        private final TreeMap<String, User> users;

        // Singleton instance to ensure exactly one UserManager exists
            // Static means its shared across all the instances
            // (Not final — because we assign it later when first accessed)
        private static UserManager INSTANCE;

    // ---------------------------------------------------------------------
    // Constructor + Singleton Accessor Method
    // ---------------------------------------------------------------------

        //Initializes the TreeMap, should be empty at the start?
            //All method called will do some sort of action on this TreeMap

        // Private constructor: initialize the TreeMap; start empty
        private UserManager() {
            users = new TreeMap<>();
        }

    /**
     * Singleton Accessor
     * - Only create an instance if we have never created one before
     * - UserManager.getInstance() is the only legal way to access UserManager from client code
     */
        public static UserManager getInstance() {
            if (INSTANCE == null){
                // First time we call this, instance doesn’t exist yet
                // So then we can make a call to the constructor only the first time
                INSTANCE = new UserManager();
            }
            return INSTANCE;
        }

    // ---------------------------------------------------------------------
    // Methods
    // ---------------------------------------------------------------------

        //No private setters needed I believe, no other fields other than the treemap?

    /**
     * void init(String[] usersIn)
     * - Create a new User object for each userId in the String array passed in.
     * - Each User object should be added to the UserManager’s TreeMap of users.
     * - If the String passed in is null, throw a DataValidationException.
     */
        //Ensure the list is not empty or null --> Illegal parameter exception
        //Iterate through the list of Strings making a call to the User class's constructor to create a User object for each string in the list
            // If you come across a string that is null throw a DataValidationException
        // Don't return anything

    public void init(String[] usersIn) throws DataValidationException, IllegalArgumentException {
        // Ensure the list is not empty
        if (usersIn == null) {
            throw new DataValidationException("User list cannot be null");
        }

        // Create a User for each tradableID and store in the TreeMap
            //Iterate through the list of Strings
        for (String rawId : usersIn) {
            //If the string is null, throw exception
            if (rawId == null) {
                throw new DataValidationException("User tradableID in list cannot be null");
            }
            // Make a call to the User class's constructor to create a User object for each string in the list
                // User class will validate formatting (3 letters) and normalize case

            // Ensure all strings used as keys are in upper case and are trimmed
            String key = rawId.trim().toUpperCase();
            users.put(key, new User(rawId));    // If a duplicate tradableID (key) appears, the new one will overwrite the previous (simplest policy)
        }
    }

    /**
     * void updateTradable(String userId, TradableDTO o)
     * - This method should add/replace a TradableDTO for the specified User by calling the User’s “updateTradable” method.
     *   - If the userId is null, throw a DataValidationException.
     *   - If the TradableDTO is null, throw a DataValidationException.
     *   - If the user does not exist, throw a DataValidationException
     */
    public void updateTradable(String userId, TradableDTO o) throws DataValidationException {
        //Ensure the ID is not null
        if (userId == null) {
            throw new DataValidationException("userId cannot be null");
        }
        //Ensure the DTO is not null
        if (o == null) {
            throw new DataValidationException("TradableDTO cannot be null");
        }

        String key = userId.trim().toUpperCase();    // Normalize the user tradableID the same way User does (trim + uppercase)
        User u = users.get(key);                     // get the User object for that user

        //Ensure the user exists
        if (u == null) {
            throw new DataValidationException("User does not exist: " + userId);
        }

        // Delegate a call to the User Class's updateTradable() method for the tradableDTO passed in
        u.updateTradable(o);
    }

    /**
     * public User getUser(String userId)
     *  - return the User object that has the user ID specified in the method parameter
     *  - Throw an exception (an application-defined exception) if the user does not exist.
     */
    public User getUser(String userId) throws UserNotFoundException, IllegalArgumentException {
        // Validate the incoming userId
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or blank.");
        }

        // Normalize the key the same way I do in the constructor and updateTradable()
        String key = userId.trim().toUpperCase();

        // Look up the User in the TreeMap of Users
        User person = users.get(key);

        // If the user is not in the registry, throw the new exception
        if (person == null) {
            throw new UserNotFoundException("User not found: " + userId);
        }
        // Return the existing User object from the registry
        return person;
    }

    /**
     * Override the toString() to match the PDF's example output
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        // Header to match expected output format
        // sb.append("User Tradables:\n");
        sb.append("\n");

        // TreeMap guarantees sorted order by userId
            // Iterate through the User objects creating a toString() for each one as you go
        for (User u : users.values()) {
            sb.append(u.toString());
            sb.append("\n"); // blank line between user blocks
        }

        // Trim trailing whitespace/newlines to avoid extra line at the very end
        return sb.toString();
    }


}
