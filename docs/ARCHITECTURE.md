# ARCHITECTURE.md — Disk Management System

**Project:** Disk Management System
**Stack:** Java 17 · JavaFX 17 · OOP · File System APIs

---

## 1. Tech Stack

| Layer | Technology | Why |
|-------|-----------|-----|
| Language | Java 17 | Stable LTS; `java.io.File` and `java.nio` give full disk access |
| GUI | JavaFX 17 | Native Java GUI; better than Swing for modern layouts and progress bars |
| File I/O | `java.io.File`, `java.nio.file.*` | Built-in; no extra libraries needed for disk reads |
| Build | Manual `javac` or Maven | Simple enough to compile by hand; Maven if team grows |
| Testing | JUnit 5 | Standard Java unit test framework |

**No external dependencies needed for v1.** The Java standard library covers everything.

---

## 2. Folder Structure

```
Disk-Management/
├── src/
│   ├── Main.java                    ← App entry point; launches JavaFX window
│   ├── DiskManager.java             ← Lists drives, reads total/used/free
│   ├── StorageAnalyzer.java         ← Breaks usage down by file extension
│   ├── FileScanner.java             ← Walks a directory tree, builds file list
│   ├── DiskCleanup.java             ← Finds temp/junk files; handles deletion
│   ├── model/
│   │   ├── DriveInfo.java           ← Data class: drive path, total, used, free
│   │   ├── FileEntry.java           ← Data class: file path, size, extension
│   │   └── CleanupItem.java         ← Data class: path, estimated size, type
│   └── ui/
│       ├── MainWindow.java          ← JavaFX root: TabPane with 4 tabs
│       ├── DashboardPane.java       ← Tab 1: drive list + usage bars
│       ├── ScannerPane.java         ← Tab 2: folder picker + results table
│       ├── AnalyzerPane.java        ← Tab 3: file type breakdown chart
│       └── CleanupPane.java         ← Tab 4: cleanup suggestions + delete button
├── docs/
│   ├── PRD.md
│   ├── ARCHITECTURE.md  ← this file
│   ├── PROGRESS.md
│   ├── PROMPTS.md
│   ├── DECISIONS.md
│   └── modules/
│       ├── DASHBOARD.md
│       ├── SCANNER.md
│       ├── ANALYZER.md
│       └── CLEANUP.md
└── README.md
```

---

## 3. Class Design

### `DriveInfo.java` — model
```
DriveInfo
  - String path           e.g. "C:\" or "/dev/sda1"
  - long totalBytes
  - long usedBytes
  - long freeBytes
  - double usagePercent   calculated: (used / total) * 100
```

### `FileEntry.java` — model
```
FileEntry
  - String absolutePath
  - long sizeBytes
  - String extension      e.g. "mp4", "log", "zip"
  - boolean isDirectory
```

### `CleanupItem.java` — model
```
CleanupItem
  - String path
  - String label          e.g. "Windows Temp Files"
  - long estimatedBytes
  - String type           e.g. "temp" | "log" | "cache"
```

### `DiskManager.java` — service
```
DiskManager
  + List<DriveInfo> getAllDrives()
    Uses File.listRoots() to get drives
    Builds DriveInfo objects for each
    Skips drives where getTotalSpace() == 0

  + DriveInfo getDriveInfo(String path)
    Returns single DriveInfo for a path
```

### `FileScanner.java` — service
```
FileScanner
  + List<FileEntry> scan(String rootPath)
    Walks all files under rootPath using Files.walk()
    Collects FileEntry for each file
    Sorts by size descending
    Returns top 500 entries (to avoid memory issues on huge drives)

  + List<FileEntry> getTopLargestFiles(String rootPath, int limit)
    Calls scan(), returns first N items
```

### `StorageAnalyzer.java` — service
```
StorageAnalyzer
  + Map<String, Long> analyzeByExtension(List<FileEntry> files)
    Groups FileEntry by extension
    Sums total bytes per extension
    Returns sorted map (largest extension first)

  + long getTotalScannedSize(List<FileEntry> files)
    Sums all file sizes
```

### `DiskCleanup.java` — service
```
DiskCleanup
  + List<CleanupItem> findCleanupTargets()
    Checks known junk paths:
      Windows: %TEMP%, C:\Windows\Temp, Recycle Bin
      Mac/Linux: /tmp, ~/.cache, ~/Library/Caches
    Returns CleanupItem list with estimated sizes

  + boolean deleteItem(CleanupItem item)
    Deletes files at item.path
    Returns true if successful
    Logs failures — never throws
```

### `MainWindow.java` — UI root
```
MainWindow extends Application
  - TabPane with 4 tabs: Dashboard, Scanner, Analyzer, Cleanup
  - Loads each Pane on startup
  - Passes service instances into each pane
```

---

## 4. Key Data Flows

### Flow 1: App Launch → Drive Dashboard
```
Main.java
  → MainWindow.start()
  → DashboardPane constructor
  → DiskManager.getAllDrives()        ← reads File.listRoots()
  → Returns List<DriveInfo>
  → DashboardPane renders DriveCard   ← one card per drive
     Each card shows: path, total, used, free, ProgressBar
```

### Flow 2: User Scans a Folder
```
User types a path + clicks "Scan"
  → ScannerPane.onScanClicked()
  → Validate: path exists and is readable
  → Start background Task<List<FileEntry>> (JavaFX Task on new thread)
  → FileScanner.scan(path)
     → Files.walk() traverses all sub-directories
     → Builds FileEntry for each file
     → Sorts descending by sizeBytes
  → On completion: update TableView<FileEntry> on UI thread
  → Show total files scanned + total size
```

### Flow 3: Cleanup Suggestions → User Confirms → Delete
```
CleanupPane.initialize()
  → DiskCleanup.findCleanupTargets()
  → Renders list: label, path, estimated size, checkbox
User checks items + clicks "Delete Selected"
  → Show confirmation dialog: "Are you sure? This cannot be undone."
  → If confirmed: for each checked CleanupItem
      DiskCleanup.deleteItem(item)
      Log result (success / failed / skipped)
  → Refresh list; show summary: "Freed X GB"
```

---

## 5. Module List

| Module | Java class(es) | UI Pane |
|--------|---------------|---------|
| Dashboard | DiskManager + DriveInfo | DashboardPane.java |
| Scanner | FileScanner + FileEntry | ScannerPane.java |
| Analyzer | StorageAnalyzer | AnalyzerPane.java |
| Cleanup | DiskCleanup + CleanupItem | CleanupPane.java |

---

## 6. Error Handling Strategy

- Wrap all `File` / `Files.walk()` operations in try-catch
- On `SecurityException` or `IOException`: log the path to console, skip it, continue
- UI never shows Java stack traces — show a friendly `Alert` dialog instead
- Background scans use JavaFX `Task` so the UI thread never blocks

---

## 7. Threading Model

| Operation | Thread | Why |
|-----------|--------|-----|
| App launch | JavaFX Application Thread | Required by JavaFX |
| Drive info read | UI thread (fast, <50ms) | OK to do inline |
| Folder scan | Background thread (JavaFX Task) | Can take 10–30 seconds |
| File deletion | Background thread (JavaFX Task) | Can take several seconds |
| UI updates | Must switch back to UI thread | Use `Platform.runLater()` or Task's `updateMessage()` |
