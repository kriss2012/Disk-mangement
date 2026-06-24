# Module: Scanner

**File:** `docs/modules/SCANNER.md`
**Java classes:** `FileScanner.java` + `FileEntry.java` + `ScannerPane.java`

---

## 1. Module Purpose

The Scanner module lets the user pick any folder on their system and scan it to find out which files and sub-folders are consuming the most space. It walks the entire directory tree and returns a sorted list of the largest files. This is the "where did my space go?" answer. Scanning runs on a background thread so the UI never freezes.

---

## 2. Screens / Panels

### ScannerPane (Tab 2 in MainWindow)

**What is shown:**
- Path input field with a "Browse…" button (opens folder picker dialog)
- A "Scan" button
- A status label ("Ready" / "Scanning…" / "Found 3,412 files — 45.2 GB total")
- A ProgressBar (indeterminate during scan)
- A TableView with these columns:
  - File Name
  - Full Path
  - Size (human-readable: GB / MB / KB)
  - Type (File or Folder)
- Summary row below the table: "X files scanned · Total: Y GB"

**User actions:**
- Type a path directly OR click "Browse…" to open a directory chooser
- Click "Scan" → starts background scan, updates table when done
- Click any row → copy path to clipboard (nice to have)

---

## 3. Component List

| Component | Type | Purpose |
|-----------|------|---------|
| `ScannerPane` | JavaFX VBox | Outer container |
| `TextField` (path input) | JavaFX TextField | User types or gets from Browse dialog |
| `Button` (Browse) | JavaFX Button | Opens `DirectoryChooser` |
| `Button` (Scan) | JavaFX Button | Starts background scan |
| `ProgressBar` | JavaFX ProgressBar | Indeterminate during scan |
| `Label` (status) | JavaFX Label | Shows current status text |
| `TableView<FileEntry>` | JavaFX TableView | Sorted list of results |
| `Label` (summary) | JavaFX Label | File count + total size |

---

## 4. Java Methods Needed

```java
// FileScanner.java
List<FileEntry> scan(String rootPath) throws IOException
  // Files.walk(Paths.get(rootPath))
  // For each Path: build FileEntry(absolutePath, size, extension, isDirectory)
  // Skip paths that throw IOException (permission denied) → log and continue
  // Sort by sizeBytes descending
  // Return up to 500 results (cap to avoid OutOfMemoryError on huge drives)

List<FileEntry> getTopLargest(String rootPath, int limit)
  // Calls scan(), returns first N

// FileEntry.java
// Getters for: absolutePath, fileName (just the name), sizeBytes, extension, isDirectory

// ScannerPane.java
void onBrowseClicked()
  // Opens DirectoryChooser
  // Sets pathField.setText(selected directory)

void onScanClicked()
  // Validate: path is not empty and File(path).exists() && File(path).isDirectory()
  // Create JavaFX Task<List<FileEntry>>
  //   calls FileScanner.scan(path)
  // On running: set status "Scanning…", progressBar visible + indeterminate
  // On succeeded: populate tableView, update summary label
  // On failed: show Alert dialog with error message
  // Run Task on new Thread

String formatSize(long bytes)
  // >= 1 GB → "X.XX GB"
  // >= 1 MB → "X.XX MB"
  // else → "X KB"
```

---

## 5. Validation & Edge Cases

| Case | Behaviour |
|------|-----------|
| Empty path field | Show "Please enter a folder path" label in red |
| Path does not exist | Show "Folder not found. Check the path." |
| Path is a file, not a folder | Show "Please select a folder, not a file." |
| No read permission | Show "Cannot read this folder. Try running as administrator." |
| Empty folder (0 files) | Show "No files found in this folder." |
| Very large directory (>100,000 files) | Show warning before starting; cap at 500 results |
| User clicks Scan while scan is running | Disable Scan button during active scan |

---

## 6. Background Thread Pattern (JavaFX Task)

```java
Task<List<FileEntry>> scanTask = new Task<>() {
    @Override
    protected List<FileEntry> call() throws Exception {
        updateMessage("Scanning…");
        return new FileScanner().scan(path);
    }
};

scanTask.setOnSucceeded(e -> {
    List<FileEntry> results = scanTask.getValue();
    tableView.getItems().setAll(results);
    statusLabel.setText("Found " + results.size() + " files");
    progressBar.setVisible(false);
});

scanTask.setOnFailed(e -> {
    progressBar.setVisible(false);
    showErrorAlert(scanTask.getException().getMessage());
});

new Thread(scanTask).start();
```

---

## 7. Build Order

1. Create `FileEntry.java` data class
2. Implement `FileScanner.scan()` — test with `System.out.println` in console first
3. Verify it lists files sorted by size for a known folder on your machine
4. Create `ScannerPane.java` — static layout (no real scan yet)
5. Add `DirectoryChooser` for the Browse button
6. Wire up Scan button with background `Task`
7. Populate `TableView` when scan completes
8. Add error handling and validation
9. Add summary label below the table
