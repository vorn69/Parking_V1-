// src/models/UserGroup.java
package models;

public class UserGroup {
    private Integer userGroupId;
    private String groupName;
    private String description;
    private Integer allowAdd;
    private Integer allowEdit;
    private Integer allowDelete;
    private Integer allowPrint;
    private Integer allowImport;
    private Integer allowExport;
    
    // Constructors
    public UserGroup() {}
    
    public UserGroup(Integer userGroupId, String groupName, String description) {
        this.userGroupId = userGroupId;
        this.groupName = groupName;
        this.description = description;
    }
    
    // Getters and Setters
    public Integer getUserGroupId() { return userGroupId; }
    public void setUserGroupId(Integer userGroupId) { this.userGroupId = userGroupId; }
    
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Integer getAllowAdd() { return allowAdd; }
    public void setAllowAdd(Integer allowAdd) { this.allowAdd = allowAdd; }
    
    public Integer getAllowEdit() { return allowEdit; }
    public void setAllowEdit(Integer allowEdit) { this.allowEdit = allowEdit; }
    
    public Integer getAllowDelete() { return allowDelete; }
    public void setAllowDelete(Integer allowDelete) { this.allowDelete = allowDelete; }
    
    public Integer getAllowPrint() { return allowPrint; }
    public void setAllowPrint(Integer allowPrint) { this.allowPrint = allowPrint; }
    
    public Integer getAllowImport() { return allowImport; }
    public void setAllowImport(Integer allowImport) { this.allowImport = allowImport; }
    
    public Integer getAllowExport() { return allowExport; }
    public void setAllowExport(Integer allowExport) { this.allowExport = allowExport; }
}