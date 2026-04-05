package ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Минималистичная палитра и стили компонентов без внешних библиотек.
 */
public final class UiTheme {
    public static final Color BG = new Color(0xF4, 0xF3, 0xEF);
    public static final Color SURFACE = Color.WHITE;
    public static final Color BORDER = new Color(0xE2, 0xE0, 0xDA);
    public static final Color TEXT = new Color(0x1A, 0x18, 0x16);
    public static final Color MUTED = new Color(0x6B, 0x69, 0x64);
    public static final Color ACCENT = new Color(0x0D, 0x6E, 0x66);
    public static final Color ACCENT_HOVER = new Color(0x0A, 0x5C, 0x55);
    public static final Color ACCENT_SOFT = new Color(0xD4, 0xEF, 0xEB);
    public static final Color ACCENT_LINE = new Color(0x0D, 0x6E, 0x66);

    private UiTheme() {
    }

    public static Font baseFont() {
        String[] names = {"Segoe UI", "SF Pro Display", "Inter", "Dialog"};
        for (String n : names) {
            Font f = new Font(n, Font.PLAIN, 13);
            if (!f.getFamily().equals(Font.DIALOG) || n.equals("Dialog")) {
                return f;
            }
        }
        return new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    }

    public static Font titleFont() {
        return baseFont().deriveFont(Font.BOLD, 20f);
    }

    public static Font captionFont() {
        return baseFont().deriveFont(11f);
    }

    public static Font monoFont() {
        return new Font(Font.MONOSPACED, Font.PLAIN, 12);
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(14, 16, 14, 16));
    }

    public static Border cardBorderTight() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(8, 10, 8, 10));
    }

    public static void styleRoot(JComponent c) {
        c.setBackground(BG);
        c.setForeground(TEXT);
        c.setFont(baseFont());
    }

    public static void stylePrimaryButton(JButton b) {
        b.setFont(baseFont().deriveFont(Font.BOLD));
        b.setForeground(Color.WHITE);
        b.setBackground(ACCENT);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(10, 18, 10, 18));
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setBackground(ACCENT_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setBackground(ACCENT);
            }
        });
    }

    public static void styleGhostButton(JButton b) {
        b.setFont(baseFont());
        b.setForeground(TEXT);
        b.setBackground(SURFACE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(8, 14, 8, 14)));
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setBackground(new Color(0xF9, 0xF8, 0xF6));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setBackground(SURFACE);
            }
        });
    }

    public static void styleCombo(JComboBox<?> combo) {
        combo.setFont(baseFont());
        combo.setBackground(SURFACE);
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(6, 10, 6, 10)));
    }
}
