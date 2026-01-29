# Trading-Book-Engine
Polymorphic, interface-driven Java stock exchange simulator with per-product order books, pricing, users, trade execution, and real-time top-of-book market updates. Uses Factory/Flyweight, Singleton, Facade, DTO, and Observer patterns. Developed and tested in IntelliJ IDEA.

## Design Patterns Used
- **Factory Pattern (`PriceFactory`)**: Centralizes creation of `Price` objects (from cents or strings) and enforces validation.
- **Flyweight Pattern (`PriceFactory`)**: Caches/reuses immutable `Price` instances by value to reduce memory and improve consistency.
- **Singleton Pattern**: Ensures one shared instance for system coordinators (e.g., `UserManager`, `ProductManager`, market publisher/tracker).
- **Observer Pattern**: Publishes current market (top-of-book) updates to subscribed users.
- **DTO Pattern (`TradableDTO`)**: Transfers tradable state safely without exposing internal object details.
- **Facade Pattern (Manager layer)**: Managers provide a simplified API over the underlying trading/book subsystems.

---

## What this project does

### 1) Price model + factory
- Implements an immutable `Price` value object stored in **integer cents** for precision.
- Supports comparison/arithmetic and string formatting (e.g., `$12.34`).
- `PriceFactory` constructs prices from validated inputs and (via Flyweight) reuses instances.

### 2) Order book per product
- Maintains a `ProductBook` per symbol with **BUY** and **SELL** sides.
- Uses price-ordered structures (e.g., `TreeMap`) to manage levels and volumes.
- Matches trades using top-of-book logic and updates tradable states as fills occur.

### 3) Users + system management
- Tracks users and their submitted tradables (orders/quotes) via DTOs.
- `UserManager` handles user creation/lookup and tradable state updates.
- `ProductManager` routes actions to the correct `ProductBook` and coordinates system-wide behavior.

### 4) Current Market publishing
- Computes and formats top-of-book market data (price + volume) for each side.
- `CurrentMarketTracker` produces market snapshots and triggers publishing.
- `CurrentMarketPublisher` notifies subscribed users via the Observer pattern.

---

## Project structure
- `src/` — Java source code organized by package (e.g., `tradeBook/`, `pricing/`).
- `docs/` — Assignment PDFs / documentation (optional).

---

## Running
- Run `Main` to execute the provided scenarios and print console output.

---

## Notes
- Build/IDE artifacts (e.g., `out/`, `.idea/`, `*.iml`) should be ignored via `.gitignore`.
- Prices are represented in cents to avoid floating point rounding issues.
- Current Market output reflects **top-of-book** only (best bid/ask and volume at that price).
