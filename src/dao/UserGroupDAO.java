    package dao;

    import java.sql.*;
    import java.util.ArrayList;
    import java.util.List;
    import models.UserGroup;

    public class UserGroupDAO extends BaseDAO<UserGroup> {
        
        @Override
        protected String getTableName() {
            return "inet_vehicleparking.tbl_user_group";
        }
        
        @Override
        protected String getIdColumnName() {
            return "user_group_id";
        }
        
        @Override
        protected UserGroup mapResultSetToEntity(ResultSet rs) throws SQLException {
            UserGroup group = new UserGroup();
            group.setUserGroupId(rs.getInt("user_group_id"));
            group.setGroupName(rs.getString("group_name"));
            group.setDescription(rs.getString("description"));
            group.setAllowAdd(rs.getInt("allow_add"));
            group.setAllowEdit(rs.getInt("allow_edit"));
            group.setAllowDelete(rs.getInt("allow_delete"));
            group.setAllowPrint(rs.getInt("allow_print"));
            group.setAllowImport(rs.getInt("allow_import"));
            group.setAllowExport(rs.getInt("allow_export"));
            return group;
        }
        
        // CRUD Operations
        public Integer create(UserGroup group) throws SQLException {
            String sql = "INSERT INTO " + getTableName() + 
                        " (group_name, description, allow_add, allow_edit, allow_delete, " +
                        "allow_print, allow_import, allow_export) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            
            try {
                conn = getConnection();
                pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pstmt.setString(1, group.getGroupName());
                pstmt.setString(2, group.getDescription());
                pstmt.setInt(3, group.getAllowAdd() != null ? group.getAllowAdd() : 0);
                pstmt.setInt(4, group.getAllowEdit() != null ? group.getAllowEdit() : 0);
                pstmt.setInt(5, group.getAllowDelete() != null ? group.getAllowDelete() : 0);
                pstmt.setInt(6, group.getAllowPrint() != null ? group.getAllowPrint() : 0);
                pstmt.setInt(7, group.getAllowImport() != null ? group.getAllowImport() : 0);
                pstmt.setInt(8, group.getAllowExport() != null ? group.getAllowExport() : 0);
                
                pstmt.executeUpdate();
                
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return null;
            } finally {
                closeResources(rs, pstmt);  // Changed from 3 params to 2 params
            }
        }
        
        public UserGroup findById(Integer id) throws SQLException {
            String sql = "SELECT * FROM " + getTableName() + " WHERE user_group_id = ?";
            
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
                closeResources(rs, pstmt);  // Changed from 3 params to 2 params
            }
        }
        
        @Override
        public List<UserGroup> findAll() throws SQLException {
            String sql = "SELECT * FROM " + getTableName() + " ORDER BY group_name";
            List<UserGroup> groups = new ArrayList<>();
            
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            
            try {
                conn = getConnection();
                pstmt = conn.prepareStatement(sql);
                rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    groups.add(mapResultSetToEntity(rs));
                }
                return groups;
            } finally {
                closeResources(rs, pstmt);  // Changed from 3 params to 2 params
            }
        }
        
        public boolean update(UserGroup group) throws SQLException {
            String sql = "UPDATE " + getTableName() + " SET " +
                        "group_name = ?, description = ?, allow_add = ?, allow_edit = ?, " +
                        "allow_delete = ?, allow_print = ?, allow_import = ?, allow_export = ? " +
                        "WHERE user_group_id = ?";
            
            Connection conn = null;
            PreparedStatement pstmt = null;
            
            try {
                conn = getConnection();
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, group.getGroupName());
                pstmt.setString(2, group.getDescription());
                pstmt.setInt(3, group.getAllowAdd() != null ? group.getAllowAdd() : 0);
                pstmt.setInt(4, group.getAllowEdit() != null ? group.getAllowEdit() : 0);
                pstmt.setInt(5, group.getAllowDelete() != null ? group.getAllowDelete() : 0);
                pstmt.setInt(6, group.getAllowPrint() != null ? group.getAllowPrint() : 0);
                pstmt.setInt(7, group.getAllowImport() != null ? group.getAllowImport() : 0);
                pstmt.setInt(8, group.getAllowExport() != null ? group.getAllowExport() : 0);
                pstmt.setInt(9, group.getUserGroupId());
                
                return pstmt.executeUpdate() > 0;
            } finally {
                closeResources(pstmt, null);  // Changed from 3 params to 2 params
            }
        }
        
        @Override
        public boolean delete(Integer id) throws SQLException {
            String sql = "DELETE FROM " + getTableName() + " WHERE user_group_id = ?";
            
            Connection conn = null;
            PreparedStatement pstmt = null;
            
            try {
                conn = getConnection();
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, id);
                
                return pstmt.executeUpdate() > 0;
            } finally {
                closeResources(pstmt, null);  // Changed from 3 params to 2 params
            }
        }
        
        public boolean existsByName(String groupName) throws SQLException {
            String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE group_name = ?";
            
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            
            try {
                conn = getConnection();
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, groupName);
                rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            } finally {
                closeResources(rs, pstmt);  // Changed from 3 params to 2 params
            }
        }
    }