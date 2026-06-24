import java.io.File;
import java.util.ArrayList;
import java.util.List;
import model.CleanupItem;

public class DiskCleanup {

    public List<CleanupItem> findCleanupTargets() {
        List<CleanupItem> items = new ArrayList<>();
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            // Windows Temp Files
            String userTemp = System.getenv("TEMP");
            if (userTemp != null) {
                addIfValid(items, userTemp, "User Temporary Files", "temp");
            }
            addIfValid(items, "C:\\Windows\\Temp", "System Temporary Files", "temp");
            addIfValid(items, "C:\\$Recycle.Bin", "Recycle Bin", "trash");
            addIfValid(items, "C:\\Windows\\SoftwareDistribution\\Download", "Windows Update Cache", "cache");
        } else if (os.contains("mac")) {
            // macOS Caches & Trash
            String macTemp = System.getenv("TMPDIR");
            if (macTemp != null) {
                addIfValid(items, macTemp, "System Temporary Files", "temp");
            }
            String userHome = System.getProperty("user.home");
            if (userHome != null) {
                addIfValid(items, userHome + "/Library/Caches", "User Caches", "cache");
                addIfValid(items, userHome + "/.Trash", "User Trash", "trash");
            }
        } else {
            // Linux / Unix / etc.
            addIfValid(items, "/tmp", "System Temporary Files", "temp");
            String userHome = System.getProperty("user.home");
            if (userHome != null) {
                addIfValid(items, userHome + "/.cache", "User Caches", "cache");
                addIfValid(items, userHome + "/.local/share/Trash", "User Trash", "trash");
            }
        }

        // Sort by estimated size descending
        items.sort((a, b) -> Long.compare(b.getEstimatedBytes(), a.getEstimatedBytes()));
        return items;
    }

    private void addIfValid(List<CleanupItem> items, String pathStr, String label, String type) {
        try {
            File pathFile = new File(pathStr);
            if (pathFile.exists() && pathFile.canRead()) {
                long size = calculateFolderSize(pathFile);
                if (size > 0) {
                    items.add(new CleanupItem(pathFile.getAbsolutePath(), label, size, type));
                }
            }
        } catch (Exception e) {
            // Skip paths that throw SecurityExceptions or other errors
        }
    }

    public long calculateFolderSize(File folder) {
        if (folder == null || !folder.exists()) {
            return 0;
        }
        if (folder.isFile()) {
            return folder.length();
        }
        long sum = 0;
        try {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        sum += calculateFolderSize(file);
                    } else {
                        sum += file.length();
                    }
                }
            }
        } catch (Exception e) {
            // Skip unreadable files or directories
        }
        return sum;
    }

    public boolean deleteItem(CleanupItem item) {
        File target = new File(item.getPath());
        if (!target.exists()) {
            return true;
        }

        // Safety check: Never delete critical system files or directories
        String pathLower = target.getAbsolutePath().toLowerCase();
        String userHome = System.getProperty("user.home").toLowerCase();
        String winTemp = "c:\\windows\\temp";
        String userTempWin = System.getenv("TEMP") != null ? System.getenv("TEMP").toLowerCase() : "";
        String macOSCache = (userHome + "/library/caches").replace("/", File.separator).toLowerCase();
        String linuxCache = (userHome + "/.cache").replace("/", File.separator).toLowerCase();
        String macOSTrash = (userHome + "/.trash").replace("/", File.separator).toLowerCase();
        String linuxTrash = (userHome + "/.local/share/trash").replace("/", File.separator).toLowerCase();
        String tmpLinux = "/tmp";

        // For major root folders, we want to clear their contents but NOT delete the root folder itself.
        boolean deleteRoot = true;
        if (pathLower.equals(winTemp) || 
            pathLower.equals(userTempWin) || 
            pathLower.equals(macOSCache) || 
            pathLower.equals(linuxCache) || 
            pathLower.equals(macOSTrash) || 
            pathLower.equals(linuxTrash) || 
            pathLower.equals(tmpLinux)) {
            deleteRoot = false;
        }

        return deleteRecursive(target, deleteRoot);
    }

    private boolean deleteRecursive(File file, boolean deleteRoot) {
        if (file == null || !file.exists()) {
            return true;
        }
        boolean allSucceeded = true;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    boolean childSucceeded = deleteRecursive(child, true);
                    if (!childSucceeded) {
                        allSucceeded = false;
                    }
                }
            }
        }
        if (deleteRoot) {
            try {
                boolean deleted = file.delete();
                if (deleted) {
                    System.out.println("Deleted: " + file.getAbsolutePath());
                } else {
                    System.out.println("Failed to delete (in use or protected): " + file.getAbsolutePath());
                    allSucceeded = false;
                }
            } catch (Exception e) {
                System.out.println("Error deleting " + file.getAbsolutePath() + ": " + e.getMessage());
                allSucceeded = false;
            }
        }
        return allSucceeded;
    }

    public static void main(String[] args) {
        DiskCleanup dc = new DiskCleanup();
        
        System.out.println("Scanning system for common junk locations...");
        List<CleanupItem> items = dc.findCleanupTargets();
        for (CleanupItem item : items) {
            System.out.printf("Label: %-25s | Path: %-50s | Size: %12s | Type: %s%n",
                    item.getLabel(), item.getPath(), DiskManager.formatSize(item.getEstimatedBytes()), item.getType());
        }

        // Safe self-test
        System.out.println("\nRunning a safe self-test on temporary files...");
        try {
            File testDir = new File("./test_cleanup_dir");
            testDir.mkdirs();
            File testFile1 = new File(testDir, "test_file_1.tmp");
            File testFile2 = new File(testDir, "test_file_2.log");
            testFile1.createNewFile();
            testFile2.createNewFile();
            
            System.out.println("Created test directory and files: " + testDir.getAbsolutePath());
            CleanupItem testItem = new CleanupItem(testDir.getAbsolutePath(), "Test Cleanup Item", dc.calculateFolderSize(testDir), "temp");
            
            System.out.printf("Deleting test item... Size was: %,d bytes%n", testItem.getEstimatedBytes());
            boolean success = dc.deleteItem(testItem);
            System.out.println("Deletion success status: " + success);
            
            // Clean up the parent test directory if still exists
            if (testDir.exists()) {
                testDir.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
