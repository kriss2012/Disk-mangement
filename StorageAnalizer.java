import java.io.File;

public class StorageAnalyzer {

    public static void analyzeDrive(String path) {

        File drive = new File(path);

        if (!drive.exists()) {
            System.out.println("Drive not found!");
            return;
        }

        long total = drive.getTotalSpace();
        long free = drive.getFreeSpace();
        long used = total - free;

        System.out.println("Analysis Report");
        System.out.println("------------------");
        System.out.println("Path : " + path);
        System.out.println("Total Space : " + total);
        System.out.println("Used Space  : " + used);
        System.out.println("Free Space  : " + free);
    }
}
