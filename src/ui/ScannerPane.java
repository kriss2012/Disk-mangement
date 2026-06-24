package ui;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import java.io.File;
import java.util.List;
import model.FileEntry;
import services.DiskManager;
import services.FileScanner;

public class ScannerPane extends VBox {
    private final FileScanner fileScanner = new FileScanner();
    private AnalyzerPane analyzerPane;

    private final TextField pathField = new TextField();
    private final Button browseBtn = new Button("Browse...");
    private final Button scanBtn = new Button("Scan");
    private final Label statusLabel = new Label("Ready");
    private final ProgressBar progressBar = new ProgressBar(0);
    private final TableView<FileEntry> tableView = new TableView<>();
    private final Label summaryLabel = new Label("No active scan results.");

    public ScannerPane() {
        this.setSpacing(15);
        this.setPadding(new Insets(25));
        this.setStyle("-fx-background-color: #1e1e24;");

        // Title
        Label title = new Label("Folder Scanner");
        title.setFont(Font.font("Outfit", FontWeight.BOLD, 26));
        title.setTextFill(Color.WHITE);

        // Input row
        HBox inputRow = new HBox(10);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        pathField.setPromptText("Enter or select a folder path to scan...");
        pathField.setFont(Font.font("Outfit", 14));
        pathField.setStyle(
            "-fx-background-color: #272730;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 10px 14px;" +
            "-fx-border-color: #3a3a3c;" +
            "-fx-border-radius: 8px;"
        );
        HBox.setHgrow(pathField, Priority.ALWAYS);

        browseBtn.setFont(Font.font("Outfit", FontWeight.SEMI_BOLD, 14));
        browseBtn.setStyle(
            "-fx-background-color: #3a3a3c;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 10px 18px;" +
            "-fx-cursor: hand;"
        );
        browseBtn.setOnMouseEntered(e -> browseBtn.setStyle(
            "-fx-background-color: #4a4a4c;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 10px 18px;" +
            "-fx-cursor: hand;"
        ));
        browseBtn.setOnMouseExited(e -> browseBtn.setStyle(
            "-fx-background-color: #3a3a3c;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 10px 18px;" +
            "-fx-cursor: hand;"
        ));
        browseBtn.setOnAction(e -> onBrowseClicked());

        scanBtn.setFont(Font.font("Outfit", FontWeight.BOLD, 14));
        scanBtn.setStyle(
            "-fx-background-color: #5856d6;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 10px 24px;" +
            "-fx-cursor: hand;"
        );
        scanBtn.setOnMouseEntered(e -> scanBtn.setStyle(
            "-fx-background-color: #6e6cef;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 10px 24px;" +
            "-fx-cursor: hand;"
        ));
        scanBtn.setOnMouseExited(e -> scanBtn.setStyle(
            "-fx-background-color: #5856d6;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 10px 24px;" +
            "-fx-cursor: hand;"
        ));
        scanBtn.setOnAction(e -> onScanClicked());

        inputRow.getChildren().addAll(pathField, browseBtn, scanBtn);

        // Status row with progress indicators
        HBox statusRow = new HBox(15);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        
        statusLabel.setFont(Font.font("Outfit", 14));
        statusLabel.setTextFill(Color.web("#a0a0a5"));
        
        progressBar.setMaxWidth(200);
        progressBar.setVisible(false);
        progressBar.setStyle("-fx-accent: #5856d6; -fx-control-inner-background: #272730; -fx-background-insets: 0;");

        statusRow.getChildren().addAll(statusLabel, progressBar);

        // Setup Table
        setupTableView();

        // Summary Label
        summaryLabel.setFont(Font.font("Outfit", FontWeight.SEMI_BOLD, 14));
        summaryLabel.setTextFill(Color.WHITE);
        HBox summaryRow = new HBox(summaryLabel);
        summaryRow.setPadding(new Insets(10, 0, 0, 0));

        this.getChildren().addAll(title, inputRow, statusRow, tableView, summaryRow);
        VBox.setVgrow(tableView, Priority.ALWAYS);
    }

    public void setAnalyzerPane(AnalyzerPane analyzerPane) {
        this.analyzerPane = analyzerPane;
    }

    private void setupTableView() {
        TableColumn<FileEntry, String> nameCol = new TableColumn<>("File Name");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFileName()));
        nameCol.setPrefWidth(220);

        TableColumn<FileEntry, String> pathCol = new TableColumn<>("Full Path");
        pathCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAbsolutePath()));
        pathCol.setPrefWidth(400);

        TableColumn<FileEntry, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(DiskManager.formatSize(data.getValue().getSizeBytes())));
        sizeCol.setPrefWidth(120);

        TableColumn<FileEntry, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().isDirectory() ? "Folder" : "File"));
        typeCol.setPrefWidth(80);

        tableView.getColumns().addAll(nameCol, pathCol, sizeCol, typeCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Styling the table for premium look
        tableView.setStyle(
            "-fx-background-color: #272730;" +
            "-fx-control-inner-background: #272730;" +
            "-fx-table-cell-border-color: #3a3a3c;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8px;"
        );

        // Row interaction (Copy path to clipboard on click)
        tableView.setRowFactory(tv -> {
            TableRow<FileEntry> row = new TableRow<>();
            row.setStyle("-fx-background-color: #272730; -fx-text-fill: white; -fx-border-color: transparent;");
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    FileEntry clickedRow = row.getItem();
                    javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                    javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                    content.putString(clickedRow.getAbsolutePath());
                    clipboard.setContent(content);
                    statusLabel.setText("Copied path to clipboard: " + clickedRow.getFileName());
                }
            });
            return row;
        });
    }

    private void onBrowseClicked() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Folder to Scan");
        File defaultDir = new File(pathField.getText().trim());
        if (defaultDir.exists() && defaultDir.isDirectory()) {
            chooser.setInitialDirectory(defaultDir);
        } else {
            String userHome = System.getProperty("user.home");
            if (userHome != null) {
                chooser.setInitialDirectory(new File(userHome));
            }
        }
        File selectedDirectory = chooser.showDialog(this.getScene().getWindow());
        if (selectedDirectory != null) {
            pathField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    private void onScanClicked() {
        String path = pathField.getText().trim();

        // Validations
        if (path.isEmpty()) {
            showValidationWarning("Please enter or select a folder path.");
            return;
        }

        File folder = new File(path);
        if (!folder.exists()) {
            showValidationWarning("Folder not found. Check the path.");
            return;
        }

        if (!folder.isDirectory()) {
            showValidationWarning("Please select a folder, not a file.");
            return;
        }

        if (!folder.canRead()) {
            showValidationWarning("Cannot read this folder. Permission denied.");
            return;
        }

        // Disable UI controls during scan
        scanBtn.setDisable(true);
        browseBtn.setDisable(true);
        pathField.setDisable(true);

        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        progressBar.setVisible(true);
        statusLabel.setText("Scanning directory tree...");
        statusLabel.setTextFill(Color.web("#a0a0a5"));

        // Setup background Task
        Task<List<FileEntry>> scanTask = new Task<>() {
            @Override
            protected List<FileEntry> call() throws Exception {
                return fileScanner.scan(path);
            }
        };

        scanTask.setOnSucceeded(e -> {
            List<FileEntry> results = scanTask.getValue();
            tableView.getItems().setAll(results);

            // Calculate totals
            long totalSize = 0;
            for (FileEntry entry : results) {
                totalSize += entry.getSizeBytes();
            }

            statusLabel.setText("Scan completed successfully.");
            statusLabel.setTextFill(Color.web("#34c759"));
            progressBar.setVisible(false);

            summaryLabel.setText(String.format("Found %,d files · Total Size: %s", results.size(), DiskManager.formatSize(totalSize)));

            // Enable controls
            scanBtn.setDisable(false);
            browseBtn.setDisable(false);
            pathField.setDisable(false);

            // Hand off results to AnalyzerPane
            if (analyzerPane != null) {
                analyzerPane.loadData(results);
            }
        });

        scanTask.setOnFailed(e -> {
            progressBar.setVisible(false);
            scanBtn.setDisable(false);
            browseBtn.setDisable(false);
            pathField.setDisable(false);

            statusLabel.setText("Scan failed.");
            statusLabel.setTextFill(Color.web("#ff3b30"));

            Throwable ex = scanTask.getException();
            showErrorAlert("Scan Error", "An error occurred while scanning:\n" + ex.getMessage());
        });

        new Thread(scanTask).start();
    }

    private void showValidationWarning(String message) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.web("#ff9500"));
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
