// src/dao/VehicleCategoryDAO.java
package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.VehicleCategory;

public class VehicleCategoryDAO extends BaseDAO<VehicleCategory> {
    
    @Override
    protected String getTableName() {
        return "inet_vehicleparking.tbl_vehicle_category";
    }
    
    @Override
    protected String getIdColumnName() {
        return "vehicle_category_id";
    }
    
    @Override
    protected VehicleCategory mapResultSetToEntity(ResultSet rs) throws SQLException {
        VehicleCategory category = new VehicleCategory();
        category.setVehicleCategoryId(rs.getInt("vehicle_category_id"));
        category.setVehicleCategoryName(rs.getString("vehicle_category_name"));
        category.setUserId(rs.getInt("user_id"));
        return category;
    }
    
    public Integer create(VehicleCategory category) throws SQLException {
        String sql = "INSERT INTO " + getTableName() + " (vehicle_category_name, user_id) VALUES (?, ?)";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, category.getVehicleCategoryName());
            
            if (category.getUserId() != null) {
                pstmt.setInt(2, category.getUserId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            
            pstmt.executeUpdate();
            
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return null;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public VehicleCategory findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE vehicle_category_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToEntity(rs);
            }
            return null;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public List<VehicleCategory> findAll() throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " ORDER BY vehicle_category_name";
        List<VehicleCategory> categories = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                categories.add(mapResultSetToEntity(rs));
            }
            return categories;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public boolean update(VehicleCategory category) throws SQLException {
        String sql = "UPDATE " + getTableName() + 
                    " SET vehicle_category_name = ?, user_id = ? WHERE vehicle_category_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, category.getVehicleCategoryName());
            
            if (category.getUserId() != null) {
                pstmt.setInt(2, category.getUserId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            
            pstmt.setInt(3, category.getVehicleCategoryId());
            
            return pstmt.executeUpdate() > 0;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }
    
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM " + getTableName() + " WHERE vehicle_category_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            
            return pstmt.executeUpdate() > 0;
        } finally {
            closeResources(conn, pstmt, null);
        }
    }
    
    public boolean existsByName(String categoryName) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE vehicle_category_name = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, categoryName);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
    
    public int countVehiclesInCategory(Integer categoryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM inet_vehicleparking.tbl_vehicle WHERE vehicle_category_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, categoryId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(conn, pstmt, rs);
        }
    }
}