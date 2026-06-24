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
import services.DiskCleanup;
import services.DiskManager;
import services.AppConfig;

public class CleanupPane extends VBox {
    private final DiskCleanup diskCleanup = new DiskCleanup();
    private final List<CleanupItem> scanResults = new ArrayList<>();

    private final Button scanBtn = new Button("Scan Junk Files");
    private final Button deleteBtn = new Button("Clean Selected Junk");
    private final Label statusLabel = new Label("Ready");
    private final ProgressBar progressBar = new ProgressBar(0);
    private final Label summaryLabel = new Label("Select items and click Clean.");

    private final VBox itemsContainer = new VBox();

    public CleanupPane() {
        this.setSpacing(15);
        this.setPadding(new Insets(25));
        this.setStyle("-fx-background-color: transparent;");

        // Title Row
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setMaxWidth(Double.MAX_VALUE);

        Label title = new Label("Junk Cleanup");
        title.setFont(Font.font("Outfit", FontWeight.BOLD, 26));
        title.getStyleClass().add("text-primary");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        scanBtn.setFont(Font.font("Outfit", FontWeight.SEMI_BOLD, 14));
        scanBtn.getStyleClass().add("btn-primary");
        scanBtn.setOnAction(e -> onScanClicked());

        header.getChildren().addAll(title, headerSpacer, scanBtn);

        // Status row
        HBox statusRow = new HBox(15);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        
        statusLabel.setFont(Font.font("Outfit", 14));
        statusLabel.getStyleClass().add("text-secondary");
        
        progressBar.setMaxWidth(200);
        progressBar.setVisible(false);
        progressBar.setStyle("-fx-accent: #ff9500;");

        statusRow.getChildren().addAll(statusLabel, progressBar);

        // Content Area ScrollPane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(itemsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        itemsContainer.setPadding(new Insets(15));
        itemsContainer.setSpacing(10);
        itemsContainer.setStyle("-fx-background-color: transparent;");

        // Footer Row
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setMaxWidth(Double.MAX_VALUE);
        footer.setPadding(new Insets(10, 0, 0, 0));

        summaryLabel.setFont(Font.font("Outfit", FontWeight.SEMI_BOLD, 14));
        summaryLabel.getStyleClass().add("text-primary");

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        deleteBtn.setFont(Font.font("Outfit", FontWeight.BOLD, 14));
        deleteBtn.setDisable(true);
        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setOnAction(e -> onDeleteClicked());

        footer.getChildren().addAll(summaryLabel, footerSpacer, deleteBtn);

        this.getChildren().addAll(header, statusRow, scrollPane, footer);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private void onScanClicked() {
        scanBtn.setDisable(true);
        deleteBtn.setDisable(true);
        itemsContainer.getChildren().clear();
        scanResults.clear();

        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        progressBar.setVisible(true);
        statusLabel.setText("Scanning system for cache, logs, and temp files...");

        Task<List<CleanupItem>> cleanupScanTask = new Task<>() {
            @Override
            protected List<CleanupItem> call() throws Exception {
                return diskCleanup.findCleanupTargets();
            }
        };

        cleanupScanTask.setOnSucceeded(e -> {
            List<CleanupItem> results = cleanupScanTask.getValue();
            scanResults.addAll(results);

            progressBar.setVisible(false);
            scanBtn.setDisable(false);

            if (results.isEmpty()) {
                statusLabel.setText("System is clean! No junk files found.");
                Label cleanMsg = new Label("🎉 System is optimized! Nothing to clean.");
                cleanMsg.setFont(Font.font("Outfit", FontWeight.BOLD, 16));
                cleanMsg.getStyleClass().add("text-primary");
                itemsContainer.getChildren().add(cleanMsg);
                updateSummary();
            } else {
                statusLabel.setText("Junk scan complete.");
                for (CleanupItem item : results) {
                    HBox row = createCleanupRow(item);
                    itemsContainer.getChildren().add(row);
                }
                deleteBtn.setDisable(false);
                updateSummary();
            }
        });

        cleanupScanTask.setOnFailed(e -> {
            progressBar.setVisible(false);
            scanBtn.setDisable(false);
            statusLabel.setText("Junk scan failed.");
            
            Throwable ex = cleanupScanTask.getException();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Scan Error");
            alert.setHeaderText(null);
            alert.setContentText("Could not scan for junk files:\n" + ex.getMessage());
            alert.showAndWait();
        });

        new Thread(cleanupScanTask).start();
    }

    private HBox createCleanupRow(CleanupItem item) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 15, 12, 15));
        row.getStyleClass().add("glass-card");

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
        label.getStyleClass().add("text-primary");

        Label pathLabel = new Label(item.getPath());
        pathLabel.setFont(Font.font("Outfit", 12));
        pathLabel.getStyleClass().add("text-secondary");

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

        // Size Label
        Label sizeLabel = new Label(DiskManager.formatSize(item.getEstimatedBytes()));
        sizeLabel.setFont(Font.font("Outfit", FontWeight.BOLD, 14));
        sizeLabel.getStyleClass().add("text-primary");

        row.getChildren().addAll(cb, labels, badge, sizeLabel);

        // Associate checkbox with the item using properties
        row.getProperties().put("checkbox", cb);
        row.getProperties().put("item", item);

        return row;
    }

    private void updateSummary() {
        long totalBytesToClean = 0;
        int selectedCount = 0;

        for (javafx.scene.Node node : itemsContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox row = (HBox) node;
                CheckBox cb = (CheckBox) row.getProperties().get("checkbox");
                CleanupItem item = (CleanupItem) row.getProperties().get("item");

                if (cb != null && cb.isSelected() && item != null) {
                    totalBytesToClean += item.getEstimatedBytes();
                    selectedCount++;
                }
            }
        }

        if (selectedCount == 0) {
            summaryLabel.setText("No items selected.");
            deleteBtn.setDisable(true);
        } else {
            summaryLabel.setText(String.format("Selected %d items (%s to free)", selectedCount, DiskManager.formatSize(totalBytesToClean)));
            deleteBtn.setDisable(false);
        }
    }

    private void onDeleteClicked() {
        // Collect items to delete
        List<CleanupItem> itemsToDelete = new ArrayList<>();
        List<HBox> rowsToDelete = new ArrayList<>();

        for (javafx.scene.Node node : itemsContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox row = (HBox) node;
                CheckBox cb = (CheckBox) row.getProperties().get("checkbox");
                CleanupItem item = (CleanupItem) row.getProperties().get("item");

                if (cb != null && cb.isSelected() && item != null) {
                    itemsToDelete.add(item);
                    rowsToDelete.add(row);
                }
            }
        }

        if (itemsToDelete.isEmpty()) return;

        // Respect config confirmation prompt
        if (AppConfig.isConfirmDelete()) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Deletion");
            confirmAlert.setHeaderText("Are you sure you want to clean selected junk files?");
            confirmAlert.setContentText("This will permanently delete " + itemsToDelete.size() + " categories of files from your drive.");
            
            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
        }

        scanBtn.setDisable(true);
        deleteBtn.setDisable(true);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        progressBar.setVisible(true);
        statusLabel.setText("Permanently deleting selected files...");

        Task<Void> deleteTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (CleanupItem item : itemsToDelete) {
                    diskCleanup.deleteItem(item);
                }
                return null;
            }
        };

        deleteTask.setOnSucceeded(e -> {
            progressBar.setVisible(false);
            scanBtn.setDisable(false);
            statusLabel.setText("Deletion complete. Space reclaimed successfully.");

            // Remove rows from UI
            itemsContainer.getChildren().removeAll(rowsToDelete);
            scanResults.removeAll(itemsToDelete);

            updateSummary();
        });

        deleteTask.setOnFailed(e -> {
            progressBar.setVisible(false);
            scanBtn.setDisable(false);
            deleteBtn.setDisable(false);
            statusLabel.setText("Deletion failed.");

            Throwable ex = deleteTask.getException();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Deletion Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred during deletion:\n" + ex.getMessage());
            alert.showAndWait();
        });

        new Thread(deleteTask).start();
    }
}
