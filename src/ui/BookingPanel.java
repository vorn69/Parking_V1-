package ui;

import dao.BookingDAO;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import models.Booking;
import models.ParkingSlot;
import models.User;
import models.Vehicle;

public class BookingPanel extends JPanel {

    private JTable bookingTable;
    private BookingDAO bookingDAO;
    private List<Booking> bookingList;

    public BookingPanel() {
        bookingDAO = new BookingDAO();
        bookingList = new ArrayList<>();
        setLayout(new BorderLayout());
        bookingTable = new JTable();
        add(new JScrollPane(bookingTable), BorderLayout.CENTER);
        loadBookings();
    }

    private void loadBookings() {
        try {
            bookingList = bookingDAO.findAll();
            Object[][] data = new Object[bookingList.size()][4];

            for (int i = 0; i < bookingList.size(); i++) {
                Booking b = bookingList.get(i);

                // Use getFullname() for customer
                User customer = b.getCustomer();
                Vehicle vehicle = b.getVehicle();
                ParkingSlot slot = b.getParkingSlot();

                data[i][0] = customer != null ? customer.getFullname() : b.getCustomerId();
                data[i][1] = vehicle != null ? vehicle.getType() : b.getVehicleId();
                data[i][2] = slot != null ? slot.getSlotNumber() : b.getSlotId();
                data[i][3] = b.getBookingTime(); // use bookingTime instead of getBookingDate()
            }

            bookingTable.setModel(new javax.swing.table.DefaultTableModel(
                data,
                new String[]{"Customer", "Vehicle", "Parking Slot", "Date"}
            ));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Example filter method
    public void filterBookings(String query) {
        try {
            Object[][] data = bookingList.stream()
                .filter(b -> (b.getCustomer() != null && b.getCustomer().getFullname().toLowerCase().contains(query.toLowerCase())) ||
                             (b.getVehicle() != null && b.getVehicle().getType().toLowerCase().contains(query.toLowerCase())) ||
                             (b.getParkingSlot() != null && b.getParkingSlot().getSlotNumber().toString().contains(query)))
                .map(b -> new Object[]{
                    b.getCustomer() != null ? b.getCustomer().getFullname() : b.getCustomerId(),
                    b.getVehicle() != null ? b.getVehicle().getType() : b.getVehicleId(),
                    b.getParkingSlot() != null ? b.getParkingSlot().getSlotNumber() : b.getSlotId(),
                    b.getBookingTime()
                })
                .toArray(Object[][]::new);

            bookingTable.setModel(new javax.swing.table.DefaultTableModel(
                data,
                new String[]{"Customer", "Vehicle", "Parking Slot", "Date"}
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
