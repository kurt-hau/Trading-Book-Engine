package tradeBook;

//For the Product --> CMO ArrayList
import java.util.HashMap;

//For the CMO ArrayList
import java.util.ArrayList;

/**
 * This class is a Singleton that uses the Observer pattern.
 *  - It maintains a list of observers, and filters (what observers want the Current Market for what stocks).
 *  - It also publishes the Current Market updates to the subscribed “observers”.
 */
public class CurrentMarketPublisher {

    // ------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------

    //Hashmap named filters for storing the CMO observers
    //Can instantiate the HashMap here as well
        //Key: Product code (TGT)
        //Value: ArrayList of CMO objects (Observers)
    private HashMap<String , ArrayList<CurrentMarketObserver>> publishers = new HashMap<>();


    //private class-owned instance variable for the singleton
    private static CurrentMarketPublisher instance;

    // ------------------------------------------------------------
    // CONSTRUCTOR
    // ------------------------------------------------------------
    /**
     * private class-owned constructor to only be called to by the getInstance() method
     */
    private CurrentMarketPublisher(){
        //Will be called by getInstance() to ensure we are only grabbing the single instance of this variable
    }


    // ------------------------------------------------------------
    // SINGLETON METHOD
    // ------------------------------------------------------------
    /**
     * Returns the single CurrentMarketPublisher instance.
     *  - public Singleton getInstance() method
     *  - Creates the instance the first time it is requested (acts as the constructor)
     *
     * @return The singleton CurrentMarketPublisher.
     */
    public static CurrentMarketPublisher getInstance(){
        //Only create if this is the first object of this class type to have been created
        if (instance == null){
            instance = new CurrentMarketPublisher();
        }

        return instance;
    }


    // ------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------

  /**
   * Subscribes a CurrentMarketObserver to receive Current Market updates for the specified stock symbol.
   *    - If no observers are currently registered for this symbol, a new list should be created and associated with the symbol.
   *
   * @param symbol The stock symbol that the observer wishes to receive updates for.
   * @param cmo    The CurrentMarketObserver to subscribe.
   */
  public void subscribeCurrentMarket(String symbol, CurrentMarketObserver cmo) {

      //Search in the HashMap for the symbol that matches what was passed in
      ArrayList<CurrentMarketObserver> observerList = publishers.get(symbol);

      //If no such symbol exists
      if (observerList == null) {
          //Create a new slot in the hashmap with that symbol as the key and a new, empty ArrayList that is the value for that symbol
          observerList = new ArrayList<>();
          publishers.put(symbol, observerList);
      }
      //If a symbol does exist
      //Simply Add the cmo object passed in to that ArrayList, at then end is fine
      observerList.add(cmo);

      //return
  }

  /**
   * Unsubscribes a CurrentMarketObserver from receiving Current Market updates for the specified stock symbol.
   *    - If the symbol has no registered observers, this method should simply return.
   *
   * @param symbol The stock symbol that the observer no longer wishes to receive updates for.
   * @param cmo    The CurrentMarketObserver to unsubscribe.
   */
  public void unSubscribeCurrentMarket(String symbol, CurrentMarketObserver cmo) {
      //Access the observers list for the symbol passed in, save in a variable
      ArrayList<CurrentMarketObserver> observerList = publishers.get(symbol);

      //If there is no such symbol, return
      if (observerList == null) {
          return;
      }

      //If the symbol has an empty list, return
      if (observerList.isEmpty()) {
          return;
      }

      //Search the list for the CMO object passed in
        //If the list contains the CMO object, remove it
      observerList.remove(cmo);
  }

  /**
   * Accepts a new Current Market snapshot (DTO) for the specified stock symbol
   *    and publishes it to all subscribed CurrentMarketObserver objects for that symbol.
   *
   *    - This method is called by the CurrentMarketTracker.
   *
   * @param symbol   The stock symbol whose Current Market has been updated.
   * @param buySide  The top-of-book buy side CurrentMarketSide.
   * @param sellSide The top-of-book sell side CurrentMarketSide.
   */
  public void acceptCurrentMarket(String symbol, CurrentMarketSide buySide, CurrentMarketSide sellSide) {

      //Access the ArrayList of observers list for the symbol passed in, save in a variable
      ArrayList<CurrentMarketObserver> observerList = publishers.get(symbol);
      if (observerList == null) {
          //If symbol is not a valid key, there are no observers to notify
          return;
      }

      //Iterate over the CMO objects in that List
      for (CurrentMarketObserver cmo : observerList) {

          //Call the CMOs' updateCurrentMarket() method  --> Giving the symbol and the BUY and SELL side CMS objects
          cmo.updateCurrentMarket(symbol, buySide, sellSide);
      }

  }
}
