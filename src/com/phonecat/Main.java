package com.phonecat;

import com.phonecat.ui.MainUI;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import java.awt.Font;
import java.util.Enumeration;

/**
 * The main entry point for the application.
 */
public class Main {
    public static void main(String[] args) {
        // Set a modern Look and Feel for the UI
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Scale default UI fonts to improve readability
        scaleUIFont(1.2f); // 20% larger; adjust if needed

        // Run the UI on the Event Dispatch Thread (EDT) for thread safety
        SwingUtilities.invokeLater(() -> {
            MainUI ui = new MainUI();
            ui.setVisible(true);
        });
    }

    private static void scaleUIFont(float scale) {
        try {
            java.util.Enumeration<?> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object val = UIManager.get(key);
                if (val instanceof FontUIResource) {
                    FontUIResource f = (FontUIResource) val;
                    int newSize = Math.round(f.getSize() * scale);
                    if (newSize < 10) newSize = 10;
                    UIManager.put(key, new FontUIResource(f.deriveFont((float) newSize)));
                }
            }
        } catch (Exception ignore) {}
    }
}
