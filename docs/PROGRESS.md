# PROGRESS.md — Disk Management System

Track every coding session here. 5 minutes of logging saves 2 hours of confusion.

---

## Template — Copy this for each session

```
## YYYY-MM-DD (Day)

### Done
- 

### Broke / Fixed
- 

### Blocked
- 

### Committed
- 

### Tomorrow
- [ ] 
```

---

## Session Log

## 2025-XX-XX (Day 1)

### Done
- Reviewed existing code: main.java, DiskManager.java, StorageAnalizer.java, Filesanner.java, Diskcleanup.java
- Set up docs/ folder structure
- Created PRD.md, ARCHITECTURE.md, module docs

### Broke / Fixed
- Noticed: class names in repo don't match README (e.g. `StorageAnalizer` not `StorageAnalyzer`)
  Fix: Rename to correct spelling in next session

### Blocked
- JavaFX setup not done yet. Need to install JavaFX SDK.

### Committed
- `docs: add PRD, ARCHITECTURE, and module documents`

### Tomorrow
- [ ] Fix class naming inconsistencies (typos in repo)
- [ ] Refactor existing code into model/ and service/ package structure
- [ ] Create DriveInfo.java data class
- [ ] Test DiskManager console output on your machine

---

## Roadmap — What to Build and in What Order

| Phase | What | Files | Status |
|-------|------|-------|--------|
| 0 | Fix naming + folder structure | All existing .java | ⬜ Todo |
| 1 | Data models | DriveInfo.java, FileEntry.java, CleanupItem.java | ⬜ Todo |
| 2 | Console-test all services | DiskManager, FileScanner, StorageAnalyzer, DiskCleanup | ⬜ Todo |
| 3 | JavaFX setup | Main.java, MainWindow.java | ⬜ Todo |
| 4 | Dashboard UI | DashboardPane.java | ⬜ Todo |
| 5 | Scanner UI | ScannerPane.java | ⬜ Todo |
| 6 | Analyzer UI | AnalyzerPane.java | ⬜ Todo |
| 7 | Cleanup UI | CleanupPane.java | ⬜ Todo |
| 8 | Polish + testing | All | ⬜ Todo |
