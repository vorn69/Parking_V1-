package ui;

import dao.UserDAO;
import dao.VehicleOwnerDAO;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.*;
import models.User;

public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnSignup;
    private UserDAO userDAO;
    private JPanel mainPanel;
    private boolean isPasswordVisible = false;

    // Modern Colors
    private final Color PRIMARY_COLOR = new Color(74, 144, 226);
    private final Color PRIMARY_HOVER = new Color(53, 122, 189);
    private final Color BACKGROUND_GRADIENT_1 = new Color(66, 134, 244);
    private final Color BACKGROUND_GRADIENT_2 = new Color(55, 59, 68);
    private final Color CARD_BG = new Color(255, 255, 255);
    private final Color TEXT_PRIMARY = new Color(50, 50, 50);
    private final Color TEXT_SECONDARY = new Color(150, 150, 150);

    public LoginFrame() {
        setTitle("Login - ParkFlow");
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        userDAO = new UserDAO();
        initUI();
    }

    private void initUI() {
        // Main Background Panel with Gradient
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, BACKGROUND_GRADIENT_1, getWidth(), getHeight(),
                        BACKGROUND_GRADIENT_2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // Add some subtle pattern or circles
                g2d.setColor(new Color(255, 255, 255, 20));
                g2d.fillOval(-100, -100, 400, 400);
                g2d.fillOval(getWidth() - 300, getHeight() - 300, 500, 500);
            }
        };
        mainPanel.setLayout(new GridBagLayout());

        // Login Card
        JPanel loginCard = createLoginCard();
        mainPanel.add(loginCard);

        add(mainPanel);

        // Enter key submits
        txtPassword.addActionListener(e -> login());

        SwingUtilities.invokeLater(() -> txtUsername.requestFocusInWindow());
    }

    private JPanel createLoginCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                g2d.dispose();
            }
        };
        card.setOpaque(false);
        card.setBackground(CARD_BG);
        card.setPreferredSize(new Dimension(450, 600));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(50, 50, 50, 50));

        // Drop Shadow (simulated by border here, simplified for Swing)
        card.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(10, 10, 10, 10),
                new EmptyBorder(50, 50, 50, 50)));

        // 1. Icon
        JLabel iconLabel = new JLabel("🚗");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 2. Title
        JLabel titleLabel = new JLabel("ParkFlow");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 3. Subtitle
        JLabel subtitleLabel = new JLabel("Welcome back!");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 4. Form
        JPanel formPanel = createFormPanel();

        // 5. Button
        btnLogin = createStyledButton("Sign In");
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 6. Footer (Signup)
        JPanel footerPanel = createSignupSection();

        // Add components with spacing
        card.add(iconLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(subtitleLabel);
        card.add(Box.createVerticalStrut(40));
        card.add(formPanel);
        card.add(Box.createVerticalStrut(30));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(20));
        card.add(footerPanel);

        return card;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username
        JLabel userLabel = new JLabel("USERNAME");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLabel.setForeground(TEXT_SECONDARY);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsername = createStyledTextField();
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password
        JLabel passLabel = new JLabel("PASSWORD");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passLabel.setForeground(TEXT_SECONDARY);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password Field Container (for toggle button)
        JPanel passContainer = new JPanel(new BorderLayout());
        passContainer.setOpaque(false);
        passContainer.setMaximumSize(new Dimension(500, 45));
        passContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = createStyledPasswordField();

        JButton toggleBtn = new JButton("👁");
        toggleBtn.setBorderPainted(false);
        toggleBtn.setContentAreaFilled(false);
        toggleBtn.setFocusPainted(false);
        toggleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleBtn.addActionListener(e -> togglePassword());

        passContainer.add(txtPassword, BorderLayout.CENTER);
        passContainer.add(toggleBtn, BorderLayout.EAST);
        passContainer.setBorder(txtPassword.getBorder()); // Move border to container
        txtPassword.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0)); // Remove field border

        // Re-apply border style to container on focus
        txtPassword.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                passContainer.setBorder(new LineBorder(PRIMARY_COLOR, 2, true));
            }

            @Override
            public void focusLost(FocusEvent e) {
                passContainer.setBorder(new LineBorder(new Color(200, 200, 200), 1, true));
            }
        });

        // Add to panel
        panel.add(userLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(txtUsername);
        panel.add(Box.createVerticalStrut(20));
        panel.add(passLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(passContainer);

        return panel;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(350, 45));
        field.setMaximumSize(new Dimension(350, 45));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setBackground(new Color(250, 252, 255));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(10, 15, 10, 15)));

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(PRIMARY_COLOR, 2, true),
                        new EmptyBorder(9, 14, 9, 14)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(200, 200, 200), 1, true),
                        new EmptyBorder(10, 15, 10, 15)));
            }
        });

        return field;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setBackground(new Color(250, 252, 255));
        return field;
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(PRIMARY_HOVER.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(PRIMARY_HOVER);
                } else {
                    g2d.setColor(PRIMARY_COLOR);
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);

                // Text
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);

                g2d.dispose();
            }
        };

        btn.setPreferredSize(new Dimension(350, 50));
        btn.setMaximumSize(new Dimension(350, 50));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> login());

        return btn;
    }

    private JPanel createSignupSection() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setOpaque(false);

        JLabel text = new JLabel("Don't have an account?");
        text.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        text.setForeground(TEXT_SECONDARY);

        btnSignup = new JButton("Create Account");
        btnSignup.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSignup.setForeground(PRIMARY_COLOR);
        btnSignup.setBorderPainted(false);
        btnSignup.setContentAreaFilled(false);
        btnSignup.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnSignup.addActionListener(e -> openSignup());

        panel.add(text);
        panel.add(btnSignup);
        return panel;
    }

    private void togglePassword() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            txtPassword.setEchoChar((char) 0);
        } else {
            txtPassword.setEchoChar('•');
        }
    }

    private void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            shakeFrame();
            return;
        }

        btnLogin.setText("Verifying...");
        btnLogin.setEnabled(false);

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                // Simulate slight delay for UX
                Thread.sleep(500);
                return userDAO.login(username, password);
            }

            @Override
            protected void done() {
                btnLogin.setText("Sign In");
                btnLogin.setEnabled(true);

                try {
                    User user = get();
                    if (user != null) {
                        openDashboard(user);
                    } else {
                        shakeFrame();
                        JOptionPane.showMessageDialog(LoginFrame.this,
                                "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void shakeFrame() {
        Point p = this.getLocation();
        Timer timer = new Timer(20, new ActionListener() {
            int count = 0;
            int direction = 1;

            @Override
            public void actionPerformed(ActionEvent e) {
                setLocation(p.x + (direction * 5), p.y);
                direction = -direction;
                count++;
                if (count >= 10) {
                    ((Timer) e.getSource()).stop();
                    setLocation(p);
                }
            }
        });
        timer.start();
    }

    private void openDashboard(User user) {
        try {
            int groupId = user.getUserGroupId() != null ? user.getUserGroupId() : 0;
            SwingUtilities.invokeLater(() -> {
                if (groupId == 1) {
                    new AdminDashboard().setVisible(true);
                } else {
                    try {
                        VehicleOwnerDAO ownerDAO = new VehicleOwnerDAO();
                        Integer ownerId = ownerDAO.findOwnerIdByUserId(user.getUserId());
                        new CustomerDashboard(user.getUserId()).setVisible(true);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
                dispose();
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openSignup() {
        JOptionPane.showMessageDialog(this, "Signup coming soon!");
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}