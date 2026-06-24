import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.FileEntry;

public class StorageAnalyzer {

    public Map<String, Long> analyzeByExtension(List<FileEntry> files) {
        Map<String, Long> sizeMap = new HashMap<>();
        if (files == null) {
            return sizeMap;
        }
        for (FileEntry file : files) {
            String ext = file.getExtension();
            sizeMap.put(ext, sizeMap.getOrDefault(ext, 0L) + file.getSizeBytes());
        }
        // Return a LinkedHashMap sorted by size descending
        return sizeMap.entrySet()
                .stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        java.util.LinkedHashMap::new
                ));
    }

    public Map<String, Integer> countByExtension(List<FileEntry> files) {
        Map<String, Integer> countMap = new HashMap<>();
        if (files == null) {
            return countMap;
        }
        for (FileEntry file : files) {
            String ext = file.getExtension();
            countMap.put(ext, countMap.getOrDefault(ext, 0) + 1);
        }
        return countMap;
    }

    public long getTotalScannedSize(List<FileEntry> files) {
        if (files == null) {
            return 0;
        }
        long total = 0;
        for (FileEntry file : files) {
            total += file.getSizeBytes();
        }
        return total;
    }

    public int countUniqueExtensions(List<FileEntry> files) {
        if (files == null) {
            return 0;
        }
        java.util.Set<String> exts = new java.util.HashSet<>();
        for (FileEntry file : files) {
            exts.add(file.getExtension());
        }
        return exts.size();
    }

    public static void main(String[] args) {
        try {
            FileScanner scanner = new FileScanner();
            List<FileEntry> files = scanner.scan(".");
            StorageAnalyzer analyzer = new StorageAnalyzer();

            long totalSize = analyzer.getTotalScannedSize(files);
            int uniqueExts = analyzer.countUniqueExtensions(files);
            Map<String, Long> sizeBreakdown = analyzer.analyzeByExtension(files);
            Map<String, Integer> countBreakdown = analyzer.countByExtension(files);

            System.out.printf("Total Scanned Size: %,d bytes%n", totalSize);
            System.out.printf("Unique Extensions: %d%n%n", uniqueExts);
            System.out.printf("%-12s | %-15s | %-10s | %-10s%n", "Extension", "Total Size", "File Count", "Percentage");
            System.out.println("---------------------------------------------------------");

            for (Map.Entry<String, Long> entry : sizeBreakdown.entrySet()) {
                String ext = entry.getKey();
                long size = entry.getValue();
                int count = countBreakdown.getOrDefault(ext, 0);
                double pct = totalSize > 0 ? ((double) size / totalSize) * 100 : 0.0;
                System.out.printf("%-12s | %-15s | %-10d | %.2f%%%n",
                        ext, DiskManager.formatSize(size), count, pct);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
