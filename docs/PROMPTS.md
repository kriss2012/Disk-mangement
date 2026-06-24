# PROMPTS.md — Disk Management System
## AI Prompt Library · Copy, adapt, and use

---

## Prompt 0 — Start Every New AI Session With This

```
I am building a Java Disk Management System.

What it does: A desktop app (JavaFX) that shows disk drive usage,
lets users scan folders to find large files, analyzes storage by
file type, and suggests junk files to delete.

Tech stack: Java 17, JavaFX 17, java.io.File, java.nio.file.*

My existing files:
- Main.java (entry point, currently console-based)
- DiskManager.java (lists drives, shows total/used/free)
- StorageAnalyzer.java (breakdown by extension — in progress)
- FileScanner.java (walks directory tree — in progress)
- DiskCleanup.java (finds junk files — in progress)

Today I want to work on: [describe your specific task]

Before we start, confirm you understand the project and the task.
```

---

## Prompt 1 — Refactor: Fix Naming and Package Structure

```
I have a Java Disk Management project with messy file names and no package structure.

Current files:
- main.java
- Diskmanager.java
- StorageAnalizer.java   ← typo in name
- Filesanner.java        ← typo in name
- Diskcleanup.java

I want to refactor this into a clean structure:
src/
  Main.java
  DiskManager.java
  StorageAnalyzer.java
  FileScanner.java
  DiskCleanup.java
  model/
    DriveInfo.java
    FileEntry.java
    CleanupItem.java
  ui/
    MainWindow.java
    DashboardPane.java
    ScannerPane.java
    AnalyzerPane.java
    CleanupPane.java

Tasks:
1. Show me the corrected DiskManager.java (with proper class name)
2. Create DriveInfo.java with fields: path (String), totalBytes (long),
   usedBytes (long), freeBytes (long), usagePercent (double)
3. Show me Main.java updated to use DiskManager and DriveInfo

Keep it console-only for now. No JavaFX yet.
Show full file content for each file you create or change.
```

---

## Prompt 2 — Build DiskManager Service (Console Test)

```
I am building a Java Disk Management System.

I have a DriveInfo data class:
[paste DriveInfo.java]

Build DiskManager.java with:
- Method: List<DriveInfo> getAllDrives()
  - Uses File.listRoots() to get all drives
  - Skips drives where getTotalSpace() == 0
  - Builds a DriveInfo for each valid drive
  - Returns the list

- Method: String formatSize(long bytes)
  - Returns human-readable string: "X.XX GB" if >= 1GB, else "X.XX MB"

Also build a quick test in main() that calls getAllDrives() and
prints each drive's info to the console.

Show the full DiskManager.java file.
```

---

## Prompt 3 — Build FileScanner Service (Console Test)

```
I am building a Java Disk Management System.

I have this FileEntry data class:
[paste FileEntry.java]

Build FileScanner.java with:
- Method: List<FileEntry> scan(String rootPath)
  - Uses Files.walk(Paths.get(rootPath)) to traverse all files
  - For each file (not directory): create a FileEntry with:
    - absolutePath: the full path string
    - sizeBytes: Files.size(path)
    - extension: the part after the last "." (lowercase), or "(none)" if no dot
    - isDirectory: false for files
  - Wrap the walk in try-catch, skip any path that throws IOException
  - Sort the result by sizeBytes descending
  - Return up to 500 entries (to cap memory use)

- Method: List<FileEntry> getTopLargest(String rootPath, int limit)
  - Calls scan(), returns the first `limit` items

Add a main() test: scan "C:/Users" (or "/" on Mac/Linux), print
the top 10 largest files with their size.

Show the full FileScanner.java file.
```

---

## Prompt 4 — JavaFX Setup and Main Window

```
I am building a Java Disk Management System.
Tech: Java 17, JavaFX 17

I have working console services:
- DiskManager.java → returns List<DriveInfo>
- FileScanner.java → returns List<FileEntry>
- StorageAnalyzer.java → analyzeByExtension()
- DiskCleanup.java → findCleanupTargets()

Build Main.java and MainWindow.java:

Main.java:
- extends javafx.application.Application
- launch() in main()

MainWindow.java:
- extends javafx.scene.layout.BorderPane
- Creates a TabPane with 4 placeholder tabs:
  Dashboard | Scanner | Analyzer | Cleanup
- Each tab just shows a Label with the tab name for now
- Window title: "Disk Management System"
- Window size: 900 x 600

Show full file content for both files.
Do not build any real tab content yet — placeholder labels only.
```

---

## Prompt 5 — Build Dashboard UI (DashboardPane)

```
I am building a Java Disk Management System with JavaFX 17.

I have these classes ready:
- DiskManager.java → getAllDrives() returns List<DriveInfo>
- DriveInfo.java: path, totalBytes, usedBytes, freeBytes, usagePercent

Build DashboardPane.java:
- Extends VBox
- Header: "Your Drives" label (large, bold)
- "Refresh" button (top right, calls refreshDrives())
- Calls DiskManager.getAllDrives() on initialization
- For each drive, creates a "drive card" (a styled VBox) containing:
  - Drive path label (e.g. "C:\")
  - Three labels: Total: X GB, Used: X GB, Free: X GB
  - A ProgressBar set to the usagePercent / 100.0
  - A percentage label: "64.1% used"
- ProgressBar color based on usage:
  - < 60%: green (use -fx-accent: green)
  - 60-85%: orange
  - > 85%: red

Rules:
- Use JavaFX inline styles (.setStyle()) — no CSS files
- If no drives are found, show a label: "No drives detected."
- On SecurityException when reading a drive: skip it silently

Show the full DashboardPane.java file.
```

---

## Prompt 6 — Build Scanner UI (ScannerPane)

```
I am building a Java Disk Management System with JavaFX 17.

I have:
- FileScanner.java with scan(String rootPath) returning List<FileEntry>
- FileEntry.java with: absolutePath, sizeBytes, extension, isDirectory

Build ScannerPane.java:
- Extends VBox
- Top row: TextField (path input), Browse button, Scan button
- Browse button: opens DirectoryChooser, sets TextField text
- Status label below: "Ready" / "Scanning..." / results summary
- ProgressBar (indeterminate, only visible while scanning)
- TableView<FileEntry> with columns:
  - "Name" → just the filename (Paths.get(path).getFileName())
  - "Full Path" → absolutePath
  - "Size" → formatted size string
  - "Type" → "File" or "Folder"
- Summary label below table: "Found X files · Total: Y GB"

Scan button behaviour:
- Validates: path not empty, path exists, path is a directory
- Creates a JavaFX Task<List<FileEntry>> that calls FileScanner.scan()
- Shows ProgressBar while running
- On success: fills table, updates summary
- On failure: shows Alert dialog with error message

Show the full ScannerPane.java file. Use JavaFX Task for background scanning.
```

---

## Prompt 7 — Bug Fix Template

```
I am building the [DashboardPane / ScannerPane / ...] in my Java
Disk Management System.

Here is the relevant code:
[paste the method or class that has the bug]

What is happening:
[describe exactly what you see — error message, wrong output, freeze, etc.]

What I expect:
[describe what should happen]

Please:
1. Find the bug
2. Show the fix
3. Explain what was wrong in 2 sentences
```

---

## Prompt 8 — Code Review Before Demo

```
I am about to demo my Java Disk Management System.

Review this file for production readiness:
[paste file]

Check for:
1. Any System.out.println() or debug statements to remove
2. Missing null checks (especially for File operations)
3. Operations that run on the UI thread that should be on a background thread
4. Any place where an IOException or SecurityException is not caught
5. Hard-coded paths that won't work on other machines

Give me a short checklist of what to fix. Do not rewrite the code.
```

---

## Prompt 9 — Add a Feature to an Existing File

```
I have a working [ScannerPane / DashboardPane / ...] in my Disk Management System.

Here is the current code:
[paste current file]

Add this feature:
[describe the feature clearly]

Rules:
- Do NOT change any existing functionality
- Do NOT add new dependencies or imports beyond java.* and javafx.*
- Show only the lines you added or changed, not the full file
```
