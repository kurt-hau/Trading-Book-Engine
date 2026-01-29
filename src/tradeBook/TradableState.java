package tradeBook;

// No 'public' class TradableState -> package-private (only visible inside this package)
    //This is the DELEGATE class for creating Ordres and QuoteSide objects

import tradeBook.pricing.Price;

class TradableState implements Tradable {
    //Centralized delegate for managing shared Tradable state and logic
        //Passes the logic to Order or QuoteSide to create the object

//Make sure to throw only instances of the IllegalArgumentException class that I created
    //Pay close attention to which fields are final
    //Look at documentation for methods that do regex checking --> maybe use String.matches( regex ) ?

        // ------------------------------------------------------------
        // FIELDS
        // ------------------------------------------------------------

        /** The 3-letter user code identifying who placed the Tradable. */
        private final String user;

        /** The stock symbol for the Tradable. */
        private final String product;

        /** The price at which the user wants to buy or sell the stock. */
        private final Price price;

        /** Indicates whether this Tradable is a BUY or SELL Tradable. */
        private final BookSide side;

        /** The original quantity of the Tradable. */
        private final int originalVolume;

        /** The remaining volume that has not yet been filled or canceled. */
        private int remainingVolume;

        /** The total volume that has been canceled. */
        private int cancelledVolume;

        /** The total volume that has been filled through trading. */
        private int filledVolume;

        /** The unique identifier for this Tradable. */
        private final String id;


        // ------------------------------------------------------------
        // CONSTRUCTOR
        // ------------------------------------------------------------
        public TradableState(String user, String product, Price price, BookSide side, int originalVolume) throws IllegalArgumentException {

            //Immutable fields initialized with their setters
            this.user = setUser(user);
            this.product = setProduct(product);
            this.price = setPrice(price);
            this.side = setSide(side);
            this.originalVolume = setOriginalVolume(originalVolume);

            // Initialize mutable fields using their own set() methods (Internal)
                //Cancelled and filled can default to zero at the start
            this.remainingVolume = setRemainingVolumeInternal(originalVolume);
            this.cancelledVolume = setCancelledVolumeInternal(0);
            this.filledVolume = setFilledVolumeInternal(0);

            // Generate unique Tradable ID
                //System nano time doesn't need to be coupled to the object
            this.id = this.user + this.product + this.price.toString() + System.nanoTime();
        }


        // ------------------------------------------------------------
        // Private MODIFIER METHODS set()
        // ------------------------------------------------------------
        /** Sets and validates the user code (must be exactly 3 letters). */
        private String setUser(String user) throws IllegalArgumentException {
            if (user == null || !user.matches("[A-Za-z]{3}")) {
                throw new IllegalArgumentException("User code must be exactly 3 letters (A–Z only).");
            }
            return user;
        }

        /** Sets and validates the product symbol. */
        private String setProduct(String product) throws IllegalArgumentException {
            // 1-5 letters/digits, no period
            // OR
            // 1–4 letters/digits, then one literal dot (\\.), then one letter/digit
            if (product == null || !product.matches("[A-Za-z0-9]{1,5}|[A-Za-z0-9]{1,4}\\.[A-Za-z0-9]")) {
                throw new IllegalArgumentException("Product symbol must be 1–5 chars (letters/numbers, may include one '.')");
            }
            return product;
        }

        /** Sets and validates the Price object. */
        private Price setPrice(Price price) throws IllegalArgumentException {
            //Just checking for if an object has a null reference → == will suffice
            //Only need to use .equals() if I am checking object to object equality
            if (price == null) {
                throw new IllegalArgumentException("Price cannot be null.");
            }
            return price;
        }

        /** Sets and validates the BookSide (BUY or SELL). */
        private BookSide setSide(BookSide side) throws IllegalArgumentException {
            //Just checking for if an object has a null reference → == will suffice
            //Only need to use .equals() if I am checking object to object equality
            if (side == null) {
                throw new IllegalArgumentException("BookSide cannot be null.");
            }
            return side;
        }

        /** Sets and validates the original volume (must be: 0< x <10,000). */
        private int setOriginalVolume(int vol) throws IllegalArgumentException {
            if (vol <= 0 || vol >= 10000) {
                throw new IllegalArgumentException("Original volume must be > 0 and < 10,000.");
            }
            return vol;
        }

        /** Sets the remaining volume for this Tradable (must be >= 0). */
        private int setRemainingVolumeInternal(int newVol) throws IllegalArgumentException {
            if (newVol < 0) {
                throw new IllegalArgumentException("Remaining volume cannot be negative.");
            }
            return newVol;
        }

        /** Sets the cancelled volume for this Tradable (must be >= 0). */
        private int setCancelledVolumeInternal(int newVol) throws IllegalArgumentException {
            if (newVol < 0) {
                throw new IllegalArgumentException("Cancelled volume cannot be negative.");
            }
            return newVol;
        }

        /** Sets the filled volume for this Tradable (must be >= 0). */
        private int setFilledVolumeInternal(int newVol) throws IllegalArgumentException {
            if (newVol < 0) {
                throw new IllegalArgumentException("Filled volume cannot be negative.");
            }
            return newVol;
        }

        // ------------------------------------------------------------
        //  Public Interface ACCESSOR (GET) METHODS
        // ------------------------------------------------------------

        /**
         * The getters for all of these fields, should return
         * All fields are immutable or primitive type so we can return direct references to them
         *  "@Override" signals that these fulfill the requirements of the interface methods as well and are public
         */

        @Override
        public String getUser() {
            return user;
        }

        @Override
        public String getProduct() {
            return product;
        }

        @Override
        public Price getPrice() {
            return price;
        }

        @Override
        public BookSide getSide() {
            return side;
        }

        @Override
        public int getOriginalVolume() {
            return originalVolume;
        }

        @Override
        public int getRemainingVolume() {
            return remainingVolume;
        }

        @Override
        public int getCancelledVolume() {
            return cancelledVolume;
        }

        @Override
        public int getFilledVolume() {
            return filledVolume;
        }

        @Override
        public String getTradableId() {
            return id;
        }

        // ------------------------------------------------------------
        //                Public INTERFACE set() METHODS
        // ------------------------------------------------------------
        /**
         * Need to have public methods here that call my internal private set() methods
         * For each setter that the Tradable interface requires, I should make a public method (with the @Override annotation)
         *      that calls my private setter for validation and assignment.
         * Don't need to worry about the get() methods, as they are already public
         */

        /**
         * Sets the cancelled volume for this Tradable --> It delegates to the private setCancelledVolume() method for validation.
         */
        @Override
        public void setCancelledVolume(int newVol) throws IllegalArgumentException {
            this.cancelledVolume = setCancelledVolumeInternal(newVol); // private method can throw exceptions
        }

        /**
         * Sets the remaining volume for this Tradable --> It delegates to the private setRemainingVolume() method for validation.
         */
        @Override
        public void setRemainingVolume(int newVol) throws IllegalArgumentException {
            this.remainingVolume = setRemainingVolumeInternal(newVol); // private method can throw exceptions
        }

        /**
         * Sets the filled volume for this Tradable. --> It delegates to the private setFilledVolume() method for validation.
         */
        @Override
        public void setFilledVolume(int newVol) throws IllegalArgumentException {
            this.filledVolume = setFilledVolumeInternal(newVol); // private method can throw exceptions
        }

        /**
         * Generates and returns a TradableDTO object for this Tradable.
         * The DTO (Data Transfer Object) is a lightweight copy of this Tradable’s data
         * used to safely share information without exposing the actual Tradable object.
         *
         * @return a TradableDTO containing a snapshot of this Tradable's current state.
         */
        @Override
        public TradableDTO makeTradableDTO() {
            return new TradableDTO(
                    user,
                    product,
                    price,
                    originalVolume,
                    remainingVolume,
                    cancelledVolume,
                    filledVolume,
                    side,
                    id
            );
        }
}


