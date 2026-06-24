package services;

import java.io.*;
import java.util.Properties;

public class AppConfig {
    private static final String CONFIG_FILE = "config.properties";
    private static final Properties props = new Properties();

    static {
        load();
    }

    public static void load() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (InputStream is = new FileInputStream(file)) {
                props.load(is);
            } catch (IOException e) {
                System.err.println("Failed to load config: " + e.getMessage());
            }
        }
    }

    public static void save() {
        try (OutputStream os = new FileOutputStream(CONFIG_FILE)) {
            props.store(os, "Disk Management System Configuration");
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }

    public static String getDefaultScanDir() {
        return props.getProperty("defaultScanDir", System.getProperty("user.home"));
    }

    public static void setDefaultScanDir(String dir) {
        props.setProperty("defaultScanDir", dir);
        save();
    }

    public static int getScanLimit() {
        try {
            return Integer.parseInt(props.getProperty("scanLimit", "500"));
        } catch (NumberFormatException e) {
            return 500;
        }
    }

    public static void setScanLimit(int limit) {
        props.setProperty("scanLimit", String.valueOf(limit));
        save();
    }

    public static boolean isConfirmDelete() {
        return Boolean.parseBoolean(props.getProperty("confirmDelete", "true"));
    }

    public static void setConfirmDelete(boolean confirm) {
        props.setProperty("confirmDelete", String.valueOf(confirm));
        save();
    }

    public static String getTheme() {
        return props.getProperty("theme", "macOS Dark");
    }

    public static void setTheme(String theme) {
        props.setProperty("theme", theme);
        save();
    }
}
