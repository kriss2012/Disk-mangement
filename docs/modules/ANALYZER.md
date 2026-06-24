# Module: Analyzer

**File:** `docs/modules/ANALYZER.md`
**Java classes:** `StorageAnalyzer.java` + `AnalyzerPane.java`

---

## 1. Module Purpose

The Analyzer module takes the file list produced by a scan and breaks it down by file type (extension). It answers: "What kind of files are taking up my space?" — showing that `.mp4` files eat 40 GB while `.log` files eat 2 GB. It uses the results from the last Scanner run, so the user scans first, then switches to the Analyzer tab to see the breakdown.

---

## 2. Screens / Panels

### AnalyzerPane (Tab 3 in MainWindow)

**What is shown:**
- If no scan has been run yet: "Run a scan first (Scanner tab) to see analysis here."
- After a scan: A two-part view
  - **Left:** A bar chart or sorted list of extensions ranked by total size
    - Each row: extension name, total size, percentage of scanned total, mini bar
  - **Right:** A summary panel
    - Total scanned size
    - Number of unique file types found
    - Top 3 extensions by size (quick summary)

**User actions:**
- No direct user input — data flows from Scanner automatically
- Click a row → filter the Scanner tab's table to show only that extension (nice to have, v2)

---

## 3. Component List

| Component | Type | Purpose |
|-----------|------|---------|
| `AnalyzerPane` | JavaFX BorderPane | Outer container |
| `Label` (placeholder) | JavaFX Label | Shown when no scan data exists |
| `TableView<ExtensionRow>` | JavaFX TableView | List of extensions + sizes |
| `ProgressBar` (mini, per row) | JavaFX ProgressBar | Visual proportion per extension |
| Summary panel | JavaFX VBox | Shows totals on the right side |

**`ExtensionRow` (inner helper class):**
```java
class ExtensionRow {
    String extension;    // e.g. ".mp4"
    long totalBytes;     // sum of all files with this extension
    int fileCount;       // how many files
    double percent;      // of total scanned size
}
```

---

## 4. Java Methods Needed

```java
// StorageAnalyzer.java
Map<String, Long> analyzeByExtension(List<FileEntry> files)
  // Group files by extension (lowercase)
  // Sum sizeBytes per group
  // Return as LinkedHashMap sorted by value descending

long getTotalScannedSize(List<FileEntry> files)
  // Sum all FileEntry.sizeBytes

int countUniqueExtensions(List<FileEntry> files)
  // Count distinct extensions

// AnalyzerPane.java
void loadData(List<FileEntry> scanResults)
  // Called by ScannerPane when scan completes
  // Runs StorageAnalyzer.analyzeByExtension()
  // Builds List<ExtensionRow>
  // Populates tableView
  // Updates summary panel

void showPlaceholder()
  // Shows "Run a scan first" message
  // Called on initialize() if no scan data yet
```

---

## 5. Data Flow: Scanner → Analyzer

The Scanner pane holds the last scan result as a field. When the scan completes, it calls `analyzerPane.loadData(results)` to push data across. Both panes are created once in `MainWindow` and hold references to each other for this handoff.

```java
// In MainWindow.java
ScannerPane scannerPane = new ScannerPane();
AnalyzerPane analyzerPane = new AnalyzerPane();
scannerPane.setAnalyzerPane(analyzerPane); // link them
```

---

## 6. Edge Cases

| Case | Behaviour |
|------|-----------|
| No scan has been run | Show placeholder text; table is hidden |
| All files have no extension | Show extension as "(none)" |
| One extension dominates (e.g. 99%) | Other bars still visible; smallest grouped as "Other" if >20 types |
| Zero-byte files | Include in count but don't affect size totals meaningfully |
| Scanner ran on empty folder | Show "No data to analyze" |

---

## 7. Build Order

1. Add `ExtensionRow` helper class (can be inside `AnalyzerPane.java` as a static inner class)
2. Implement `StorageAnalyzer.analyzeByExtension()` — test in console first
3. Create `AnalyzerPane.java` with placeholder text (no data yet)
4. Wire the Scanner-to-Analyzer data handoff via `setAnalyzerPane()` in `MainWindow`
5. Implement `loadData()` to populate the table
6. Add mini ProgressBar per row for visual proportion
7. Add summary panel on the right
