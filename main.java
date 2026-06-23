import java.io.File;

public class Main {

    public static void main(String[] args) {

        File[] drives = File.listRoots();

        System.out.println("================================");
        System.out.println("      DISK MANAGEMENT SYSTEM");
        System.out.println("================================");

        for (File drive : drives) {

            long totalSpace = drive.getTotalSpace();
            long freeSpace = drive.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;

            System.out.println("\nDrive : " + drive);

            System.out.println("Total Space : " + formatSize(totalSpace));
            System.out.println("Used Space  : " + formatSize(usedSpace));
            System.out.println("Free Space  : " + formatSize(freeSpace));

            double usage =
                    ((double) usedSpace / totalSpace) * 100;

            System.out.printf("Usage       : %.2f%%\n", usage);
        }
    }

    private static String formatSize(long bytes) {

        double gb = bytes / (1024.0 * 1024 * 1024);

        return String.format("%.2f GB", gb);
    }
}
