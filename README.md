Smart Parking System (Java Swing)
Description

The Smart Parking System is a Java-based desktop application developed using Java Swing for the user interface and PostgreSQL as the backend database. This system is designed to help users find, book, and pay for parking spaces in real time. Admin users can manage parking spaces, reservations, and monitor the system’s performance. The system reduces traffic congestion, improves parking space management, and enhances the overall user experience.

Key Features

Real-time Parking Availability: Displays available parking spaces in real-time.

User Registration & Authentication: Users can register, log in, and view their booking history.

Parking Space Reservation: Users can reserve parking spots for a specific duration.

Payment Integration: Users can pay for parking reservations through an integrated payment system.

Admin Dashboard: Admins can manage parking spaces, view reservations, and generate reports.

Notifications: Notifications to users regarding reservation updates and availability.

Installation
Prerequisites

To run the Smart Parking System, you’ll need the following:

Java Development Kit (JDK) (Version 11 or higher)

PostgreSQL Database (or another relational database)

Maven (for dependency management)

IDE like IntelliJ IDEA, Eclipse, or NetBeans for Java development

Steps to Install

Clone the Repository

git clone https://github.com/yourusername/smart-parking-system.git
cd smart-parking-system


Install Java Dependencies

The project uses Maven for managing dependencies. To install them, run:

mvn install


Set Up PostgreSQL Database

Create a new PostgreSQL database, e.g., smart_parking.

Update the db.properties file with your database credentials (username, password, URL):

Example in db.properties:

db.url=jdbc:postgresql://localhost:5432/smart_parking
db.username=yourusername
db.password=yourpassword


Create Database Tables

Run the SQL script located in the src/main/resources/db/schema.sql file to create the required tables.

Example command:

psql -U yourusername -d smart_parking -f src/main/resources/db/schema.sql


Run the Application

To run the application locally, execute the following Maven command:

mvn clean install
mvn exec:java


The application will launch as a desktop GUI application.

Access the Application

The Java Swing interface will allow users to interact with the system for making reservations, payments, and managing parking spaces.

Usage
For Users:

Sign Up / Log In: Create an account or log in to an existing account.

Search Parking Spaces: View available parking spots based on location and availability.

Reserve a Spot: Choose a parking spot and make a reservation.

Payment: Pay for the parking space through the integrated payment gateway (e.g., PayPal, Stripe).

View Reservations: Check past and current parking reservations.

For Admins:

Admin Dashboard: Admins can manage parking spaces and reservations.

Manage Parking Spaces: Add, remove, or update parking spots in the system.

View Reservation Data: See all active and completed reservations.

Generate Reports: Track occupancy rates and generate reports for analysis.

Technologies Used

Java Swing (for building the desktop GUI)

PostgreSQL (for the relational database)

Maven (for project dependency management)

JDBC (for database connectivity)

JavaFX (optional for more advanced UI features)

Payment Gateway Integration (e.g., PayPal or Stripe API for processing payments)

Database Schema

The system uses the following tables:

users: Stores user information such as name, email, and password.

parking_spaces: Stores information about parking spaces, such as location, price, and availability.

reservations: Tracks user reservations, including parking spot, reservation time, and payment status.

payments: Stores payment transactions and related details.

Running Tests

To run tests for this application, use the following Maven command:

mvn test

Contributing

If you’d like to contribute to this project, feel free to fork the repository and submit a pull request. Contributions are always welcome!

Steps to Contribute:

Fork the repository.

Create a new feature branch (git checkout -b feature/your-feature-name).

Commit your changes (git commit -am 'Add new feature').

Push to the branch (git push origin feature/your-feature-name).

Create a pull request.
