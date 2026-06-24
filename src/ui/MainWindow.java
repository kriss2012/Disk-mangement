package ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private FileManagerPane fileManagerPane;
    private PrivateSafePane privateSafePane;
    private SystemInfoPane systemInfoPane;
    private PreferencesPane preferencesPane;

    private Button selectedMenuButton = null;

    @Override
    public void start(Stage stage) {
        mainLayout = new BorderPane();
        mainLayout.getStyleClass().addAll("root", "main-window-layout");
        
        // Load Stage Application Icon
        try {
            stage.getIcons().add(new Image("file:logo.png"));
        } catch (Exception e) {
            System.err.println("Could not load stage window icon: " + e.getMessage());
        }

        // 1. Initialize Panes
        dashboardPane = new DashboardPane(this);
        scannerPane = new ScannerPane();
        analyzerPane = new AnalyzerPane();
        cleanupPane = new CleanupPane();
        fileManagerPane = new FileManagerPane();
        privateSafePane = new PrivateSafePane();
        systemInfoPane = new SystemInfoPane();
        preferencesPane = new PreferencesPane(this);

        // Link scanner and analyzer
        scannerPane.setAnalyzerPane(analyzerPane);

        // 2. Sidebar
        sidebar = createSidebar();
        mainLayout.setLeft(sidebar);

        // 3. Content Area
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: transparent;");
        mainLayout.setCenter(contentArea);

        // Apply theme on load
        applyLiveTheme();

        Scene scene = new Scene(mainLayout, 1080, 720);
        
        // Link CSS stylesheet
        try {
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load style.css stylesheet: " + e.getMessage());
        }

        stage.setScene(scene);
        stage.setTitle("Disk Utility Pro");
        stage.show();
    }

    private VBox createSidebar() {
        VBox sb = new VBox(15);
        sb.setPrefWidth(240);
        sb.setPadding(new Insets(20, 15, 20, 15));
        sb.getStyleClass().add("sidebar");

        // App/Profile Header with PNG Logo
        VBox appHeader = new VBox(10);
        appHeader.setAlignment(Pos.CENTER);
        appHeader.setPadding(new Insets(0, 0, 10, 0));

        ImageView appLogo = new ImageView();
        try {
            Image img = new Image("file:logo.png");
            appLogo.setImage(img);
            appLogo.setFitWidth(64);
            appLogo.setFitHeight(64);
            appLogo.setPreserveRatio(true);

            // Clip logo image to clean rounded corners (macOS style)
            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(64, 64);
            clip.setArcWidth(16);
            clip.setArcHeight(16);
            appLogo.setClip(clip);
        } catch (Exception e) {
            System.err.println("Could not load app logo image: " + e.getMessage());
        }

        Label appName = new Label("Disk Utility Pro");
        appName.setFont(Font.font("Outfit", FontWeight.BOLD, 18));
        appName.getStyleClass().add("text-primary");

        Label appVersion = new Label("Version 1.2.0");
        appVersion.setFont(Font.font("Outfit", 11));
        appVersion.getStyleClass().add("text-secondary");

        if (appLogo.getImage() != null) {
            appHeader.getChildren().add(appLogo);
        }
        appHeader.getChildren().addAll(appName, appVersion);

        // Sidebar Search
        TextField searchField = new TextField();
        searchField.setPromptText("Search settings...");
        searchField.setFont(Font.font("Outfit", 12));
        searchField.getStyleClass().add("text-field");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterSidebar(newVal));

        // Sidebar Items
        menuItemsContainer = new VBox(4);
        
        Button dashBtn = createMenuButton("\uD83D\uDDA5  Dashboard", () -> showPane(dashboardPane));
        Button scanBtn = createMenuButton("\uD83D\uDD0D  Folder Scanner", () -> showPane(scannerPane));
        Button analyzeBtn = createMenuButton("\uD83D\uDCC8  Storage Analyzer", () -> showPane(analyzerPane));
        Button cleanBtn = createMenuButton("\uD83D\uDDD1  Junk Cleanup", () -> showPane(cleanupPane));
        Button fileManagerBtn = createMenuButton("\uD83D\uDCC1  File Manager", () -> showPane(fileManagerPane));
        Button privateSafeBtn = createMenuButton("\uD83D\uDD12  Private Safe", () -> showPane(privateSafePane));
        Button sysInfoBtn = createMenuButton("\uD83D\uDCBB  System Info", () -> showPane(systemInfoPane));
        Button prefBtn = createMenuButton("\u2699  Preferences", () -> showPane(preferencesPane));

        menuItemsContainer.getChildren().addAll(
            dashBtn, scanBtn, analyzeBtn, cleanBtn, 
            new Separator(javafx.geometry.Orientation.HORIZONTAL),
            fileManagerBtn, privateSafeBtn,
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
        footerText.getStyleClass().add("text-secondary");
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
        btn.getStyleClass().addAll("sidebar-button");

        btn.setOnAction(e -> {
            selectMenuButton(btn);
            action.run();
        });

        return btn;
    }

    private void selectMenuButton(Button btn) {
        if (selectedMenuButton != null) {
            selectedMenuButton.getStyleClass().remove("selected");
        }
        selectedMenuButton = btn;
        selectedMenuButton.getStyleClass().add("selected");
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

        // Reset and apply style class to trigger CSS variable lookup
        mainLayout.getStyleClass().setAll("root", "main-window-layout");

        if ("macOS Light".equals(theme)) {
            mainLayout.getStyleClass().add("light-theme");
        } else if ("Cyberpunk".equals(theme)) {
            mainLayout.getStyleClass().add("cyberpunk-theme");
        } else {
            // macOS Dark (Default, base variables in .root)
        }

        // Show dashboard page
        showPane(dashboardPane);
    }

    // Redirect to File Manager and open a specific path
    public void showFileManagerAndOpenPath(String path) {
        if (fileManagerPane != null) {
            fileManagerPane.loadDirectory(new java.io.File(path));
            // Find and highlight the File Manager sidebar button
            for (javafx.scene.Node node : menuItemsContainer.getChildren()) {
                if (node instanceof Button) {
                    Button btn = (Button) node;
                    if (btn.getText().contains("File Manager")) {
                        selectMenuButton(btn);
                        break;
                    }
                }
            }
            showPane(fileManagerPane);
        }
    }
}
