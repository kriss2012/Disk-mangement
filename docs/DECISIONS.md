# DECISIONS.md — Disk Management System
## Why we chose X over Y

Record every significant decision here as you make it.
Future you — or a teammate — will thank you.

---

## Decision 1 — JavaFX over Swing

**Chosen:** JavaFX 17
**Rejected:** Swing

**Why:**
JavaFX has a proper layout system (VBox, HBox, BorderPane) that makes building a modern-looking UI much easier than Swing's old layout managers. Progress bars, styled labels, and tab panes all look modern in JavaFX without extra work. Swing is older and harder to make look non-1998.

**Trade-off:**
JavaFX requires downloading the SDK separately (it was removed from the JDK after Java 8). Students need to add it to their classpath. Worth it for the cleaner code.

---

## Decision 2 — No Database, No External Libraries

**Chosen:** Pure Java standard library (`java.io.File`, `java.nio.file.*`)
**Rejected:** SQLite, H2, Apache Commons IO

**Why:**
Disk information is read live from the OS every time — there is nothing to store persistently. Using external libraries for file I/O would add complexity with zero benefit. The Java standard library has everything we need: `File.listRoots()`, `Files.walk()`, `Files.size()`.

**Trade-off:**
No caching of previous scan results. Every scan re-reads from disk. This is acceptable for v1.

---

## Decision 3 — JavaFX Task for Background Operations

**Chosen:** `javafx.concurrent.Task<T>` on a new Thread
**Rejected:** `SwingWorker`, raw `Thread` with `Platform.runLater()`

**Why:**
`Task` is the JavaFX-native way to do background work. It has built-in `setOnSucceeded` / `setOnFailed` callbacks that automatically run on the UI thread — no need to manually call `Platform.runLater()`. It also supports `updateMessage()` to show progress text without any extra wiring.

---

## Decision 4 — Cap Scan Results at 500 Files

**Chosen:** Return max 500 `FileEntry` objects from `FileScanner.scan()`
**Rejected:** Return everything

**Why:**
A full scan of `C:\Users` or `/home` can return 100,000+ files. Storing all of them as objects and displaying them in a `TableView` would cause the app to run out of memory or freeze the UI on slow machines. 500 is enough to identify the largest files. The user can narrow the scan path if they want more detail.

**Trade-off:**
If the 501st file is somehow huge and the first 500 aren't, we'll miss it. Acceptable for v1.

---

## Decision 5 — OS Detection at Runtime

**Chosen:** `System.getProperty("os.name")` to detect Windows vs Mac vs Linux
**Rejected:** Separate builds per OS

**Why:**
The cleanup junk paths are completely different per OS. Rather than shipping three different apps, we detect at runtime and build the correct path list. One codebase, one JAR.

---

## Decision 6 — No Deletion Without Confirmation Dialog

**Chosen:** Always show a confirmation `Alert` before any deletion
**Rejected:** Delete immediately on button click

**Why:**
Deleting files is irreversible. Even if the user clicked "Delete Selected" on purpose, the 2-second pause from the confirmation dialog prevents accidents. This is a hard rule that must never be removed, even if a user asks to skip it.

---

## Decisions to Make Later

- Should we add export to CSV (list of large files)?
- Should we add a settings screen to exclude certain folders from scanning?
- Should we add scheduled scans?
- Should we package as a `.exe` / `.dmg` using jpackage?
