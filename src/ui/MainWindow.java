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

        Tab dashTab = new Tab("Dashboard", new Label("Dashboard coming soon"));
        Tab scanTab = new Tab("Scanner", new Label("Scanner coming soon"));
        Tab analyzeTab = new Tab("Analyzer", new Label("Analyzer coming soon"));
        Tab cleanTab = new Tab("Cleanup", new Label("Cleanup coming soon"));

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
