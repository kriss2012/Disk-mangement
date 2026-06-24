package model;

import java.nio.file.Paths;

public class FileEntry {
    private String absolutePath;
    private long sizeBytes;
    private String extension;
    private boolean isDirectory;

    public FileEntry(String absolutePath, long sizeBytes, String extension, boolean isDirectory) {
        this.absolutePath = absolutePath;
        this.sizeBytes = sizeBytes;
        this.extension = extension;
        this.isDirectory = isDirectory;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getExtension() {
        return extension;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public String getFileName() {
        if (absolutePath == null) {
            return "";
        }
        try {
            return Paths.get(absolutePath).getFileName().toString();
        } catch (Exception e) {
            // Fallback for root directories or invalid path names
            int lastSlash = absolutePath.lastIndexOf(java.io.File.separator);
            if (lastSlash >= 0 && lastSlash < absolutePath.length() - 1) {
                return absolutePath.substring(lastSlash + 1);
            }
            return absolutePath;
        }
    }
}
