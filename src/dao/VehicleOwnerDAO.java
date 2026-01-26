package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.VehicleOwner;

public class VehicleOwnerDAO extends BaseDAO<VehicleOwner> {

    @Override
    protected String getTableName() {
        return "inet_vehicleparking.tbl_vehicle_owner";
    }

    @Override
    protected String getIdColumnName() {
        return "vehicle_owner_id";
    }

    /* ================= RESULT MAPPER ================= */
    @Override
    protected VehicleOwner mapResultSetToEntity(ResultSet rs) throws SQLException {
        VehicleOwner owner = new VehicleOwner();

        owner.setVehicleOwnerId(rs.getInt("vehicle_owner_id"));

        // Handle vehicle_owner_name (might be null)
        try {
            owner.setVehicleOwnerName(rs.getString("vehicle_owner_name"));
        } catch (SQLException e) {
            // Column might not exist in some schemas
            owner.setVehicleOwnerName(null);
        }

        owner.setVehicleOwnerContact(rs.getString("vehicle_owner_contact"));
        owner.setVehicleOwnerEmail(rs.getString("vehicle_owner_email"));

        // Handle avatar BLOB safely
        try {
            Blob avatarBlob = rs.getBlob("avatar");
            if (avatarBlob != null) {
                owner.setAvatar(avatarBlob.getBytes(1, (int) avatarBlob.length()));
            }
        } catch (SQLException e) {
            // Column might not exist
        }

        // Handle optional fields
        try {
            owner.setOwnerUsername(rs.getString("owner_username"));
        } catch (SQLException e) {
            // Column might not exist
        }

        try {
            owner.setOwnerPassword(rs.getString("owner_password"));
        } catch (SQLException e) {
            // Column might not exist
        }

        owner.setStatus(rs.getInt("status"));
        owner.setUserId(rs.getInt("user_id"));

        // Handle timestamps (might be null)
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            owner.setCreatedAt(createdAt);
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            owner.setUpdatedAt(updatedAt);
        }

        return owner;
    }

    /* ================= CREATE ================= */
    public Integer create(VehicleOwner owner) throws SQLException {
        // Check which columns exist in your schema
        String sql = """
                    INSERT INTO inet_vehicleparking.tbl_vehicle_owner
                    (vehicle_owner_name, vehicle_owner_contact, vehicle_owner_email, status, user_id)
                    VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, owner.getVehicleOwnerName());
            ps.setString(2, owner.getVehicleOwnerContact());
            ps.setString(3, owner.getVehicleOwnerEmail());
            ps.setInt(4, owner.getStatus() != null ? owner.getStatus() : 1);

            if (owner.getUserId() != null) {
                ps.setInt(5, owner.getUserId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    /* ================= READ ================= */
    public VehicleOwner findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE vehicle_owner_id = ?";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToEntity(rs) : null;
            }
        }
    }

    public List<VehicleOwner> findAll() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY vehicle_owner_id";
        List<VehicleOwner> list = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        }
        return list;
    }

    /* ================= UPDATE ================= */
    public boolean update(VehicleOwner owner) throws SQLException {
        String sql = """
                    UPDATE inet_vehicleparking.tbl_vehicle_owner
                    SET vehicle_owner_name = ?,
                        vehicle_owner_contact = ?,
                        vehicle_owner_email = ?,
                        status = ?,
                        user_id = ?
                    WHERE vehicle_owner_id = ?
                """;

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, owner.getVehicleOwnerName());
            ps.setString(2, owner.getVehicleOwnerContact());
            ps.setString(3, owner.getVehicleOwnerEmail());
            ps.setInt(4, owner.getStatus() != null ? owner.getStatus() : 1);

            if (owner.getUserId() != null) {
                ps.setInt(5, owner.getUserId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            ps.setInt(6, owner.getVehicleOwnerId());
            return ps.executeUpdate() > 0;
        }
    }

    /* ================= FIND OWNER ID BY USER ID ================= */
    public Integer findOwnerIdByUserId(Integer userId) throws SQLException {
        String sql = "SELECT vehicle_owner_id FROM " + getTableName() + " WHERE user_id = ?";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("vehicle_owner_id") : null;
            }
        }
    }

    /* ================= DELETE ================= */
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM " + getTableName() + " WHERE vehicle_owner_id = ?";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /* ================= COUNT ================= */
    public int countOwners() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName();

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /* ================= ADDITIONAL METHODS ================= */

    // Find by user ID
    public VehicleOwner findByUserId(Integer userId) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE user_id = ?";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToEntity(rs) : null;
            }
        }
    }

    // Find by email
    public VehicleOwner findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE vehicle_owner_email = ?";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToEntity(rs) : null;
            }
        }
    }

    // Find by contact number
    public VehicleOwner findByContact(String contact) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE vehicle_owner_contact = ?";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, contact);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToEntity(rs) : null;
            }
        }
    }

    // Find active owners only
    public List<VehicleOwner> findActiveOwners() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE status = 1 ORDER BY vehicle_owner_id";
        List<VehicleOwner> list = new ArrayList<>();

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        }
        return list;
    }

    // Check if email already exists
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE vehicle_owner_email = ?";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // Check if contact already exists
    public boolean contactExists(String contact) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE vehicle_owner_contact = ?";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, contact);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}