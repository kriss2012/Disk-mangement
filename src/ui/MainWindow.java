package ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainWindow extends Application {
    @Override
    public void start(Stage stage) {
        TabPane tabPane = new TabPane();

        // Real Dashboard tab
        DashboardPane dashboardPane = new DashboardPane();
        Tab dashTab = new Tab("Dashboard", dashboardPane);
        
        // Real Scanner tab
        ScannerPane scannerPane = new ScannerPane();
        Tab scanTab = new Tab("Scanner", scannerPane);
        
        // Real Analyzer tab
        AnalyzerPane analyzerPane = new AnalyzerPane();
        Tab analyzeTab = new Tab("Analyzer", analyzerPane);
        
        // Link Scanner to Analyzer for data handoff
        scannerPane.setAnalyzerPane(analyzerPane);

        // Real Cleanup tab
        CleanupPane cleanupPane = new CleanupPane();
        Tab cleanTab = new Tab("Cleanup", cleanupPane);

        tabPane.getTabs().addAll(dashTab, scanTab, analyzeTab, cleanTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        BorderPane root = new BorderPane(tabPane);
        
        // Premium styling for tab layout
        root.setStyle("-fx-background-color: #1e1e24;");
        tabPane.setStyle("-fx-background-color: #1e1e24;");

        stage.setScene(new Scene(root, 900, 600));
        stage.setTitle("Disk Management System");
        stage.show();
    }
}
