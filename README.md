# 💽 Disk Management System

A modern desktop application written in Java and built with JavaFX that helps users monitor, analyze, and manage storage devices efficiently. The application provides details about disk usage, large file scanning, file type aggregates, and common system junk cleanup through an intuitive, dark-themed user interface.

## 🚀 Features

- 📊 **Drive Dashboard**: Real-time disk capacity indicator cards with progress bars that change color based on utilization (green < 60%, orange < 85%, red >= 85%).
- 🔍 **Folder Scanner**: Walks folder trees asynchronously on background threads to prevent UI freezes. Displays the top 500 largest files in a sortable table.
- 📈 **Storage Analyzer**: Aggregates scan results by file type (extension) showing total size, file count, and percentage distribution.
- 🧹 **System Cleanup**: Scans OS-specific directories (temp, cache, logs, recycle bins) and safely deletes selected files with a confirmation prompt.

## 🛠️ Tech Stack & Dependencies

- **Language**: Java 17+ (JDK 25 recommended)
- **GUI Library**: JavaFX 21 (Windows x64 SDK bundled locally)
- **APIs**: Standard `java.io.File`, `java.nio.file.*` APIs with no external runtime libraries required.

## 📁 Project Structure

```text
Disk-Management/
├── docs/               # Architecture documents and specifications
│   ├── PRD.md
│   ├── ARCHITECTURE.md
│   ├── PROGRESS.md
│   ├── DECISIONS.md
│   └── modules/
│       ├── DASHBOARD.md
│       ├── SCANNER.md
│       ├── ANALYZER.md
│       └── CLEANUP.md
├── javafx-sdk-21.0.2/  # Bundled local JavaFX SDK (ignored by git)
├── src/
│   ├── Main.java
│   ├── model/          # Data transfer objects
│   │   ├── DriveInfo.java
│   │   ├── FileEntry.java
│   │   └── CleanupItem.java
│   ├── services/       # File system I/O business logic
│   │   ├── DiskManager.java
│   │   ├── FileScanner.java
│   │   ├── StorageAnalyzer.java
│   │   └── DiskCleanup.java
│   └── ui/             # JavaFX views and layouts
│       ├── MainWindow.java
│       ├── DashboardPane.java
│       ├── ScannerPane.java
│       ├── AnalyzerPane.java
│       └── CleanupPane.java
└── README.md
```

## ⚙️ Compilation & Running

We have set up the project to use a local version of JavaFX to keep compilation simple and self-contained.

### 1. Compile the Project
From the project root directory, run:
```bash
javac --module-path "javafx-sdk-21.0.2\lib" --add-modules javafx.controls,javafx.fxml src/model/*.java src/services/*.java src/ui/*.java src/*.java
```

### 2. Run the Application
From the project root directory, run:
```bash
java --module-path "javafx-sdk-21.0.2\lib" --add-modules javafx.controls,javafx.fxml -cp src Main
```

---
*Follows the 5-Phase Vibe Coding methodology from the build guides.*
