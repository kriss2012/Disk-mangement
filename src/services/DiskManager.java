package services;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import model.DriveInfo;

public class DiskManager {

    public List<DriveInfo> getAllDrives() {
        List<DriveInfo> driveList = new ArrayList<>();
        try {
            File[] roots = File.listRoots();
            if (roots != null) {
                for (File root : roots) {
                    try {
                        long total = root.getTotalSpace();
                        if (total == 0) {
                            continue; // Skip unmounted or empty drives
                        }
                        long free = root.getFreeSpace();
                        long used = total - free;
                        driveList.add(new DriveInfo(root.getAbsolutePath(), total, used, free));
                    } catch (SecurityException e) {
                        System.err.println("Permission denied for drive: " + root.getAbsolutePath());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error listing drives: " + e.getMessage());
        }
        return driveList;
    }

    public static String formatSize(long bytes) {
        if (bytes < 0) return "0.00 B";
        if (bytes >= 1024L * 1024L * 1024L) {
            double gb = bytes / (1024.0 * 1024.0 * 1024.0);
            return String.format("%.2f GB", gb);
        } else if (bytes >= 1024L * 1024L) {
            double mb = bytes / (1024.0 * 1024.0);
            return String.format("%.2f MB", mb);
        } else if (bytes >= 1024L) {
            double kb = bytes / 1024.0;
            return String.format("%.2f KB", kb);
        } else {
            return bytes + " B";
        }
    }

    public static void main(String[] args) {
        DiskManager dm = new DiskManager();
        List<DriveInfo> drives = dm.getAllDrives();
        System.out.println("Detected Drives:");
        for (DriveInfo d : drives) {
            System.out.printf("Drive: %s  Total: %s  Used: %s  Free: %s  Usage: %.1f%%%n",
                d.getPath(),
                formatSize(d.getTotalBytes()),
                formatSize(d.getUsedBytes()),
                formatSize(d.getFreeBytes()),
                d.getUsagePercent());
        }
    }
}
