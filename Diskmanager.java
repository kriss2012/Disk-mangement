import java.io.File;

public class DiskManager {

    public void displayDiskInformation() {

        File[] drives = File.listRoots();

        for (File drive : drives) {

            long total = drive.getTotalSpace();
            long free = drive.getFreeSpace();
            long used = total - free;

            System.out.println("Drive: " + drive);
            System.out.println("Total: " + convert(total));
            System.out.println("Used : " + convert(used));
            System.out.println("Free : " + convert(free));
            System.out.println("-----------------------");
        }
    }

    private String convert(long bytes) {
        return String.format("%.2f GB",
                bytes / (1024.0 * 1024 * 1024));
    }
}
