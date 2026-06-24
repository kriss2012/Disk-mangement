package model;

public class SafeItem {
    private final String id;
    private final String name;
    private final String originalPath;
    private final long sizeBytes;

    public SafeItem(String id, String name, String originalPath, long sizeBytes) {
        this.id = id;
        this.name = name;
        this.originalPath = originalPath;
        this.sizeBytes = sizeBytes;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }
}
