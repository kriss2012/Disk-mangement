package model;

public class DriveInfo {
    private String path;
    private long totalBytes;
    private long usedBytes;
    private long freeBytes;
    private double usagePercent;

    public DriveInfo(String path, long totalBytes, long usedBytes, long freeBytes) {
        this.path = path;
        this.totalBytes = totalBytes;
        this.usedBytes = usedBytes;
        this.freeBytes = freeBytes;
        this.usagePercent = totalBytes > 0
            ? ((double) usedBytes / totalBytes) * 100.0
            : 0.0;
    }

    public String getPath() {
        return path;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public long getUsedBytes() {
        return usedBytes;
    }

    public long getFreeBytes() {
        return freeBytes;
    }

    public double getUsagePercent() {
        return usagePercent;
    }
}
