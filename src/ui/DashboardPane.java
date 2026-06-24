package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;
import model.DriveInfo;
import services.DiskManager;

public class DashboardPane extends VBox {
    private final DiskManager diskManager = new DiskManager();
    private final FlowPane cardsContainer = new FlowPane();
    private final Button refreshBtn = new Button("Refresh");
    private final MainWindow mainWindow;

    public DashboardPane(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.setSpacing(20);
        this.setPadding(new Insets(25));
        this.setStyle("-fx-background-color: transparent;");

        // Header Layout
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setMaxWidth(Double.MAX_VALUE);

        Label title = new Label("Your Drives");
        title.setFont(Font.font("Outfit", FontWeight.BOLD, 26));
        title.getStyleClass().add("text-primary");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        refreshBtn.setFont(Font.font("Outfit", FontWeight.SEMI_BOLD, 13));
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> refreshDrives());

        header.getChildren().addAll(title, spacer, refreshBtn);

        // ScrollPane for drives container
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(cardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        cardsContainer.setHgap(20);
        cardsContainer.setVgap(20);
        cardsContainer.setPadding(new Insets(10, 0, 10, 0));
        cardsContainer.setStyle("-fx-background-color: transparent;");

        this.getChildren().addAll(header, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        refreshDrives();
    }

    public void refreshDrives() {
        cardsContainer.getChildren().clear();
        List<DriveInfo> drives = diskManager.getAllDrives();

        if (drives.isEmpty()) {
            Label noDrives = new Label("No active storage drives detected.");
            noDrives.getStyleClass().add("text-secondary");
            noDrives.setFont(Font.font("Outfit", 15));
            cardsContainer.getChildren().add(noDrives);
            return;
        }

        for (DriveInfo drive : drives) {
            VBox card = createDriveCard(drive);
            cardsContainer.getChildren().add(card);
        }
    }

    private VBox createDriveCard(DriveInfo drive) {
        VBox card = new VBox(12);
        card.setPrefWidth(260);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("glass-card");
        card.setCursor(javafx.scene.Cursor.HAND);

        // Hover scale transforms
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-scale-x: 1.02; -fx-scale-y: 1.02;");
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-scale-x: 1.0; -fx-scale-y: 1.0;");
        });

        // Click redirection
        card.setOnMouseClicked(e -> {
            if (mainWindow != null) {
                mainWindow.showFileManagerAndOpenPath(drive.getPath());
            }
        });

        // Drive Letter/Path
        Label pathLabel = new Label("Drive " + drive.getPath());
        pathLabel.setFont(Font.font("Outfit", FontWeight.BOLD, 18));
        pathLabel.getStyleClass().add("text-primary");

        // Storage details VBox
        VBox details = new VBox(4);
        Label totalLbl = new Label("Total: " + DiskManager.formatSize(drive.getTotalBytes()));
        Label usedLbl = new Label("Used: " + DiskManager.formatSize(drive.getUsedBytes()));
        Label freeLbl = new Label("Free: " + DiskManager.formatSize(drive.getFreeBytes()));
        
        totalLbl.getStyleClass().add("text-secondary");
        usedLbl.getStyleClass().add("text-secondary");
        freeLbl.getStyleClass().add("text-secondary");
        
        totalLbl.setFont(Font.font("Outfit", 13));
        usedLbl.setFont(Font.font("Outfit", 13));
        freeLbl.setFont(Font.font("Outfit", 13));
        
        details.getChildren().addAll(totalLbl, usedLbl, freeLbl);

        // Progress bar
        ProgressBar bar = new ProgressBar(drive.getUsagePercent() / 100.0);
        bar.setMaxWidth(Double.MAX_VALUE);
        
        // Dynamic Accent Color
        double pct = drive.getUsagePercent();
        String accentColor = "-fx-accent: #34c759;"; // Green
        if (pct >= 85.0) {
            accentColor = "-fx-accent: #ff3b30;"; // Red
        } else if (pct >= 60.0) {
            accentColor = "-fx-accent: #ff9500;"; // Orange
        }
        bar.setStyle(accentColor);

        // Percentage label
        Label pctLabel = new Label(String.format("%.1f%% used", pct));
        pctLabel.setFont(Font.font("Outfit", FontWeight.SEMI_BOLD, 13));
        pctLabel.setTextFill(pct >= 85.0 ? Color.web("#ff3b30") : pct >= 60.0 ? Color.web("#ff9500") : Color.web("#34c759"));

        card.getChildren().addAll(pathLabel, details, bar, pctLabel);
        return card;
    }
}
