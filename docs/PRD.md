# PRD — Disk Management System v1

**Project:** Disk Management System
**Author:** kriss2012
**Version:** 1.0
**Status:** Draft

---

## 1. Product Overview

Disk Management System is a Java desktop application that gives users a clear, real-time view of their storage devices. It shows how much space each drive is using, lets users scan folders to find what is consuming space, and suggests files that can be safely deleted to free up storage.

**In one sentence:** A tool that answers "where did all my disk space go?" and helps you get it back.

---

## 2. Target Users

- **Students** who want to understand disk usage on their PC/laptop
- **Developers** who need to monitor space usage during builds
- **General users** whose disk is running low and want to find large/junk files

The user is non-technical enough to need a clear visual display, but technical enough to run a Java application.

---

## 3. Core Features — Version 1

### Drive Overview
- List all disk drives/partitions on the system
- Show for each drive: total space, used space, free space, percentage used
- Visual progress bar per drive showing usage level

### Folder/File Scanner
- Let user select a folder path to scan
- Walk through all files and sub-folders
- Show top 10 largest files/folders within the scanned path
- Show total file count and total size of the scan

### Storage Analyzer
- Break down drive usage by file type (e.g., .mp4, .zip, .log, .exe)
- Show which extensions take the most space

### Disk Cleanup Suggestions
- Identify common junk file locations (temp folders, recycle bin, log files)
- Show estimated space that can be recovered
- Let user confirm before any deletion

### User Interface
- JavaFX window with tab-based navigation (Dashboard / Scanner / Analyzer / Cleanup)
- Progress bar for long-running scans
- Refresh button to re-read drive info

---

## 4. Out of Scope — Version 1

- Actual disk defragmentation
- Network drives / cloud storage
- Scheduled/automatic cleanup
- Multiple user accounts or login
- Mobile or web version
- Disk health S.M.A.R.T. data
- File encryption or secure delete

---

## 5. User Roles

This is a single-user desktop application. No role system needed.

- **User (the person running the app):** Can view all drive info, scan any folder they have permission to read, view cleanup suggestions, and confirm deletions.

---

## 6. Key Business Rules

- The app must NEVER delete a file without explicit user confirmation
- Read-only operations (scanning, displaying) must never require admin/root access
- All size values are displayed in human-readable format: KB / MB / GB
- If a drive or path is inaccessible, show a clear error — do not crash
- Scan of large directories must run on a background thread, never freezing the UI

---

## 7. Success Criteria

- User can see all drives and their usage within 2 seconds of launch
- User can scan a folder and see the 10 largest files in under 30 seconds for a typical home directory
- User can identify at least one folder to clean up using the Cleanup tab
- App never crashes on permission errors, empty drives, or missing paths
- Code is split into clearly named classes — a new developer can understand the structure in 5 minutes
