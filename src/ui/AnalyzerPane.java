package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import model.FileEntry;
import services.DiskManager;
import services.StorageAnalyzer;

public class AnalyzerPane extends StackPane {
    private final StorageAnalyzer storageAnalyzer = new StorageAnalyzer();

    private final VBox placeholder = new VBox(15);
    private final HBox mainLayout = new HBox(20);
    private final TableView<ExtensionRow> tableView = new TableView<>();

    // Summary elements
    private final Label totalSizeVal = new Label("-");
    private final Label uniqueTypesVal = new Label("-");
    private final VBox topExtsList = new VBox(8);

    public AnalyzerPane() {
        this.setStyle("-fx-background-color: transparent;");
        this.setPadding(new Insets(25));

        // Setup Placeholder
        setupPlaceholder();

        // Setup Main Layout
        setupMainLayout();

        this.getChildren().addAll(placeholder, mainLayout);
        showPlaceholder(true);
    }

    private void setupPlaceholder() {
        placeholder.setAlignment(Pos.CENTER);
        
        Label icon = new Label("📊");
        icon.setFont(Font.font(60));
        
        Label msg = new Label("No Scan Data Available");
        msg.setFont(Font.font("Outfit", FontWeight.BOLD, 20));
        msg.setTextFill(Color.WHITE);

        Label subMsg = new Label("Run a folder scan first under the 'Scanner' tab to view storage analysis.");
        subMsg.setFont(Font.font("Outfit", 14));
        subMsg.setTextFill(Color.web("#a0a0a5"));

        placeholder.getChildren().addAll(icon, msg, subMsg);
    }

    private void setupMainLayout() {
        mainLayout.setAlignment(Pos.CENTER);
        HBox.setHgrow(mainLayout, Priority.ALWAYS);

        // Left Pane: TableView
        VBox leftPane = new VBox(15);
        HBox.setHgrow(leftPane, Priority.ALWAYS);

        Label tableTitle = new Label("Extension Breakdown");
        tableTitle.setFont(Font.font("Outfit", FontWeight.BOLD, 20));
        tableTitle.setTextFill(Color.WHITE);

        setupTableView();
        leftPane.getChildren().addAll(tableTitle, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        // Right Pane: Summary Card
        VBox rightPane = new VBox(20);
        rightPane.setPrefWidth(300);
        rightPane.setMinWidth(280);
        rightPane.setPadding(new Insets(20));
        rightPane.setStyle(
            "-fx-background-color: #272730;" +
            "-fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 4);"
        );

        Label summaryTitle = new Label("Storage Stats");
        summaryTitle.setFont(Font.font("Outfit", FontWeight.BOLD, 18));
        summaryTitle.setTextFill(Color.WHITE);

        // Total Size Stat
        VBox sizeStat = new VBox(4);
        Label totalSizeLbl = new Label("TOTAL SCANNED SIZE");
        totalSizeLbl.setFont(Font.font("Outfit", FontWeight.BOLD, 11));
        totalSizeLbl.setTextFill(Color.web("#a0a0a5"));
        totalSizeVal.setFont(Font.font("Outfit", FontWeight.BOLD, 22));
        totalSizeVal.setTextFill(Color.web("#5856d6"));
        sizeStat.getChildren().addAll(totalSizeLbl, totalSizeVal);

        // Unique Extensions Stat
        VBox typesStat = new VBox(4);
        Label uniqueTypesLbl = new Label("UNIQUE FILE TYPES");
        uniqueTypesLbl.setFont(Font.font("Outfit", FontWeight.BOLD, 11));
        uniqueTypesLbl.setTextFill(Color.web("#a0a0a5"));
        uniqueTypesVal.setFont(Font.font("Outfit", FontWeight.BOLD, 22));
        uniqueTypesVal.setTextFill(Color.WHITE);
        typesStat.getChildren().addAll(uniqueTypesLbl, uniqueTypesVal);

        // Divider line
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: #3a3a3c;");

        // Top Extensions list
        VBox topExtsBox = new VBox(10);
        Label topExtsLbl = new Label("TOP EXTENSIONS BY SIZE");
        topExtsLbl.setFont(Font.font("Outfit", FontWeight.BOLD, 11));
        topExtsLbl.setTextFill(Color.web("#a0a0a5"));
        topExtsBox.getChildren().addAll(topExtsLbl, topExtsList);

        rightPane.getChildren().addAll(summaryTitle, sizeStat, typesStat, divider, topExtsBox);

        mainLayout.getChildren().addAll(leftPane, rightPane);
    }

    private void setupTableView() {
        TableColumn<ExtensionRow, String> extCol = new TableColumn<>("Extension");
        extCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getExtension()));
        extCol.setPrefWidth(100);

        TableColumn<ExtensionRow, String> sizeCol = new TableColumn<>("Total Size");
        sizeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(DiskManager.formatSize(data.getValue().getTotalBytes())));
        sizeCol.setPrefWidth(120);

        TableColumn<ExtensionRow, Integer> countCol = new TableColumn<>("File Count");
        countCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getFileCount()));
        countCol.setPrefWidth(100);

        TableColumn<ExtensionRow, Double> pctCol = new TableColumn<>("Percentage");
        pctCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPercent()));
        pctCol.setCellFactory(column -> new TableCell<>() {
            private final ProgressBar bar = new ProgressBar();
            private final Label label = new Label();
            private final HBox container = new HBox(8);

            {
                container.setAlignment(Pos.CENTER_LEFT);
                bar.setPrefWidth(80);
                bar.setStyle("-fx-accent: #5856d6; -fx-control-inner-background: #1e1e24; -fx-background-insets: 0;");
                label.setFont(Font.font("Outfit", 12));
                label.setTextFill(Color.WHITE);
                container.getChildren().addAll(bar, label);
            }

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    bar.setProgress(item / 100.0);
                    label.setText(String.format("%.1f%%", item));
                    setGraphic(container);
                }
            }
        });
        pctCol.setPrefWidth(180);

        tableView.getColumns().addAll(extCol, sizeCol, countCol, pctCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tableView.setStyle(
            "-fx-background-color: #272730;" +
            "-fx-control-inner-background: #272730;" +
            "-fx-table-cell-border-color: #3a3a3c;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8px;"
        );

        tableView.setRowFactory(tv -> new TableRow<>() {
            {
                setStyle("-fx-background-color: #272730; -fx-text-fill: white; -fx-border-color: transparent;");
            }
        });
    }

    public void loadData(List<FileEntry> scanResults) {
        if (scanResults == null || scanResults.isEmpty()) {
            showPlaceholder(true);
            return;
        }

        showPlaceholder(false);

        long totalSize = storageAnalyzer.getTotalScannedSize(scanResults);
        int uniqueExts = storageAnalyzer.countUniqueExtensions(scanResults);

        totalSizeVal.setText(DiskManager.formatSize(totalSize));
        uniqueTypesVal.setText(String.valueOf(uniqueExts));

        Map<String, Long> sizeMap = storageAnalyzer.analyzeByExtension(scanResults);
        Map<String, Integer> countMap = storageAnalyzer.countByExtension(scanResults);

        List<ExtensionRow> rows = new ArrayList<>();
        topExtsList.getChildren().clear();

        int topCount = 0;
        for (Map.Entry<String, Long> entry : sizeMap.entrySet()) {
            String ext = entry.getKey();
            long size = entry.getValue();
            int count = countMap.getOrDefault(ext, 0);
            double pct = totalSize > 0 ? ((double) size / totalSize) * 100.0 : 0.0;

            rows.add(new ExtensionRow(ext, size, count, pct));

            // Populate top 3 on the right
            if (topCount < 3) {
                HBox extRow = new HBox(10);
                extRow.setAlignment(Pos.CENTER_LEFT);
                
                Label nameLbl = new Label(ext);
                nameLbl.setFont(Font.font("Outfit", FontWeight.BOLD, 13));
                nameLbl.setTextFill(Color.WHITE);
                
                Region extSpacer = new Region();
                HBox.setHgrow(extSpacer, Priority.ALWAYS);
                
                Label valLbl = new Label(DiskManager.formatSize(size));
                valLbl.setFont(Font.font("Outfit", 13));
                valLbl.setTextFill(Color.web("#a0a0a5"));
                
                extRow.getChildren().addAll(nameLbl, extSpacer, valLbl);
                topExtsList.getChildren().add(extRow);
                topCount++;
            }
        }

        tableView.getItems().setAll(rows);
    }

    private void showPlaceholder(boolean show) {
        placeholder.setVisible(show);
        placeholder.setManaged(show);
        mainLayout.setVisible(!show);
        mainLayout.setManaged(!show);
    }

    public static class ExtensionRow {
        private final String extension;
        private final long totalBytes;
        private final int fileCount;
        private final double percent;

        public ExtensionRow(String extension, long totalBytes, int fileCount, double percent) {
            this.extension = extension;
            this.totalBytes = totalBytes;
            this.fileCount = fileCount;
            this.percent = percent;
        }

        public String getExtension() {
            return extension;
        }

        public long getTotalBytes() {
            return totalBytes;
        }

        public int getFileCount() {
            return fileCount;
        }

        public double getPercent() {
            return percent;
        }
    }
}
