// src/dao/VehicleOwnerDAO.java
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
        owner.setVehicleOwnerContact(rs.getString("vehicle_owner_contact"));
        owner.setVehicleOwnerEmail(rs.getString("vehicle_owner_email"));
        owner.setStatus(rs.getInt("status"));
        owner.setUserId(rs.getInt("user_id"));
        owner.setCreatedAt(rs.getTimestamp("created_at"));
        owner.setUpdatedAt(rs.getTimestamp("updated_at"));

        return owner;
    }

    /* ================= CREATE ================= */

    public Integer create(VehicleOwner owner) throws SQLException {
        String sql = """
            INSERT INTO inet_vehicleparking.tbl_vehicle_owner
            (vehicle_owner_contact, vehicle_owner_email, status, user_id, created_at)
            VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, owner.getVehicleOwnerContact());
            ps.setString(2, owner.getVehicleOwnerEmail());
            ps.setInt(3, owner.getStatus() != null ? owner.getStatus() : 1);

            if (owner.getUserId() != null)
                ps.setInt(4, owner.getUserId());
            else
                ps.setNull(4, Types.INTEGER);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    /* ================= READ ================= */

    public VehicleOwner findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM inet_vehicleparking.tbl_vehicle_owner WHERE vehicle_owner_id=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapResultSetToEntity(rs) : null;
            }
        }
    }

    public List<VehicleOwner> findAll() throws SQLException {
        String sql = "SELECT * FROM inet_vehicleparking.tbl_vehicle_owner ORDER BY vehicle_owner_id";
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
            SET vehicle_owner_contact=?,
                vehicle_owner_email=?,
                status=?,
                user_id=?,
                updated_at=CURRENT_TIMESTAMP
            WHERE vehicle_owner_id=?
        """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, owner.getVehicleOwnerContact());
            ps.setString(2, owner.getVehicleOwnerEmail());
            ps.setInt(3, owner.getStatus() != null ? owner.getStatus() : 1);

            if (owner.getUserId() != null)
                ps.setInt(4, owner.getUserId());
            else
                ps.setNull(4, Types.INTEGER);

            ps.setInt(5, owner.getVehicleOwnerId());
            return ps.executeUpdate() > 0;
        }
    }

    /* ================= DELETE ================= */

    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM inet_vehicleparking.tbl_vehicle_owner WHERE vehicle_owner_id=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /* ================= COUNT ================= */

    public int countOwners() throws SQLException {
        String sql = "SELECT COUNT(*) FROM inet_vehicleparking.tbl_vehicle_owner";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
