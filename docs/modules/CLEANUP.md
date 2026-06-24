# Module: Cleanup

**File:** `docs/modules/CLEANUP.md`
**Java classes:** `DiskCleanup.java` + `CleanupItem.java` + `CleanupPane.java`

---

## 1. Module Purpose

The Cleanup module identifies known junk file locations (temporary files, system caches, log files) and shows the user how much space they can recover. The user selects which items to delete and clicks "Delete Selected." This is the only module in the app that modifies the file system, so it requires an explicit confirmation dialog before any deletion happens.

---

## 2. Screens / Panels

### CleanupPane (Tab 4 in MainWindow)

**What is shown:**
- A "Scan for Junk" button
- A list of cleanup targets, each showing:
  - Checkbox (to select for deletion)
  - Label (e.g., "Windows Temp Files")
  - Path (e.g., `C:\Users\...\AppData\Local\Temp`)
  - Estimated size (e.g., `1.24 GB`)
  - Type badge (Temp / Cache / Log)
- Summary at bottom: "X items selected · ~Y GB recoverable"
- A red "Delete Selected" button
- A result label after deletion: "Freed Z GB · X files deleted"

**User actions:**
- Click "Scan for Junk" → app checks known paths, shows list
- Check/uncheck individual items
- Click "Delete Selected" → confirmation dialog → deletion → refresh list

---

## 3. Known Junk Locations to Check

The app detects the OS and checks OS-specific paths:

**Windows:**
```
%TEMP%                           → User temp files
C:\Windows\Temp                  → System temp files
C:\$Recycle.Bin                  → Recycle bin content
C:\Windows\SoftwareDistribution\Download  → Windows Update cache
```

**macOS:**
```
/private/var/folders/...         → System temp (use System.getenv("TMPDIR"))
~/Library/Caches                 → App caches
~/.Trash                         → Trash contents
~/Downloads (files >30 days old) → Old downloads (flag, not auto-delete)
```

**Linux:**
```
/tmp                             → Temp files
~/.cache                         → App caches
~/.local/share/Trash             → Trash contents
/var/log (files >7 days old)    → Old logs (flag only)
```

---

## 4. Java Methods Needed

```java
// DiskCleanup.java
List<CleanupItem> findCleanupTargets()
  // Detect OS: System.getProperty("os.name")
  // Build list of CleanupItem for known junk paths
  // For each path: check if it exists, calculate size (recursive)
  // Skip paths that don't exist or can't be read
  // Return list sorted by estimatedBytes descending

long calculateFolderSize(File folder)
  // Recursive sum of all file sizes in a directory tree
  // Returns 0 if folder is null, doesn't exist, or throws exception

boolean deleteItem(CleanupItem item)
  // Delete all files at item.path (recursive for directories)
  // Return true if all deletions succeeded
  // Return false if any file failed (log the failure)
  // NEVER throw — always catch and log

// CleanupItem.java
// Fields: path, label, estimatedBytes, type (temp/cache/log/trash)

// CleanupPane.java
void onScanClicked()
  // Background Task: DiskCleanup.findCleanupTargets()
  // Show results in list with checkboxes

void onDeleteClicked()
  // Get checked items
  // Show confirmation dialog:
  //   "You are about to delete X items (~Y GB). This cannot be undone."
  //   OK / Cancel
  // If OK: background Task to delete each item
  // On complete: refresh list, show summary

void renderCleanupList(List<CleanupItem> items)
  // Build a row for each item with checkbox, labels, size
```

---

## 5. Validation & Edge Cases

| Case | Behaviour |
|------|-----------|
| No junk found | Show "Your system looks clean! No common junk files found." |
| Path no longer exists when deleting | Log it, count as "already deleted", continue |
| File is locked (in use by another process) | Skip it, log it, show warning count at end |
| User clicks Delete with nothing checked | Show "Please select at least one item." |
| Deletion partially fails | Show "Freed X GB · Y items skipped (in use or protected)" |
| System temp folder is protected (e.g. Windows) | Skip gracefully — do not show items user cannot delete |

---

## 6. Safety Rules (Must Be Followed)

1. **Never delete without confirmation.** Always show a dialog.
2. **Never delete files outside the known junk list.** Do not let users browse to arbitrary paths.
3. **Never delete system files.** Skip anything in `C:\Windows\System32`, `/bin`, `/usr/lib`, etc.
4. **Log every deletion.** Write to `System.out` at minimum: `"Deleted: [path]"` or `"Failed: [path]"`.
5. **Test deletion on a temp folder you create yourself first.** Never test on real system temp on first run.

---

## 7. Build Order

1. Create `CleanupItem.java` data class
2. Implement `DiskCleanup.calculateFolderSize()` — test in console
3. Implement `DiskCleanup.findCleanupTargets()` — test in console, verify sizes
4. Create `CleanupPane.java` — static layout with placeholder data (hardcoded)
5. Wire up "Scan for Junk" button with background Task
6. Render real cleanup items in the list
7. Implement checkbox tracking and "Delete Selected" button
8. Add confirmation dialog before deletion
9. Implement `DiskCleanup.deleteItem()` — test on a throwaway temp folder first
10. Wire deletion to the button; show result summary
