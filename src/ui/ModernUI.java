package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class ModernUI {
    
    public static final Color PRIMARY = new Color(67, 97, 238);
    public static final Color SECONDARY = new Color(255, 107, 107);
    public static final Color SUCCESS = new Color(76, 209, 96);
    public static final Color WARNING = new Color(255, 159, 67);
    public static final Color DARK_BG = new Color(30, 30, 46);
    public static final Color CARD_BG = new Color(37, 37, 59);
    public static final Color TEXT_LIGHT = new Color(250, 250, 250);
    public static final Color TEXT_MUTED = new Color(169, 169, 169);
    
    public static JButton createPrimaryButton(String text) {
        return createButton(text, PRIMARY, 15);
    }
    
    public static JButton createSuccessButton(String text) {
        return createButton(text, SUCCESS, 15);
    }
    
    public static JButton createDangerButton(String text) {
        return createButton(text, SECONDARY, 15);
    }
    
    public static JButton createButton(String text, Color color, int radius) {
        JButton btn = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(new EmptyBorder(12, 25, 12, 25));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        
        return btn;
    }
    
    public static JPanel createCard() {
        JPanel card = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        return card;
    }
    
    public static void applyModernStyle(JComponent component) {
        component.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        component.setForeground(TEXT_LIGHT);
    }
}