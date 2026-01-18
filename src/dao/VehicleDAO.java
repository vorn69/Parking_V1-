// src/dao/VehicleDAO.java
package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Vehicle;
import models.VehicleCategory;
import models.VehicleOwner;

public class VehicleDAO extends BaseDAO<Vehicle> {

    @Override
    protected String getTableName() {
        return "inet_vehicleparking.tbl_vehicle";
    }

    @Override
    protected String getIdColumnName() {
        return "vehicle_id";
    }

    @Override
    protected Vehicle mapResultSetToEntity(ResultSet rs) throws SQLException {
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleId(rs.getInt("vehicle_id"));
        vehicle.setVehicleCategoryId(rs.getInt("vehicle_category_id"));
        vehicle.setVehiclePlateNumber(rs.getString("vehicle_plate_number"));
        vehicle.setVehicleDescription(rs.getString("vehicle_description"));
        vehicle.setVehicleImage(rs.getBytes("vehicle_image"));
        vehicle.setVehicleOwnerId(rs.getInt("vehicle_owner_id"));
        return vehicle;
    }

    // Add this method to VehicleDAO.java
    /* ================= CREATE ================= */

    
    public Integer create(Vehicle vehicle) throws SQLException {
        String sql = "INSERT INTO " + getTableName() +
                " (vehicle_category_id, vehicle_plate_number, vehicle_description, vehicle_image, vehicle_owner_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, vehicle.getVehicleCategoryId());
            pstmt.setString(2, vehicle.getVehiclePlateNumber());
            pstmt.setString(3, vehicle.getVehicleDescription());

            if (vehicle.getVehicleImage() != null) {
                pstmt.setBytes(4, vehicle.getVehicleImage());
            } else {
                pstmt.setNull(4, Types.BINARY);
            }

            pstmt.setInt(5, vehicle.getVehicleOwnerId());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    public List<Vehicle> findByUserId(int userId) throws SQLException {
    String sql = """
        SELECT v.* 
        FROM inet_vehicleparking.tbl_vehicle v
        JOIN inet_vehicleparking.tbl_vehicle_owner vo ON v.vehicle_owner_id = vo.vehicle_owner_id
        WHERE vo.user_id = ?
    """;
    
    List<Vehicle> list = new ArrayList<>();
    try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, userId);
        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        }
    }
    return list;
}
    /* ================= READ ================= */

    public Vehicle findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE vehicle_id=?";

        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? mapResultSetToEntity(rs) : null;
            }
        }
    }

    public Vehicle findByPlateNumber(String plateNumber) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() +
                " WHERE vehicle_plate_number=?";

        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, plateNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? mapResultSetToEntity(rs) : null;
            }
        }
    }

    // In VehicleDAO.java - Add this method
// In VehicleDAO.java
public int getVehicleOwnerIdForUser(int userId) throws SQLException {
    String sql = "SELECT vehicle_owner_id FROM inet_vehicleparking.tbl_vehicle_owner WHERE user_id = ?";
    
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt(1) : -1;
    }
}

    public Vehicle findByIdWithDetails(Integer id) throws SQLException {
        String sql = "SELECT v.*, vc.vehicle_category_name, vo.vehicle_owner_name " +
                "FROM " + getTableName() + " v " +
                "JOIN inet_vehicleparking.tbl_vehicle_category vc ON v.vehicle_category_id = vc.vehicle_category_id " +
                "JOIN inet_vehicleparking.tbl_vehicle_owner vo ON v.vehicle_owner_id = vo.vehicle_owner_id " +
                "WHERE v.vehicle_id=?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {

                if (!rs.next()) return null;

                Vehicle vehicle = mapResultSetToEntity(rs);

                VehicleCategory category = new VehicleCategory();
                category.setVehicleCategoryId(rs.getInt("vehicle_category_id"));
                category.setVehicleCategoryName(rs.getString("vehicle_category_name"));
                vehicle.setVehicleCategory(category);

                VehicleOwner owner = new VehicleOwner();
                owner.setVehicleOwnerId(rs.getInt("vehicle_owner_id"));
                owner.setVehicleOwnerName(rs.getString("vehicle_owner_name"));
                vehicle.setVehicleOwner(owner);

                return vehicle;
            }
        }
    }

    public List<Vehicle> findAll() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() +
                " ORDER BY vehicle_plate_number";

        List<Vehicle> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        }
        return list;
    }

    public List<Vehicle> findByOwnerId(Integer ownerId) throws SQLException {
        return findByField("vehicle_owner_id", ownerId);
    }

    public List<Vehicle> findByCategoryId(Integer categoryId) throws SQLException {
        return findByField("vehicle_category_id", categoryId);
    }

    private List<Vehicle> findByField(String field, Integer value) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() +
                " WHERE " + field + "=? ORDER BY vehicle_plate_number";

        List<Vehicle> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, value);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEntity(rs));
                }
            }
        }
        return list;
    }

    public List<Vehicle> searchByPlateNumber(String plate) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() +
                " WHERE vehicle_plate_number ILIKE ? ORDER BY vehicle_plate_number";

        List<Vehicle> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + plate + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEntity(rs));
                }
            }
        }
        return list;
    }

    /* ================= UPDATE ================= */

    public boolean update(Vehicle vehicle) throws SQLException {
        String sql = "UPDATE " + getTableName() +
                " SET vehicle_category_id=?, vehicle_plate_number=?, vehicle_description=?, " +
                "vehicle_image=?, vehicle_owner_id=? WHERE vehicle_id=?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, vehicle.getVehicleCategoryId());
            pstmt.setString(2, vehicle.getVehiclePlateNumber());
            pstmt.setString(3, vehicle.getVehicleDescription());

            if (vehicle.getVehicleImage() != null) {
                pstmt.setBytes(4, vehicle.getVehicleImage());
            } else {
                pstmt.setNull(4, Types.BINARY);
            }

            pstmt.setInt(5, vehicle.getVehicleOwnerId());
            pstmt.setInt(6, vehicle.getVehicleId());

            return pstmt.executeUpdate() > 0;
        }
    }

    /* ================= DELETE ================= */

    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM " + getTableName() +
                " WHERE vehicle_id=?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    /* ================= COUNT ================= */

    public int countVehicles() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countVehiclesByOwner(Integer ownerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() +
                " WHERE vehicle_owner_id=?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, ownerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /* ================= VALIDATION ================= */

    public boolean existsByPlateNumber(String plateNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() +
                " WHERE vehicle_plate_number=?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, plateNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean isVehicleAvailableForBooking(Integer vehicleId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM inet_vehicleparking.tbl_booking " +
                "WHERE vehicle_id=? AND booking_status IN (1,2)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, vehicleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) == 0;
            }
        }
    }
}
