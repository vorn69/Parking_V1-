package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.User;

public class UserDAO extends BaseDAO<User> {

    @Override
    protected String getTableName() {
        return "inet_vehicleparking.tbl_user";
    }

    @Override
    protected String getIdColumnName() {
        return "user_id";
    }

    @Override
    protected User mapResultSetToEntity(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setAvatar(rs.getBytes("avatar"));
        user.setFullname(rs.getString("fullname"));
        user.setContact(rs.getString("contact"));
        user.setEmail(rs.getString("email"));
        user.setUserGroupId(rs.getInt("user_group_id"));
        user.setStatus(rs.getInt("status"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (!rs.wasNull()) {
            user.setCreatedAt(createdAt);
            user.setUpdatedAt(updatedAt);
        }

        return user;
    }

    // ========================= LOGIN / AUTH =========================
    public User checkLogin(String username, String password) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() +
                     " WHERE username = ? AND password = ? AND status = 1";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
                return null;
            }
        }
    }

    public boolean authenticate(String username, String password) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() +
                    " WHERE username = ? AND password = ? AND status = 1";

        try (Connection conn = getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    // ========================= CRUD =========================
    public Integer create(User user) throws SQLException {
        String sql = "INSERT INTO " + getTableName() +
                    " (username, password, avatar, fullname, contact, email, user_group_id, status, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());

            if (user.getAvatar() != null) {
                pstmt.setBytes(3, user.getAvatar());
            } else {
                pstmt.setNull(3, Types.BINARY);
            }

            pstmt.setString(4, user.getFullname());
            pstmt.setString(5, user.getContact());
            pstmt.setString(6, user.getEmail());

            if (user.getUserGroupId() != null) {
                pstmt.setInt(7, user.getUserGroupId());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }

            pstmt.setInt(8, user.getStatus() != null ? user.getStatus() : 1);

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return null;
            }
        }
    }

        public User login(String username, String password) throws SQLException {
        String sql = "SELECT * FROM inet_vehicleparking.tbl_user WHERE username=? AND password=? AND status=1";

        try (Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User();
                    u.setUserId(rs.getInt("user_id"));
                    u.setUsername(rs.getString("username"));
                    u.setUserGroupId((Integer) rs.getObject("user_group_id"));
                    return u;
                }
            }
        }
        return null;
    }

    public int findOwnerIdByUserId(int userId) throws SQLException {
    String sql = "SELECT vehicle_owner_id FROM inet_vehicleparking.tbl_vehicle_owner WHERE user_id=?";
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, userId);
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}


    public boolean update(User user) throws SQLException {
        String sql = "UPDATE " + getTableName() + " SET " +
                     "username = ?, password = ?, avatar = ?, fullname = ?, contact = ?, email = ?, " +
                     "user_group_id = ?, status = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());

            if (user.getAvatar() != null) {
                pstmt.setBytes(3, user.getAvatar());
            } else {
                pstmt.setNull(3, Types.BINARY);
            }

            pstmt.setString(4, user.getFullname());
            pstmt.setString(5, user.getContact());
            pstmt.setString(6, user.getEmail());

            if (user.getUserGroupId() != null) {
                pstmt.setInt(7, user.getUserGroupId());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }

            pstmt.setInt(8, user.getStatus() != null ? user.getStatus() : 1);
            pstmt.setInt(9, user.getUserId());

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM " + getTableName() + " WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    // ========================= FIND =========================
    public User findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToEntity(rs);
                return null;
            }
        }
    }

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToEntity(rs);
                return null;
            }
        }
    }

    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY fullname";
        List<User> users = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToEntity(rs));
            }
            return users;
        }
    }

    // ========================= OTHER UTILS =========================
    public boolean existsByUsername(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public int countUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            rs.next();
            return rs.getInt(1);
        }
    }
}
