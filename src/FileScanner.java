import java.io.File;

public class FileScanner {

    public static void scanFolder(String path) {

        File folder = new File(path);

        if (!folder.exists()) {
            System.out.println("Folder not found!");
            return;
        }

        File[] files = folder.listFiles();

        if (files == null) {
            return;
        }

        for (File file : files) {

            if (file.isFile()) {
                System.out.println(
                        "File : " + file.getName() +
                        " | Size : " +
                        file.length() + " bytes");
            }
        }
    }
}
