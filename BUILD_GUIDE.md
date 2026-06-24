# 💽 Disk Management System — Complete Build Guide
### Following the 5-Phase Vibe Coding Process

**Repo:** https://github.com/kriss2012/Disk-mangement
**Stack:** Java 17 · JavaFX 17 · java.nio · java.io.File
**Guide follows:** The 5-Phase Vibe Coding process (PRD → Specs → Modules → Build → Review)

---

# Overview: The 5 Phases

| Phase | What You Do | Output |
|-------|------------|--------|
| 1 | Raw idea → PRD | `docs/PRD.md` |
| 2 | PRD → Architecture | `docs/ARCHITECTURE.md` |
| 3 | Architecture → Module Docs | `docs/modules/*.md` |
| 4 | Build each module with AI | Working Java code |
| 5 | Review, test, commit, log | `docs/PROGRESS.md` + clean repo |

---

# Phase 1 — PRD (Product Requirements Document)

> **See:** `docs/PRD.md` for the full document.

### What the project does (in plain language)

The Disk Management System is a Java desktop app that answers three questions:
1. How full are my drives right now?
2. What is taking up all my space?
3. What can I safely delete?

### Core Features (v1)

- **Drive Dashboard** — List all drives with used/free/total and a visual bar
- **Folder Scanner** — Pick any folder, see the biggest files inside it
- **Storage Analyzer** — See which file types (.mp4, .zip, .log) eat the most space
- **Cleanup Suggestions** — Find temp/junk files and optionally delete them

### Out of Scope (v1)

- Disk defragmentation
- Network or cloud drives
- Disk health (S.M.A.R.T.)
- Scheduled cleanup
- Mobile or web version

### Key Business Rules

1. Never delete a file without showing a confirmation dialog first
2. All scan operations run on a background thread — never freeze the UI
3. If a folder/drive is inaccessible, skip it cleanly — never crash

---

# Phase 2 — Architecture

> **See:** `docs/ARCHITECTURE.md` for the full document.

### Tech Stack

| Layer | Choice | Why |
|-------|--------|-----|
| Language | Java 17 | LTS; full File System API built-in |
| GUI | JavaFX 17 | Modern layouts; ProgressBar, TabPane, TableView |
| File I/O | `java.io.File` + `java.nio.file.*` | No external libs needed |
| Threading | JavaFX `Task<T>` | Background scans without freezing UI |
| Build | Manual javac (or Maven) | Simple for a single-developer project |

### Class Map

```
model/
  DriveInfo.java       ← path, totalBytes, usedBytes, freeBytes, usagePercent
  FileEntry.java       ← absolutePath, sizeBytes, extension, isDirectory
  CleanupItem.java     ← path, label, estimatedBytes, type

services/
  DiskManager.java     ← getAllDrives()
  FileScanner.java     ← scan(rootPath), getTopLargest()
  StorageAnalyzer.java ← analyzeByExtension(), getTotalScannedSize()
  DiskCleanup.java     ← findCleanupTargets(), deleteItem()

ui/
  MainWindow.java      ← JavaFX Application; TabPane root
  DashboardPane.java   ← Tab 1
  ScannerPane.java     ← Tab 2
  AnalyzerPane.java    ← Tab 3
  CleanupPane.java     ← Tab 4

Main.java              ← entry point; calls Application.launch()
```

### Folder Structure (Target State)

```
Disk-Management/
├── src/
│   ├── Main.java
│   ├── DiskManager.java
│   ├── StorageAnalyzer.java
│   ├── FileScanner.java
│   ├── DiskCleanup.java
│   ├── model/
│   │   ├── DriveInfo.java
│   │   ├── FileEntry.java
│   │   └── CleanupItem.java
│   └── ui/
│       ├── MainWindow.java
│       ├── DashboardPane.java
│       ├── ScannerPane.java
│       ├── AnalyzerPane.java
│       └── CleanupPane.java
├── docs/
│   ├── PRD.md
│   ├── ARCHITECTURE.md
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

# Phase 3 — Module Documents

> **See:** `docs/modules/` for the full documents for each module below.

### Module List

| # | Module | Classes | UI Pane | Module Doc |
|---|--------|---------|---------|------------|
| 1 | Dashboard | DiskManager + DriveInfo | DashboardPane | `docs/modules/DASHBOARD.md` |
| 2 | Scanner | FileScanner + FileEntry | ScannerPane | `docs/modules/SCANNER.md` |
| 3 | Analyzer | StorageAnalyzer | AnalyzerPane | `docs/modules/ANALYZER.md` |
| 4 | Cleanup | DiskCleanup + CleanupItem | CleanupPane | `docs/modules/CLEANUP.md` |

### Quick Summary of Each Module

**Module 1 — Dashboard**
Lists all drives. Shows total/used/free with a colour-coded ProgressBar (green < 60%, orange < 85%, red > 85%). Has a Refresh button. Read-only — no file system changes.

**Module 2 — Scanner**
User picks a folder path. App walks the directory tree on a background thread. Returns top 500 files sorted by size. Displays in a TableView. Shows total file count + total scanned size.

**Module 3 — Analyzer**
Takes the scan results from Module 2. Groups files by extension. Shows a sorted table of which file types use the most space. No user input needed — data flows automatically from Scanner.

**Module 4 — Cleanup**
Detects OS-specific junk file locations (temp, cache, trash, logs). Shows estimated recoverable space per item. User checks items and clicks "Delete Selected." Always shows a confirmation dialog. Background thread handles deletion.

---

# Phase 4 — Build Guide (Step by Step)

> **See:** `docs/PROMPTS.md` for copy-paste AI prompts for every task below.

## Step 0 — Fix the Existing Code First (Do This Before Anything)

The current repo has naming issues. Fix these before building further:

| Current (broken) | Should be |
|-----------------|-----------|
| `main.java` | `Main.java` |
| `Diskmanager.java` | `DiskManager.java` |
| `StorageAnalizer.java` | `StorageAnalyzer.java` (typo fixed) |
| `Filesanner.java` | `FileScanner.java` (typo fixed) |
| `Diskcleanup.java` | `DiskCleanup.java` |

**Use Prompt 1 from `docs/PROMPTS.md`** for this task.

Commit after:
```bash
git add .
git commit -m "refactor: fix class naming and file casing"
```

---

## Step 1 — Create Data Model Classes

Build these three classes first. They have no dependencies and are easy to verify.

### `DriveInfo.java`

```java
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

    // Add getters for all fields
}
```

### `FileEntry.java`

```java
public class FileEntry {
    private String absolutePath;
    private long sizeBytes;
    private String extension;
    private boolean isDirectory;

    // Constructor + getters

    public String getFileName() {
        return Paths.get(absolutePath).getFileName().toString();
    }
}
```

### `CleanupItem.java`

```java
public class CleanupItem {
    private String path;
    private String label;       // e.g. "Windows Temp Files"
    private long estimatedBytes;
    private String type;        // "temp" | "cache" | "log" | "trash"

    // Constructor + getters
}
```

Commit: `feat: add DriveInfo, FileEntry, CleanupItem data classes`

---

## Step 2 — Implement Services and Test in Console

Build and test each service in isolation before adding any UI.

### 2A — DiskManager (test it now)

```java
// At the bottom of DiskManager.java, temporarily:
public static void main(String[] args) {
    DiskManager dm = new DiskManager();
    for (DriveInfo d : dm.getAllDrives()) {
        System.out.printf("Drive: %s  Total: %.2f GB  Used: %.2f GB  Free: %.2f GB  Usage: %.1f%%%n",
            d.getPath(),
            d.getTotalBytes() / 1e9,
            d.getUsedBytes() / 1e9,
            d.getFreeBytes() / 1e9,
            d.getUsagePercent());
    }
}
```

Run it. Verify the numbers match what your OS reports. Fix any discrepancies.

### 2B — FileScanner (test it now)

```java
// Test: scan your home directory or a known folder
public static void main(String[] args) throws Exception {
    FileScanner fs = new FileScanner();
    List<FileEntry> results = fs.getTopLargest("C:/Users/YourName/Documents", 10);
    for (FileEntry f : results) {
        System.out.printf("%-60s  %,d bytes%n", f.getAbsolutePath(), f.getSizeBytes());
    }
}
```

### 2C — StorageAnalyzer (test it now)

```java
// After FileScanner works, feed its output into StorageAnalyzer
List<FileEntry> files = new FileScanner().scan("C:/some/folder");
Map<String, Long> byExt = new StorageAnalyzer().analyzeByExtension(files);
byExt.forEach((ext, bytes) ->
    System.out.printf("%-10s  %.2f MB%n", ext, bytes / 1e6));
```

### 2D — DiskCleanup (test it now — but do NOT delete anything yet)

```java
// Test: just list what would be deleted
List<CleanupItem> items = new DiskCleanup().findCleanupTargets();
for (CleanupItem item : items) {
    System.out.printf("%-30s  %s  ~%.2f GB%n",
        item.getLabel(), item.getPath(), item.getEstimatedBytes() / 1e9);
}
```

Commit after all 4 pass: `feat: implement all 4 service classes with console tests`

---

## Step 3 — JavaFX Setup

Only start JavaFX after all 4 services are working in the console.

### Download JavaFX

1. Go to https://openjfx.io
2. Download JavaFX SDK 17 for your OS
3. Extract it somewhere, e.g. `C:\javafx-sdk-17\`

### Compile command (Windows)

```bash
javac --module-path "C:\javafx-sdk-17\lib" --add-modules javafx.controls,javafx.fxml src/*.java src/model/*.java src/ui/*.java -d out/

java --module-path "C:\javafx-sdk-17\lib" --add-modules javafx.controls,javafx.fxml -cp out Main
```

### Main.java (updated for JavaFX)

```java
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        Application.launch(MainWindow.class, args);
    }
}
```

### MainWindow.java (skeleton)

```java
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainWindow extends Application {
    @Override
    public void start(Stage stage) {
        TabPane tabPane = new TabPane();

        Tab dashTab = new Tab("Dashboard", new Label("Dashboard coming soon"));
        Tab scanTab = new Tab("Scanner", new Label("Scanner coming soon"));
        Tab analyzeTab = new Tab("Analyzer", new Label("Analyzer coming soon"));
        Tab cleanTab = new Tab("Cleanup", new Label("Cleanup coming soon"));

        tabPane.getTabs().addAll(dashTab, scanTab, analyzeTab, cleanTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        stage.setScene(new Scene(tabPane, 900, 600));
        stage.setTitle("Disk Management System");
        stage.show();
    }
}
```

Verify it launches and shows 4 tabs with placeholder text.
Commit: `feat: JavaFX window with 4 tab placeholders`

---

## Step 4 — Build Dashboard UI

> **See:** `docs/modules/DASHBOARD.md` for full spec.
> **Use:** Prompt 5 from `docs/PROMPTS.md`

**Build order inside this module:**
1. Static layout with one hardcoded drive card
2. Connect `DiskManager.getAllDrives()` to render real cards
3. Add ProgressBar color logic (green/orange/red)
4. Wire Refresh button

Commit: `feat: dashboard tab with real drive data and progress bars`

---

## Step 5 — Build Scanner UI

> **See:** `docs/modules/SCANNER.md` for full spec.
> **Use:** Prompt 6 from `docs/PROMPTS.md`

**Build order inside this module:**
1. TextField + Browse button (DirectoryChooser)
2. Scan button with background Task
3. TableView with file results
4. Summary label (file count + total size)
5. Error handling (invalid path, permission denied)

Commit: `feat: scanner tab with background folder scan and file table`

---

## Step 6 — Build Analyzer UI

> **See:** `docs/modules/ANALYZER.md` for full spec.

**Build order inside this module:**
1. Placeholder ("Run a scan first…")
2. Data handoff from Scanner (wire ScannerPane → AnalyzerPane)
3. Extension breakdown table
4. Summary panel

Commit: `feat: analyzer tab showing file type breakdown from scan results`

---

## Step 7 — Build Cleanup UI

> **See:** `docs/modules/CLEANUP.md` for full spec.

**Build order inside this module:**
1. "Scan for Junk" button + background Task
2. Render cleanup list with checkboxes
3. Summary: "X items · ~Y GB recoverable"
4. Confirmation dialog + delete logic
5. Post-delete summary

⚠️ **Test deletion on a folder you create just for testing, not on your real temp folder, until you are confident the code is correct.**

Commit: `feat: cleanup tab with junk detection and confirmed deletion`

---

## Step 8 — Polish and Testing

- [ ] Remove all `System.out.println()` debug statements
- [ ] Test every tab on a fresh machine (or fresh directory)
- [ ] Test with a folder that has no read permission — verify no crash
- [ ] Test Refresh button on Dashboard
- [ ] Test Scanner with an empty folder
- [ ] Test Cleanup with nothing checked — verify button gives feedback
- [ ] Update README.md with correct setup instructions
- [ ] Verify the app compiles from a clean `git clone`

Commit: `chore: final polish, remove debug prints, update README`

---

# Phase 5 — Review, Test, Commit, Log

## Session End Checklist (Do This Every Session)

```
□ Run the app — click through every change you made today
□ Try to break it: empty inputs, weird paths, fast clicking
□ git add .
□ git commit -m "[type]: [what you did]"
□ git push
□ Update docs/PROGRESS.md (5 minutes — done / broke / next)
```

## Git Commit Message Guide

| Prefix | When to use |
|--------|------------|
| `feat:` | New feature or class |
| `fix:` | Bug fix |
| `refactor:` | Restructuring code, no new behaviour |
| `docs:` | Updating .md files |
| `style:` | UI/JavaFX styling only |
| `chore:` | Removing debug code, cleanup |
| `test:` | Adding test code |

**Always commit before a big AI rewrite:**
```bash
git add . && git commit -m "chore: safe checkpoint before refactor"
```

---

# The 7 Rules — Read These Before Every Session

| Rule | Why It Matters |
|------|---------------|
| 1 — One task per AI prompt | Focused prompts give reliable output. Vague prompts give random output. |
| 2 — Understand before accepting | If you can't explain what a line does, ask AI to explain. Don't ship code you don't understand. |
| 3 — Console test before UI | Every service class (DiskManager, FileScanner, etc.) must work in the terminal before you wire it to JavaFX. |
| 4 — Never delete without confirmation | Hard rule. Never bypass this even if a user asks. |
| 5 — Test edge cases | Empty folders, locked files, drives with 0 bytes, paths that don't exist. |
| 6 — Fresh context every session | Paste your PRD at the start of every new AI chat. Always. |
| 7 — You own the code | Be ready to explain every class in an interview. If you can't, learn it now, not on demo day. |

---

# Document Index

| File | What it contains |
|------|-----------------|
| `README.md` | Project overview, how to run, current status |
| `docs/PRD.md` | What to build, who for, what's in/out of scope |
| `docs/ARCHITECTURE.md` | Stack, class design, data flows |
| `docs/modules/DASHBOARD.md` | Full spec for the Drive Dashboard module |
| `docs/modules/SCANNER.md` | Full spec for the Folder Scanner module |
| `docs/modules/ANALYZER.md` | Full spec for the Storage Analyzer module |
| `docs/modules/CLEANUP.md` | Full spec for the Disk Cleanup module |
| `docs/PROGRESS.md` | Daily log — done / broke / next |
| `docs/PROMPTS.md` | AI prompts for every task — copy and adapt |
| `docs/DECISIONS.md` | Why you chose X over Y |

---

*The builders who win are not the ones who prompt the most.
They are the ones who think the clearest — and have the docs to prove it.*
