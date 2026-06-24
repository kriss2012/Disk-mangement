package ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import java.lang.management.ManagementFactory;
import services.DiskManager;

public class SystemInfoPane extends VBox {
    private final Label osNameLbl = new Label();
    private final Label osArchLbl = new Label();
    private final Label cpuCountLbl = new Label();
    private final Label javaVerLbl = new Label();
    private final Label uptimeLbl = new Label();

    // Memory stats
    private final Label memUsedLbl = new Label();
    private final Label memAllocatedLbl = new Label();
    private final Label memMaxLbl = new Label();
    private final ProgressBar memBar = new ProgressBar();
    private final Label memPercentLbl = new Label();

    private Timeline updateTimeline;

    public SystemInfoPane() {
        this.setSpacing(20);
        this.setPadding(new Insets(25));
        this.setStyle("-fx-background-color: transparent;");

        // Title
        Label title = new Label("System Information");
        title.setFont(Font.font("Outfit", FontWeight.BOLD, 26));
        title.getStyleClass().add("text-primary");

        // Grid of system specs
        GridPane specGrid = new GridPane();
        specGrid.setHgap(30);
        specGrid.setVgap(15);
        specGrid.setPadding(new Insets(20));
        specGrid.getStyleClass().add("glass-card");

        addSpecRow(specGrid, 0, "Operating System", osNameLbl);
        addSpecRow(specGrid, 1, "Architecture", osArchLbl);
        addSpecRow(specGrid, 2, "Processor Cores", cpuCountLbl);
        addSpecRow(specGrid, 3, "Java Runtime Version", javaVerLbl);
        addSpecRow(specGrid, 4, "App Uptime", uptimeLbl);

        // Memory card (macOS Activity Monitor Style)
        VBox memCard = new VBox(15);
        memCard.setPadding(new Insets(20));
        memCard.getStyleClass().add("glass-card");

        Label memTitle = new Label("JVM Memory Diagnostics");
        memTitle.setFont(Font.font("Outfit", FontWeight.BOLD, 16));
        memTitle.getStyleClass().add("text-primary");

        HBox memProgressRow = new HBox(12);
        memProgressRow.setAlignment(Pos.CENTER_LEFT);
        
        memBar.setPrefWidth(300);
        memBar.setPrefHeight(16);
        memBar.setStyle("-fx-accent: #007aff;");
        
        memPercentLbl.setFont(Font.font("Outfit", FontWeight.BOLD, 14));
        memPercentLbl.getStyleClass().add("text-primary");
        memProgressRow.getChildren().addAll(memBar, memPercentLbl);

        GridPane memGrid = new GridPane();
        memGrid.setHgap(30);
        memGrid.setVgap(10);
        addSpecRow(memGrid, 0, "Used Memory", memUsedLbl);
        addSpecRow(memGrid, 1, "Allocated Heap", memAllocatedLbl);
        addSpecRow(memGrid, 2, "Max Allowed Heap", memMaxLbl);

        Button gcBtn = new Button("Run Garbage Collector");
        gcBtn.setFont(Font.font("Outfit", FontWeight.SEMI_BOLD, 13));
        gcBtn.getStyleClass().add("btn-secondary");
        gcBtn.setOnAction(e -> {
            System.gc();
            updateStats();
            statusAlert();
        });

        memCard.getChildren().addAll(memTitle, memProgressRow, memGrid, gcBtn);

        this.getChildren().addAll(title, specGrid, memCard);

        initStaticSpecs();
        updateStats();

        // Start periodic update timeline
        updateTimeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> updateStats()));
        updateTimeline.setCycleCount(Timeline.INDEFINITE);
        updateTimeline.play();
    }

    private void addSpecRow(GridPane grid, int row, String name, Label valLabel) {
        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Outfit", FontWeight.BOLD, 13));
        nameLabel.getStyleClass().add("text-secondary");
        
        valLabel.setFont(Font.font("Outfit", 13));
        valLabel.getStyleClass().add("text-primary");

        grid.add(nameLabel, 0, row);
        grid.add(valLabel, 1, row);
    }

    private void initStaticSpecs() {
        osNameLbl.setText(System.getProperty("os.name") + " (v" + System.getProperty("os.version") + ")");
        osArchLbl.setText(System.getProperty("os.arch"));
        cpuCountLbl.setText(String.valueOf(Runtime.getRuntime().availableProcessors()));
        javaVerLbl.setText(System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
    }

    private void updateStats() {
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        uptimeLbl.setText(formatUptime(uptime));

        long free = Runtime.getRuntime().freeMemory();
        long total = Runtime.getRuntime().totalMemory();
        long max = Runtime.getRuntime().maxMemory();
        long used = total - free;

        memUsedLbl.setText(DiskManager.formatSize(used));
        memAllocatedLbl.setText(DiskManager.formatSize(total));
        memMaxLbl.setText(DiskManager.formatSize(max));

        double pct = (double) used / total;
        memBar.setProgress(pct);
        memPercentLbl.setText(String.format("%.1f%% used", pct * 100));
        
        // Dynamic coloring of bar based on JVM usage
        if (pct > 0.85) {
            memBar.setStyle("-fx-accent: #ff3b30;");
        } else if (pct > 0.60) {
            memBar.setStyle("-fx-accent: #ff9500;");
        } else {
            memBar.setStyle("-fx-accent: #007aff;");
        }
    }

    private String formatUptime(long ms) {
        long secs = ms / 1000;
        long mins = secs / 60;
        long hours = mins / 60;
        return String.format("%02d hours, %02d minutes, %02d seconds", hours, mins % 60, secs % 60);
    }

    private void statusAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Garbage Collector");
        alert.setHeaderText(null);
        alert.setContentText("Requested Java Garbage Collection. Unused system heap space released successfully.");
        alert.show();
    }
}
