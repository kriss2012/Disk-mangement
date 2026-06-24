package ui;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import model.CleanupItem;
import services.DiskManager;
import services.DiskCleanup;

public class CleanupPane extends VBox {
    private final DiskCleanup diskCleanup = new DiskCleanup();
    private final List<CleanupRow> rowList = new ArrayList<>();

    private final Button scanBtn = new Button("Scan for Junk");
    private final Button deleteBtn = new Button("Delete Selected");
    private final Label statusLabel = new Label("Ready");
    private final ProgressBar progressBar = new ProgressBar(0);
    private final VBox itemsContainer = new VBox(10);
    private final Label summaryLabel = new Label("Scan to find common system junk files.");

    public CleanupPane() {
        this.setSpacing(15);
        this.setPadding(new Insets(25));
        this.setStyle("-fx-background-color: #1e1e24;");

        // Title Row
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setMaxWidth(Double.MAX_VALUE);

        Label title = new Label("Junk Cleanup");
        title.setFont(Font.font("Outfit", FontWeight.BOLD, 26));
        title.setTextFill(Color.WHITE);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        scanBtn.setFont(Font.font("Outfit", FontWeight.SEMI_BOLD, 14));
        scanBtn.setStyle(
            "-fx-background-color: #5856d6;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 8px 16px;" +
            "-fx-cursor: hand;"
        );
        scanBtn.setOnMouseEntered(e -> scanBtn.setStyle(
            "-fx-background-color: #6e6cef;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 8px 16px;" +
            "-fx-cursor: hand;"
        ));
        scanBtn.setOnMouseExited(e -> scanBtn.setStyle(
            "-fx-background-color: #5856d6;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 8px 16px;" +
            "-fx-cursor: hand;"
        ));
        scanBtn.setOnAction(e -> onScanClicked());

        header.getChildren().addAll(title, headerSpacer, scanBtn);

        // Status row
        HBox statusRow = new HBox(15);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        
        statusLabel.setFont(Font.font("Outfit", 14));
        statusLabel.setTextFill(Color.web("#a0a0a5"));
        
        progressBar.setMaxWidth(200);
        progressBar.setVisible(false);
        progressBar.setStyle("-fx-accent: #5856d6; -fx-control-inner-background: #272730; -fx-background-insets: 0;");

        statusRow.getChildren().addAll(statusLabel, progressBar);

        // Content Area ScrollPane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(itemsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #1e1e24; -fx-border-color: #3a3a3c; -fx-border-radius: 8px; -fx-background-radius: 8px;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        itemsContainer.setPadding(new Insets(15));
        itemsContainer.setSpacing(10);
        itemsContainer.setStyle("-fx-background-color: #1e1e24;");

        // Footer Row
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setMaxWidth(Double.MAX_VALUE);
        footer.setPadding(new Insets(10, 0, 0, 0));

        summaryLabel.setFont(Font.font("Outfit", FontWeight.SEMI_BOLD, 14));
        summaryLabel.setTextFill(Color.WHITE);

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        deleteBtn.setFont(Font.font("Outfit", FontWeight.BOLD, 14));
        deleteBtn.setDisable(true);
        deleteBtn.setStyle(
            "-fx-background-color: #ff3b30;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 10px 20px;" +
            "-fx-cursor: hand;"
        );
        deleteBtn.setOnMouseEntered(e -> {
            if (!deleteBtn.isDisable()) {
                deleteBtn.setStyle(
                    "-fx-background-color: #ff5e55;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-padding: 10px 20px;" +
                    "-fx-cursor: hand;"
                );
            }
        });
        deleteBtn.setOnMouseExited(e -> {
            if (!deleteBtn.isDisable()) {
                deleteBtn.setStyle(
                    "-fx-background-color: #ff3b30;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 8px;" +
                    "-fx-padding: 10px 20px;" +
                    "-fx-cursor: hand;"
                );
            }
        });
        deleteBtn.setOnAction(e -> onDeleteClicked());

        footer.getChildren().addAll(summaryLabel, footerSpacer, deleteBtn);

        this.getChildren().addAll(header, statusRow, scrollPane, footer);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private void onScanClicked() {
        scanBtn.setDisable(true);
        deleteBtn.setDisable(true);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        progressBar.setVisible(true);
        statusLabel.setText("Scanning for junk files...");
        statusLabel.setTextFill(Color.web("#a0a0a5"));

        itemsContainer.getChildren().clear();
        rowList.clear();

        Task<List<CleanupItem>> scanTask = new Task<>() {
            @Override
            protected List<CleanupItem> call() throws Exception {
                return diskCleanup.findCleanupTargets();
            }
        };

        scanTask.setOnSucceeded(e -> {
            List<CleanupItem> results = scanTask.getValue();
            progressBar.setVisible(false);
            scanBtn.setDisable(false);

            if (results.isEmpty()) {
                Label noJunk = new Label("Your system is clean! No common junk folders found.");
                noJunk.setFont(Font.font("Outfit", 15));
                noJunk.setTextFill(Color.web("#34c759"));
                itemsContainer.getChildren().add(noJunk);
                summaryLabel.setText("0 items found.");
                statusLabel.setText("Scan completed.");
                statusLabel.setTextFill(Color.web("#34c759"));
            } else {
                for (CleanupItem item : results) {
                    HBox itemRow = createCleanupRow(item);
                    itemsContainer.getChildren().add(itemRow);
                }
                statusLabel.setText("Junk scan completed.");
                statusLabel.setTextFill(Color.web("#34c759"));
                updateSummary();
            }
        });

        scanTask.setOnFailed(e -> {
            progressBar.setVisible(false);
            scanBtn.setDisable(false);
            statusLabel.setText("Scan failed.");
            statusLabel.setTextFill(Color.web("#ff3b30"));
            showErrorAlert("Scan Error", "Could not scan for junk files:\n" + scanTask.getException().getMessage());
        });

        new Thread(scanTask).start();
    }

    private HBox createCleanupRow(CleanupItem item) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 15, 12, 15));
        row.setStyle(
            "-fx-background-color: #272730;" +
            "-fx-background-radius: 8px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );

        row.setOnMouseEntered(e -> row.setStyle(
            "-fx-background-color: #31313c;" +
            "-fx-background-radius: 8px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 5, 0, 0, 2);"
        ));
        row.setOnMouseExited(e -> row.setStyle(
            "-fx-background-color: #272730;" +
            "-fx-background-radius: 8px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        ));

        // CheckBox
        CheckBox cb = new CheckBox();
        cb.setSelected(true); // checked by default
        cb.setStyle("-fx-cursor: hand;");
        cb.setOnAction(e -> updateSummary());

        // Labels
        VBox labels = new VBox(4);
        HBox.setHgrow(labels, Priority.ALWAYS);

        Label label = new Label(item.getLabel());
        label.setFont(Font.font("Outfit", FontWeight.BOLD, 15));
        label.setTextFill(Color.WHITE);

        Label pathLabel = new Label(item.getPath());
        pathLabel.setFont(Font.font("Outfit", 12));
        pathLabel.setTextFill(Color.web("#a0a0a5"));

        labels.getChildren().addAll(label, pathLabel);

        // Badge
        Label badge = new Label(item.getType().toUpperCase());
        badge.setFont(Font.font("Outfit", FontWeight.BOLD, 10));
        
        String badgeColor;
        switch (item.getType()) {
            case "temp": badgeColor = "#ff9500"; break;
            case "cache": badgeColor = "#5856d6"; break;
            case "log": badgeColor = "#34c759"; break;
            case "trash": badgeColor = "#ff3b30"; break;
            default: badgeColor = "#a0a0a5";
        }
        badge.setStyle(
            "-fx-text-fill: " + badgeColor + ";" +
            "-fx-border-color: " + badgeColor + ";" +
            "-fx-border-radius: 4px;" +
            "-fx-padding: 3px 6px;"
        );

        // Size
        Label sizeLabel = new Label(DiskManager.formatSize(item.getEstimatedBytes()));
        sizeLabel.setFont(Font.font("Outfit", FontWeight.BOLD, 14));
        sizeLabel.setTextFill(Color.WHITE);
        sizeLabel.setPrefWidth(90);
        sizeLabel.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(cb, labels, badge, sizeLabel);

        rowList.add(new CleanupRow(cb, item));
        return row;
    }

    private void updateSummary() {
        int count = 0;
        long totalBytes = 0;
        for (CleanupRow row : rowList) {
            if (row.checkBox.isSelected()) {
                count++;
                totalBytes += row.item.getEstimatedBytes();
            }
        }

        summaryLabel.setText(String.format("%d items selected · ~%s recoverable", count, DiskManager.formatSize(totalBytes)));
        deleteBtn.setDisable(count == 0);
    }

    private void onDeleteClicked() {
        List<CleanupItem> selectedItems = new ArrayList<>();
        long totalBytes = 0;
        for (CleanupRow row : rowList) {
            if (row.checkBox.isSelected()) {
                selectedItems.add(row.item);
                totalBytes += row.item.getEstimatedBytes();
            }
        }

        if (selectedItems.isEmpty()) return;

        // Show Confirmation Dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Delete Junk Files?");
        confirm.setContentText(String.format("You are about to delete %d items (~%s). This cannot be undone. Are you sure you want to proceed?", 
                selectedItems.size(), DiskManager.formatSize(totalBytes)));
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                executeDeletion(selectedItems);
            }
        });
    }

    private void executeDeletion(List<CleanupItem> targets) {
        scanBtn.setDisable(true);
        deleteBtn.setDisable(true);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        progressBar.setVisible(true);
        statusLabel.setText("Deleting selected junk files...");
        statusLabel.setTextFill(Color.web("#ff9500"));

        Task<DeletionResult> deleteTask = new Task<>() {
            @Override
            protected DeletionResult call() throws Exception {
                long freedBytes = 0;
                int filesCount = 0;
                int failedCount = 0;

                for (CleanupItem target : targets) {
                    long sizeBefore = diskCleanup.calculateFolderSize(new File(target.getPath()));
                    boolean success = diskCleanup.deleteItem(target);
                    long sizeAfter = diskCleanup.calculateFolderSize(new File(target.getPath()));
                    
                    long freed = sizeBefore - sizeAfter;
                    if (freed > 0) {
                        freedBytes += freed;
                    }
                    if (!success) {
                        failedCount++;
                    }
                }
                return new DeletionResult(freedBytes, failedCount);
            }
        };

        deleteTask.setOnSucceeded(e -> {
            DeletionResult result = deleteTask.getValue();
            progressBar.setVisible(false);
            scanBtn.setDisable(false);

            String statusMsg = String.format("Deletion complete. Freed: %s", DiskManager.formatSize(result.freedBytes));
            if (result.failedCount > 0) {
                statusMsg += String.format(" (%d folders had items skipped)", result.failedCount);
            }

            statusLabel.setText(statusMsg);
            statusLabel.setTextFill(Color.web("#34c759"));

            Alert resultAlert = new Alert(Alert.AlertType.INFORMATION);
            resultAlert.setTitle("Cleanup Complete");
            resultAlert.setHeaderText(null);
            resultAlert.setContentText(String.format("Successfully completed cleanup! Saved %s of storage space.", 
                    DiskManager.formatSize(result.freedBytes)));
            resultAlert.showAndWait();

            // Refresh items list
            onScanClicked();
        });

        deleteTask.setOnFailed(e -> {
            progressBar.setVisible(false);
            scanBtn.setDisable(false);
            statusLabel.setText("Deletion failed.");
            statusLabel.setTextFill(Color.web("#ff3b30"));
            showErrorAlert("Deletion Error", "Could not complete file deletion:\n" + deleteTask.getException().getMessage());
        });

        new Thread(deleteTask).start();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private static class CleanupRow {
        final CheckBox checkBox;
        final CleanupItem item;

        CleanupRow(CheckBox checkBox, CleanupItem item) {
            this.checkBox = checkBox;
            this.item = item;
        }
    }

    private static class DeletionResult {
        final long freedBytes;
        final int failedCount;

        DeletionResult(long freedBytes, int failedCount) {
            this.freedBytes = freedBytes;
            this.failedCount = failedCount;
        }
    }
}
