import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import model.FileEntry;

public class FileScanner {

    public List<FileEntry> scan(String rootPath) throws IOException {
        List<FileEntry> entries = new ArrayList<>();
        Path root = Paths.get(rootPath);
        if (!Files.exists(root) || !Files.isDirectory(root)) {
            return entries;
        }

        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                try {
                    String pathStr = file.toAbsolutePath().toString();
                    long size = attrs.size();
                    String ext = getFileExtension(pathStr);
                    entries.add(new FileEntry(pathStr, size, ext, false));
                } catch (Exception e) {
                    // Skip files that fail metadata collection
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                // Skip files we cannot access
                return FileVisitResult.CONTINUE;
            }
        });

        // Sort by sizeBytes descending
        entries.sort((a, b) -> Long.compare(b.getSizeBytes(), a.getSizeBytes()));

        // Limit results to 500 to avoid UI lag and memory issues on large drives
        if (entries.size() > 500) {
            return new ArrayList<>(entries.subList(0, 500));
        }
        return entries;
    }

    public List<FileEntry> getTopLargest(String rootPath, int limit) throws IOException {
        List<FileEntry> all = scan(rootPath);
        if (all.size() > limit) {
            return new ArrayList<>(all.subList(0, limit));
        }
        return all;
    }

    private String getFileExtension(String path) {
        int dotIndex = path.lastIndexOf('.');
        int separatorIndex = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        if (dotIndex > separatorIndex && dotIndex < path.length() - 1) {
            return path.substring(dotIndex + 1).toLowerCase();
        }
        return "(none)";
    }

    public static void main(String[] args) {
        FileScanner fs = new FileScanner();
        try {
            // Test with the workspace folder itself
            System.out.println("Scanning current workspace directory...");
            List<FileEntry> results = fs.getTopLargest(".", 10);
            for (FileEntry f : results) {
                System.out.printf("%-60s  %,d bytes (%s)%n", f.getAbsolutePath(), f.getSizeBytes(), f.getExtension());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
