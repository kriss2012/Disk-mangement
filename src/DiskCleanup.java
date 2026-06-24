import java.io.File;

public class DiskCleanup {

    public static void deleteTempFiles(String path) {

        File folder = new File(path);

        if (!folder.exists()) {
            System.out.println("Path not found!");
            return;
        }

        File[] files = folder.listFiles();

        if (files == null) {
            return;
        }

        int count = 0;

        for (File file : files) {

            if (file.getName().endsWith(".tmp")) {

                if (file.delete()) {
                    count++;
                }
            }
        }

        System.out.println(
                count + " temporary files deleted.");
    }
}
