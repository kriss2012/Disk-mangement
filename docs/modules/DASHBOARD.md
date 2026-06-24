# Module: Dashboard

**File:** `docs/modules/DASHBOARD.md`
**Java class:** `DiskManager.java` + `DriveInfo.java` + `DashboardPane.java`

---

## 1. Module Purpose

The Dashboard is the first thing the user sees when they open the app. It reads all disk drives on the system and displays a clear summary card for each one — showing total capacity, used space, free space, and a visual progress bar. This module does read-only disk access only; it never modifies anything.

---

## 2. Screens / Panels

### DashboardPane (Tab 1 in MainWindow)

**What is shown:**
- A header: "Your Drives"
- A "Refresh" button (top right)
- One DriveCard per detected drive

**Each DriveCard shows:**
- Drive path (e.g., `C:\` or `/`)
- Total space (e.g., `500.00 GB`)
- Used space (e.g., `320.50 GB`)
- Free space (e.g., `179.50 GB`)
- Usage percentage (e.g., `64.1%`)
- Progress bar filled to usage percentage
  - Green: 0–60%
  - Orange: 60–85%
  - Red: 85–100%

**User actions:**
- Click "Refresh" → re-reads all drives and redraws cards

---

## 3. Component List

| Component | Type | Purpose |
|-----------|------|---------|
| `DashboardPane` | JavaFX VBox | Outer container for the tab |
| `DriveCard` | JavaFX VBox (inner) | Card UI for one drive |
| `ProgressBar` | JavaFX ProgressBar | Visual fill bar |
| `Label` | JavaFX Label | Drive path, sizes, % text |
| `Button` (Refresh) | JavaFX Button | Triggers re-read of drives |

---

## 4. Java Methods Needed

```java
// DiskManager.java
List<DriveInfo> getAllDrives()
  // File.listRoots() → for each root:
  //   skip if getTotalSpace() == 0 (unmounted/empty)
  //   build DriveInfo(path, total, used, free, percent)
  //   return list

// DriveInfo.java
// Constructor that calculates usagePercent from total and used

// DashboardPane.java
void initialize()             // called once on tab creation
void refreshDrives()          // called by Refresh button
void renderDriveCards(List<DriveInfo> drives)  // builds card UI
String getBarColor(double percent)  // returns "green"/"orange"/"red"
String formatSize(long bytes)       // bytes → "X.XX GB" / "X.XX MB"
```

---

## 5. Validation & Edge Cases

| Case | Behaviour |
|------|-----------|
| Drive has 0 total space (unmounted) | Skip it — do not show in list |
| Only one drive (Linux /dev/sda) | Show single card, no error |
| Drive is 100% full | Progress bar red, show "0.00 GB free" |
| Permission denied reading a drive | Skip it, show warning label |
| No drives detected at all | Show "No drives found" message |

---

## 6. Build Order

1. Create `DriveInfo.java` data class with all fields + constructor
2. Implement `DiskManager.getAllDrives()` — test in console first (no UI)
3. Verify console output shows correct values for your machine
4. Create `DashboardPane.java` — static layout first (hardcoded sample data)
5. Connect real data: call `DiskManager.getAllDrives()` and render cards
6. Add color logic to ProgressBar (green/orange/red by percentage)
7. Wire up Refresh button
8. Test on a machine with multiple drives if possible
