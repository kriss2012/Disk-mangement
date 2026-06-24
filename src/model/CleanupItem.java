package model;

public class CleanupItem {
    private String path;
    private String label;       // e.g. "Windows Temp Files"
    private long estimatedBytes;
    private String type;        // "temp" | "cache" | "log" | "trash"

    public CleanupItem(String path, String label, long estimatedBytes, String type) {
        this.path = path;
        this.label = label;
        this.estimatedBytes = estimatedBytes;
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public String getLabel() {
        return label;
    }

    public long getEstimatedBytes() {
        return estimatedBytes;
    }

    public String getType() {
        return type;
    }
}
