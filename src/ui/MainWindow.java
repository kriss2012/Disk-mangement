package ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import services.AppConfig;

public class MainWindow extends Application {
    private BorderPane mainLayout;
    private VBox sidebar;
    private StackPane contentArea;
    private VBox menuItemsContainer;

    // View panes
    private DashboardPane dashboardPane;
    private ScannerPane scannerPane;
    private AnalyzerPane analyzerPane;
    private CleanupPane cleanupPane;
    private SystemInfoPane systemInfoPane;
    private PreferencesPane preferencesPane;

    private Button selectedMenuButton = null;

    @Override
    public void start(Stage stage) {
        mainLayout = new BorderPane();
        
        // 1. Initialize Panes
        dashboardPane = new DashboardPane();
        scannerPane = new ScannerPane();
        analyzerPane = new AnalyzerPane();
        cleanupPane = new CleanupPane();
        systemInfoPane = new SystemInfoPane();
        preferencesPane = new PreferencesPane(this);

        // Link scanner and analyzer
        scannerPane.setAnalyzerPane(analyzerPane);

        // 2. Sidebar
        sidebar = createSidebar();
        mainLayout.setLeft(sidebar);

        // 3. Content Area
        contentArea = new StackPane();
        mainLayout.setCenter(contentArea);

        // Apply theme on load
        applyLiveTheme();

        Scene scene = new Scene(mainLayout, 1020, 680);
        stage.setScene(scene);
        stage.setTitle("Disk Utility Pro");
        stage.show();
    }

    private VBox createSidebar() {
        VBox sb = new VBox(15);
        sb.setPrefWidth(240);
        sb.setPadding(new Insets(20, 15, 20, 15));

        // App/Profile Header
        VBox appHeader = new VBox(5);
        appHeader.setAlignment(Pos.CENTER);
        appHeader.setPadding(new Insets(0, 0, 10, 0));

        Label appIcon = new Label("💽");
        appIcon.setFont(Font.font(42));

        Label appName = new Label("Disk Utility Pro");
        appName.setFont(Font.font("Outfit", FontWeight.BOLD, 18));
        appName.setTextFill(Color.WHITE);

        Label appVersion = new Label("Version 1.2.0");
        appVersion.setFont(Font.font("Outfit", 11));
        appVersion.setTextFill(Color.web("#a0a0a5"));

        appHeader.getChildren().addAll(appIcon, appName, appVersion);

        // Sidebar Search
        TextField searchField = new TextField();
        searchField.setPromptText("Search settings...");
        searchField.setFont(Font.font("Outfit", 12));
        searchField.setStyle(
            "-fx-background-color: #1e1e24;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6px;" +
            "-fx-padding: 6px 10px;" +
            "-fx-border-color: #3a3a3c;" +
            "-fx-border-radius: 6px;"
        );
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterSidebar(newVal));

        // Sidebar Items
        menuItemsContainer = new VBox(4);
        
        Button dashBtn = createMenuButton("🖥️  Dashboard", () -> showPane(dashboardPane));
        Button scanBtn = createMenuButton("🔍  Folder Scanner", () -> showPane(scannerPane));
        Button analyzeBtn = createMenuButton("📈  Storage Analyzer", () -> showPane(analyzerPane));
        Button cleanBtn = createMenuButton("🧹  Junk Cleanup", () -> showPane(cleanupPane));
        Button sysInfoBtn = createMenuButton("ℹ️  System Info", () -> showPane(systemInfoPane));
        Button prefBtn = createMenuButton("⚙️  Preferences", () -> showPane(preferencesPane));

        menuItemsContainer.getChildren().addAll(
            dashBtn, scanBtn, analyzeBtn, cleanBtn, 
            new Separator(javafx.geometry.Orientation.HORIZONTAL),
            sysInfoBtn, prefBtn
        );

        // Sidebar Footer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox footerBox = new VBox(4);
        footerBox.setAlignment(Pos.CENTER);
        Label footerText = new Label("Inspired by macOS Settings");
        footerText.setFont(Font.font("Outfit", 10));
        footerText.setTextFill(Color.web("#666666"));
        footerBox.getChildren().add(footerText);

        sb.getChildren().addAll(appHeader, searchField, menuItemsContainer, spacer, footerBox);
        
        // Select Dashboard initially
        selectMenuButton(dashBtn);

        return sb;
    }

    private Button createMenuButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setFont(Font.font("Outfit", FontWeight.SEMI_BOLD, 13));
        
        // Normal state
        btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #e1e1e4;" +
            "-fx-background-radius: 6px;" +
            "-fx-padding: 10px 14px;" +
            "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e -> {
            if (btn != selectedMenuButton) {
                btn.setStyle(
                    "-fx-background-color: #323236;" +
                    "-fx-text-fill: white;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-padding: 10px 14px;" +
                    "-fx-cursor: hand;"
                );
            }
        });

        btn.setOnMouseExited(e -> {
            if (btn != selectedMenuButton) {
                btn.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: #e1e1e4;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-padding: 10px 14px;" +
                    "-fx-cursor: hand;"
                );
            }
        });

        btn.setOnAction(e -> {
            selectMenuButton(btn);
            action.run();
        });

        return btn;
    }

    private void selectMenuButton(Button btn) {
        if (selectedMenuButton != null) {
            // Restore previous button style
            selectedMenuButton.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #e1e1e4;" +
                "-fx-background-radius: 6px;" +
                "-fx-padding: 10px 14px;" +
                "-fx-cursor: hand;"
            );
        }
        selectedMenuButton = btn;
        // Highlight active button
        selectedMenuButton.setStyle(
            "-fx-background-color: #007aff;" + // macOS Accent Blue
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6px;" +
            "-fx-padding: 10px 14px;" +
            "-fx-cursor: hand;"
        );
    }

    private void showPane(Pane pane) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(pane);
    }

    private void filterSidebar(String query) {
        if (query == null || query.isEmpty()) {
            for (javafx.scene.Node node : menuItemsContainer.getChildren()) {
                node.setVisible(true);
                node.setManaged(true);
            }
            return;
        }

        for (javafx.scene.Node node : menuItemsContainer.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                String text = btn.getText().toLowerCase();
                boolean match = text.contains(query.toLowerCase());
                btn.setVisible(match);
                btn.setManaged(match);
            } else if (node instanceof Separator) {
                node.setVisible(false);
                node.setManaged(false);
            }
        }
    }

    // Refresh UI theme styling live
    public void applyLiveTheme() {
        String theme = AppConfig.getTheme();

        String mainBg;
        String sidebarBg;
        String sidebarBorder;

        if ("macOS Light".equals(theme)) {
            mainBg = "#f5f5f7";
            sidebarBg = "#e1e1e4";
            sidebarBorder = "#d2d2d7";
            
            mainLayout.setStyle("-fx-background-color: #f5f5f7;");
            sidebar.setStyle("-fx-background-color: #e1e1e4; -fx-border-color: #d2d2d7; -fx-border-width: 0 1 0 0;");
            contentArea.setStyle("-fx-background-color: #f5f5f7;");
        } else if ("Cyberpunk".equals(theme)) {
            mainBg = "#0f0f13";
            sidebarBg = "#180828";
            sidebarBorder = "#ff0055";
            
            mainLayout.setStyle("-fx-background-color: #0f0f13;");
            sidebar.setStyle("-fx-background-color: #180828; -fx-border-color: #ff0055; -fx-border-width: 0 1 0 0;");
            contentArea.setStyle("-fx-background-color: #0f0f13;");
        } else { // macOS Dark (Default)
            mainBg = "#1e1e24";
            sidebarBg = "#252528";
            sidebarBorder = "#323236";
            
            mainLayout.setStyle("-fx-background-color: #1e1e24;");
            sidebar.setStyle("-fx-background-color: #252528; -fx-border-color: #323236; -fx-border-width: 0 1 0 0;");
            contentArea.setStyle("-fx-background-color: #121214;");
        }

        // Show dashboard page
        showPane(dashboardPane);
    }
}
