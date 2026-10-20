# CinePass Cinemas — Online Movie Ticket Booking System

A desktop cinema ticket booking application built in **Java 21** using **JavaFX**, **SQLite (JDBC)**, **Object Serialization**, and **Multithreading**.

---

## 🎬 Project Overview

**CinePass Cinemas** is an end-to-end cinema ticket reservation platform designed to demonstrate foundational and advanced Java software engineering principles. The application allows patrons to browse movies, inspect theatres and screening times, choose individual or multiple seats using a visual 5×8 seating matrix, complete simulated payments, generate formatted ticket vouchers, and manage booking history with atomic cancellation and seat release.

---

## 🎯 Academic Module Mapping

| Module | Core Java Concept | Concrete Implementation & Source Location |
| :--- | :--- | :--- |
| **Module 1: Fundamentals** | **Two-Dimensional Arrays** | `Showtime.java` maintains `boolean[][] seatGrid` (true = booked, false = available) and `Seat[][] seatMatrix` for the 5-row × 8-column layout. |
| | **Enhanced For Loops** | Traversal over 2D seating arrays in `Showtime.getAvailableSeatCount()` and iterating movie/showtime collections. |
| | **String Processing** | Case-insensitive regex validation in `ValidationUtils.java`, formatted IDs (`BK101`), and text parsing. |
| | **Control Flow & Switches** | Modern Java switch expressions in `PriceCalculator.java` for polymorphic ticket creation. |
| **Module 2: OOP Principles** | **Encapsulation** | Private member variables, getters, and setters in `Movie`, `Theater`, `Showtime`, `Seat`, `Booking`, `Ticket`. |
| | **Inheritance & Abstraction** | `abstract class Ticket` extended by `RegularTicket`, `PremiumTicket`, and `IMAXTicket`. |
| | **Runtime Polymorphism** | Dynamic dispatch on `Ticket.calculatePrice()` overridden across each ticket category. |
| | **Interfaces** | `Payable` interface implemented by `SimulatedPayment` with `pay(double)` and `refund(double)`. |
| | **Nested Classes** | `Showtime.ShowtimeSeat` nested domain class inside `Showtime.java`. |
| | **Static ID Generator** | `BookingIdGenerator.java` utilizing atomic counters synced to SQLite max ID. |
| **Module 3: Exceptions & Concurrency** | **Custom Exceptions** | Checked exception `SeatAlreadyBookedException`, plus `BookingException` and `InvalidInputException`. |
| | **Thread Synchronization** | Fine-grained `ReentrantLock` per showtime ID in `BookingService.java`. |
| | **Double-Booking Prevention** | Atomic SQLite transactions (`setAutoCommit(false)`, `BEGIN`, `COMMIT`, `ROLLBACK`) and `UNIQUE(showtime_id, seat_code)` constraint. |
| | **Race Condition Simulation** | `ConcurrencyDemoService.java` with `CountDownLatch` coordinating 2 simultaneous threads competing for seat C4. |
| **Module 4: Files, Streams & Collections** | **Object Serialization** | `SerializationService.java` exporting/importing domain object graphs to `exports/bookings.dat` via `ObjectOutputStream`/`ObjectInputStream`. |
| | **Text File I/O** | `TicketFileService.java` generating readable cinema ticket vouchers (`exports/ticket_BK101.txt`) via `BufferedWriter`/`FileWriter`. |
| | **Java Collections** | `ArrayList` (movies/bookings), `HashMap` (ID lookups), `HashSet` (unique seat selections). |
| | **Streams & Lambdas** | Stream filtering by genre (`filterByGenre()`), title search (`searchMovies()`), and `Comparator` sorting. |
| **Module 5: JavaFX GUI & JDBC** | **JavaFX UI Design** | Responsive desktop interface with cinema color palette (`#0B0F17`, `#F59E0B`), `GridPane`, `TableView`, and Canvas poster art. |
| | **SQLite Database** | Embedded local database (`data/movieticket.db`) managed via JDBC `Connection`, `PreparedStatement`, `ResultSet`. |
| | **Data Consistency** | Database acts as the single source of truth; collections cache display state and refresh after mutations. |

---

## 🏛️ Project Directory Structure

```text
java_project/
├── pom.xml                               # Maven project descriptor & dependencies
├── mvnw.cmd                              # Maven wrapper configured for Java 21
├── run.bat                               # Double-click launcher to start JavaFX GUI
├── test.bat                              # Double-click runner for JUnit 5 test suite
├── README.md                             # Comprehensive project documentation
├── .gitignore                            # Git ignore configuration
├── data/
│   └── movieticket.db                    # Authoritative SQLite database
├── exports/
│   ├── bookings.dat                      # Binary serialized booking archive
│   └── ticket_BK101.txt                  # Generated ticket text file
└── src/
    ├── main/
    │   ├── java/com/movieticket/
    │   │   ├── MainApp.java              # JavaFX application entrypoint
    │   │   ├── model/
    │   │   │   ├── Movie.java            # Movie entity (Comparable, Serializable)
    │   │   │   ├── Theater.java          # Theater entity
    │   │   │   ├── Showtime.java         # Showtime entity with 2D Seat Array
    │   │   │   ├── Seat.java             # Seat model
    │   │   │   ├── Ticket.java           # Abstract base Ticket class
    │   │   │   ├── RegularTicket.java    # Concrete Regular Ticket
    │   │   │   ├── PremiumTicket.java    # Concrete Premium Ticket (1.5x)
    │   │   │   ├── IMAXTicket.java       # Concrete IMAX Ticket (Base + 250)
    │   │   │   ├── TicketCategory.java   # Ticket category enum
    │   │   │   ├── Booking.java          # Booking transaction model
    │   │   │   └── BookingStatus.java    # CONFIRMED / CANCELLED / PENDING
    │   │   ├── payment/
    │   │   │   ├── Payable.java          # Payment interface
    │   │   │   └── SimulatedPayment.java # Simulated payment gateway
    │   │   ├── exception/
    │   │   │   ├── SeatAlreadyBookedException.java
    │   │   │   ├── BookingException.java
    │   │   │   └── InvalidInputException.java
    │   │   ├── db/
    │   │   │   ├── DatabaseManager.java  # Connection manager (PRAGMA foreign keys)
    │   │   │   └── SchemaInitializer.java# DDL schema setup & seed data
    │   │   ├── service/
    │   │   │   ├── MovieService.java     # Movie & showtime queries (Streams)
    │   │   │   ├── BookingService.java   # Atomic booking & cancellation
    │   │   │   ├── TicketFileService.java# BufferedWriter ticket file generator
    │   │   │   ├── SerializationService.java# ObjectOutputStream binary archive
    │   │   │   └── ConcurrencyDemoService.java# Thread contention simulation
    │   │   ├── util/
    │   │   │   ├── BookingIdGenerator.java# Monotonic persistent ID generator
    │   │   │   ├── PriceCalculator.java  # Switch expressions & pricing logic
    │   │   │   └── ValidationUtils.java  # Regex input validations
    │   │   └── ui/
    │   │       ├── CinemaAppUI.java      # Master navigation container
    │   │       ├── UITheme.java          # Theme constants & Canvas graphics
    │   │       ├── HomeScreen.java       # Movie catalogue & filters
    │   │       ├── ShowtimeSelectionScreen.java # Screening & tier selection
    │   │       ├── SeatSelectionScreen.java     # Visual 5x8 seat selection grid
    │   │       ├── BookingConfirmationScreen.java # Summary & simulated payment
    │   │       ├── TicketScreen.java     # Ticket stub view & voucher export
    │   │       ├── MyBookingsScreen.java # TableView & booking cancellation
    │   │       ├── ConcurrencyDemoScreen.java   # Real-time multithreading console
    │   │       ├── DataBackupScreen.java # Serialization export & inspector
    │   │       └── HelpScreen.java       # Academic reference guide
    │   └── resources/
    │       └── css/
    │           └── styles.css            # Dark cinema-themed styling
    └── test/
        └── java/com/movieticket/
            ├── TicketPriceTest.java      # Price calculations & polymorphism
            ├── BookingServiceTest.java   # Booking, rollback, cancellation, validation
            ├── DatabasePersistenceTest.java# Persistence across restarts
            ├── ConcurrencyBookingTest.java # Contested thread race condition test
            └── SerializationAndFileTest.java # .txt export & .dat serialization
```

---

## 🛠️ Prerequisites & Environment Setup

- **Java Development Kit:** JDK 21 LTS (or JDK 17+).
- **Build Tool:** Maven (or the included portable `mvnw.cmd` wrapper).
- **Operating System:** Windows, macOS, or Linux.

---

## 🚀 How to Run the Application

### Option A: Using the double-click batch file (Windows)
Double-click `run.bat` in the project root.

### Option B: Using the Maven Wrapper in PowerShell or Command Prompt
```powershell
.\mvnw.cmd javafx:run
```

---

## 🧪 How to Execute Unit Tests

All 16 test cases can be executed via JUnit 5:

### Option A: Using the double-click batch file (Windows)
Double-click `test.bat` in the project root.

### Option B: Using the Maven Wrapper
```powershell
.\mvnw.cmd test
```

### Test Suite Summary:
1. `TicketPriceTest`: Verifies pricing formulas for Regular, Premium, and IMAX categories. Verifies the demo scenario where 2 Regular seats for *Inception* total **₹600**.
2. `BookingServiceTest`: Tests single-seat reservation, duplicate booking rejection (`SeatAlreadyBookedException`), multi-seat atomic booking, transaction rollback on conflict, booking cancellation with seat release, and customer input validation.
3. `DatabasePersistenceTest`: Verifies that bookings, seat states, and monotonic booking IDs (`BK101`, `BK102`...) persist across simulated application restarts.
4. `ConcurrencyBookingTest`: Simulates two concurrent threads released by a `CountDownLatch` attempting to book seat **C4** at the exact same millisecond. Exactly one succeeds and the other receives `SeatAlreadyBookedException`.
5. `SerializationAndFileTest`: Validates text ticket generation (`exports/ticket_BK101.txt`) and binary object serialization round-trip (`exports/bookings.dat`).

---

## 🎟️ Demonstration Scenario Walkthrough

To reproduce the exact academic presentation scenario:
1. Launch the application with `run.bat` or `.\mvnw.cmd javafx:run`.
2. On the **Movies** screen, select **Inception** and click **Book Tickets**.
3. Choose theatre **IMAX City**, showtime **07:30 PM**, and category **Regular Seating**. Click **Proceed to Seat Selection**.
4. In the visual 5×8 seating grid:
   - Click seat **C4** (turns yellow / selected).
   - Click seat **C5** (turns yellow / selected).
   - Notice the live sidebar calculates: **2 Seats × ₹300 = ₹600**.
   - Customer details default to **Priya**, `priya@cinepass.com`, `9876543210`.
5. Click **Proceed to Confirmation →**.
6. Review the booking summary and click **Pay ₹600 & Confirm Booking ✓**.
7. The ticket voucher screen displays:
   ```text
   == MOVIE TICKET ==
   ID: BK101
   Movie: Inception
   Theater: IMAX City
   Time: 07:30 PM
   Seats: C4, C5
   Amount: Rs 600
   Status: CONFIRMED
   ```
8. Click **📄 Export Ticket to .TXT** to generate `exports/ticket_BK101.txt`.
9. Navigate to **🎟️ My Bookings** to view the table record or test booking cancellation (which automatically frees seats C4 and C5 in the database).
10. Navigate to **⚡ Concurrency Test** to see live multi-threaded race condition prevention in action.
11. Navigate to **💾 Backup & Export** to test binary object serialization into `exports/bookings.dat`.
