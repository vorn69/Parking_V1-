package ui;

import dao.UserDAO;
import dao.UserGroupDAO;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;
import models.User;
import models.UserGroup;

public class UserManagementPanel extends JPanel {

    private JTable userTable;
    private DefaultTableModel tableModel;
    private JLabel pageInfoLabel;

    private final Color MAIN_BG = new Color(245, 247, 250);
    private final Color CARD_BG = Color.WHITE;
    private final Color TEXT_BLACK = Color.BLACK;
    private final Color TEXT_WHITE = Color.WHITE;
    private final Color BOTTOM_BG = Color.BLACK;
    private final Color ACCENT_BLUE = new Color(0, 122, 255);
    private final Color ACCENT_GREEN = new Color(52, 199, 89);

    private UserDAO userDAO;
    private UserGroupDAO groupDAO;

    public UserManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(MAIN_BG);

        userDAO = new UserDAO();
        groupDAO = new UserGroupDAO();

        initializePanel();
        loadUsersFromDatabase();
    }

    private void initializePanel() {
        // Title
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(MAIN_BG);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        JLabel title = new JLabel("USER MANAGEMENT");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_BLACK);
        titlePanel.add(title, BorderLayout.WEST);
        add(titlePanel, BorderLayout.NORTH);

        // Content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(MAIN_BG);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        // Search + Action buttons
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBackground(MAIN_BG);

        JLabel searchLabel = new JLabel("Search:");
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        styleButton(searchButton, ACCENT_BLUE);

        JButton refreshButton = new JButton("↻ Refresh");
        styleButton(refreshButton, ACCENT_BLUE);
        refreshButton.addActionListener(e -> loadUsersFromDatabase());

        JButton addUserButton = new JButton("+ Add New User");
        styleButton(addUserButton, ACCENT_GREEN);
        addUserButton.addActionListener(e -> showAddUserDialog());

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);
        searchPanel.add(addUserButton);

        contentPanel.add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Fullname", "Username", "Contact", "Email", "Group", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        userTable = new JTable(tableModel);
        userTable.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(userTable);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        // Pagination info (optional)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BOTTOM_BG);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pageInfoLabel = new JLabel();
        pageInfoLabel.setForeground(TEXT_WHITE);
        bottomPanel.add(pageInfoLabel, BorderLayout.WEST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(TEXT_WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void loadUsersFromDatabase() {
        try {
            List<User> users = userDAO.findAll();
            tableModel.setRowCount(0);
            for (User u : users) {
                UserGroup group = groupDAO.findById(u.getUserGroupId());
                tableModel.addRow(new Object[]{
                    u.getUserId(),
                    u.getFullname(),
                    u.getUsername(),
                    u.getContact(),
                    u.getEmail(),
                    group != null ? group.getGroupName() : "-",
                    u.getStatus() == 1 ? "Active" : "Inactive"
                });
            }
            pageInfoLabel.setText("Showing 1 to " + users.size() + " of " + users.size() + " entries");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load users from database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddUserDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New User", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblFullname = new JLabel("Fullname:");
        JTextField tfFullname = new JTextField(20);
        JLabel lblUsername = new JLabel("Username:");
        JTextField tfUsername = new JTextField(20);
        JLabel lblPassword = new JLabel("Password:");
        JPasswordField pfPassword = new JPasswordField(20);
        JLabel lblEmail = new JLabel("Email:");
        JTextField tfEmail = new JTextField(20);
        JLabel lblContact = new JLabel("Contact:");
        JTextField tfContact = new JTextField(20);

        // User group dropdown
        JLabel lblGroup = new JLabel("Group:");
        JComboBox<String> cbGroup = new JComboBox<>();
        try {
            for (UserGroup g : groupDAO.findAll()) {
                cbGroup.addItem(g.getGroupName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        gbc.gridx = 0; gbc.gridy = 0; panel.add(lblFullname, gbc);
        gbc.gridx = 1; panel.add(tfFullname, gbc);
        gbc.gridx = 0; gbc.gridy++; panel.add(lblUsername, gbc);
        gbc.gridx = 1; panel.add(tfUsername, gbc);
        gbc.gridx = 0; gbc.gridy++; panel.add(lblPassword, gbc);
        gbc.gridx = 1; panel.add(pfPassword, gbc);
        gbc.gridx = 0; gbc.gridy++; panel.add(lblEmail, gbc);
        gbc.gridx = 1; panel.add(tfEmail, gbc);
        gbc.gridx = 0; gbc.gridy++; panel.add(lblContact, gbc);
        gbc.gridx = 1; panel.add(tfContact, gbc);
        gbc.gridx = 0; gbc.gridy++; panel.add(lblGroup, gbc);
        gbc.gridx = 1; panel.add(cbGroup, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");
        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; panel.add(btnPanel, gbc);

        dialog.add(panel);

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            try {
                String fullname = tfFullname.getText();
                String username = tfUsername.getText();
                String password = new String(pfPassword.getPassword());
                String email = tfEmail.getText();
                String contact = tfContact.getText();
                String groupName = (String) cbGroup.getSelectedItem();
                UserGroup group = null;
                for (UserGroup g : groupDAO.findAll()) {
                    if (g.getGroupName().equals(groupName)) { group = g; break; }
                }

                User u = new User();
                u.setFullname(fullname);
                u.setUsername(username);
                u.setPassword(password);
                u.setEmail(email);
                u.setContact(contact);
                u.setUserGroupId(group != null ? group.getUserGroupId() : null);
                u.setStatus(1);

                userDAO.create(u);
                loadUsersFromDatabase();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "User added successfully!");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding user!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }
}
