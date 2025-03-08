import java.sql.*;
import java.util.*;

// Flight class with Encapsulation
class Flight {
    private String flightNumber;
    private String airline;
    private int availableSeats;
    private double price;

    public Flight(String flightNumber, String airline, int availableSeats, double price) {
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.availableSeats = availableSeats;
        this.price = price;
    }

    public String getFlightNumber() { return flightNumber; }
    public String getAirline() { return airline; }
    public int getAvailableSeats() { return availableSeats; }
    public double getPrice() { return price; }

    public boolean bookSeat() {
        if (availableSeats > 0) {
            availableSeats--;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Flight: " + flightNumber + ", Airline: " + airline + ", Seats Available: " + availableSeats + ", Price: " + price;
    }
}

// Passenger class using Inheritance
class Passenger {
    private String name;
    private String passportNumber;

    public Passenger(String name, String passportNumber) {
        this.name = name;
        this.passportNumber = passportNumber;
    }

    public String getName() { return name; }
    public String getPassportNumber() { return passportNumber; }
}

// Booking class using Polymorphism
class Booking {
    private Passenger passenger;
    private Flight flight;

    public Booking(Passenger passenger, Flight flight) {
        this.passenger = passenger;
        this.flight = flight;
    }

    @Override
    public String toString() {
        return "Booking Confirmed: " + passenger.getName() + " | Flight: " + flight.getFlightNumber();
    }
}

// Database Connection Class
class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/airline";
    private static final String USER = "root";
    private static final String PASSWORD = "password";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

// Main System
public class AirlineReservationSystem {
    private static List<Flight> flights = new ArrayList<>();
    private static List<Booking> bookings = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        loadFlightsFromDatabase();

        System.out.println("Welcome to the Airline Reservation System!");
        while (true) {
            System.out.println("1. View Flights\n2. Book Flight\n3. Exit");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    for (Flight flight : flights) {
                        System.out.println(flight);
                    }
                    break;
                case 2:
                    System.out.println("Enter your name:");
                    String name = scanner.next();
                    System.out.println("Enter passport number:");
                    String passport = scanner.next();
                    System.out.println("Enter flight number to book:");
                    String flightNumber = scanner.next();

                    Flight selectedFlight = null;
                    for (Flight flight : flights) {
                        if (flight.getFlightNumber().equals(flightNumber)) {
                            selectedFlight = flight;
                            break;
                        }
                    }

                    if (selectedFlight != null && selectedFlight.bookSeat()) {
                        Passenger passenger = new Passenger(name, passport);
                        Booking booking = new Booking(passenger, selectedFlight);
                        bookings.add(booking);
                        saveBookingToDatabase(passenger, selectedFlight);
                        System.out.println("Booking successful! " + booking);
                    } else {
                        System.out.println("Booking failed! No available seats or invalid flight number.");
                    }
                    break;
                case 3:
                    System.out.println("Thank you for using the system!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void loadFlightsFromDatabase() {
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM flights")) {
            while (rs.next()) {
                flights.add(new Flight(
                    rs.getString("flight_number"),
                    rs.getString("airline"),
                    rs.getInt("available_seats"),
                    rs.getDouble("price")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void saveBookingToDatabase(Passenger passenger, Flight flight) {
        String query = "INSERT INTO bookings (name, passport, flight_number) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, passenger.getName());
            pstmt.setString(2, passenger.getPassportNumber());
            pstmt.setString(3, flight.getFlightNumber());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
