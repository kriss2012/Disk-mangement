package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import java.io.File;
import services.AppConfig;

public class PreferencesPane extends VBox {
    private final MainWindow mainWindow;

    private final TextField defaultDirField = new TextField();
    private final ComboBox<Integer> scanLimitCombo = new ComboBox<>();
    private final ComboBox<String> themeCombo = new ComboBox<>();
    private final CheckBox confirmDeleteCheck = new CheckBox("Require confirmation before deleting junk files");

    public PreferencesPane(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.setSpacing(20);
        this.setPadding(new Insets(25));
        this.setStyle("-fx-background-color: transparent;");

        // Title
        Label title = new Label("Preferences");
        title.setFont(Font.font("Outfit", FontWeight.BOLD, 26));
        title.getStyleClass().add("text-primary");

        // Grid container
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(20));
        grid.getStyleClass().add("glass-card");

        // Column Constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(150);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        // 1. Default Directory
        Label dirLbl = new Label("Default Scan Path");
        dirLbl.setFont(Font.font("Outfit", FontWeight.BOLD, 13));
        dirLbl.getStyleClass().add("text-secondary");

        HBox dirRow = new HBox(10);
        defaultDirField.setText(AppConfig.getDefaultScanDir());
        defaultDirField.setFont(Font.font("Outfit", 13));
        defaultDirField.getStyleClass().add("text-field");
        HBox.setHgrow(defaultDirField, Priority.ALWAYS);

        Button browseBtn = new Button("Browse...");
        browseBtn.setFont(Font.font("Outfit", FontWeight.SEMI_BOLD, 13));
        browseBtn.getStyleClass().add("btn-secondary");
        browseBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select Default Folder");
            File current = new File(defaultDirField.getText().trim());
            if (current.exists() && current.isDirectory()) {
                chooser.setInitialDirectory(current);
            }
            File selected = chooser.showDialog(this.getScene().getWindow());
            if (selected != null) {
                defaultDirField.setText(selected.getAbsolutePath());
            }
        });
        dirRow.getChildren().addAll(defaultDirField, browseBtn);

        grid.add(dirLbl, 0, 0);
        grid.add(dirRow, 1, 0);

        // 2. Scan limit
        Label limitLbl = new Label("Scan File Cap");
        limitLbl.setFont(Font.font("Outfit", FontWeight.BOLD, 13));
        limitLbl.getStyleClass().add("text-secondary");

        scanLimitCombo.getItems().addAll(100, 200, 500, 1000, 5000);
        scanLimitCombo.setValue(AppConfig.getScanLimit());
        scanLimitCombo.setStyle("-fx-background-color: -color-control-bg; -fx-text-fill: -color-text-primary; -fx-mark-color: -color-text-primary;");
        grid.add(limitLbl, 0, 1);
        grid.add(scanLimitCombo, 1, 1);

        // 3. Theme selection
        Label themeLbl = new Label("Appearance Theme");
        themeLbl.setFont(Font.font("Outfit", FontWeight.BOLD, 13));
        themeLbl.getStyleClass().add("text-secondary");

        themeCombo.getItems().addAll("macOS Dark", "macOS Light", "Cyberpunk");
        themeCombo.setValue(AppConfig.getTheme());
        themeCombo.setStyle("-fx-background-color: -color-control-bg; -fx-text-fill: -color-text-primary; -fx-mark-color: -color-text-primary;");
        grid.add(themeLbl, 0, 2);
        grid.add(themeCombo, 1, 2);

        // 4. Safety Prompt checkbox
        Label safetyLbl = new Label("Safety Prompts");
        safetyLbl.setFont(Font.font("Outfit", FontWeight.BOLD, 13));
        safetyLbl.getStyleClass().add("text-secondary");

        confirmDeleteCheck.setSelected(AppConfig.isConfirmDelete());
        confirmDeleteCheck.setFont(Font.font("Outfit", 13));
        confirmDeleteCheck.getStyleClass().add("check-box");
        grid.add(safetyLbl, 0, 3);
        grid.add(confirmDeleteCheck, 1, 3);

        // 5. Save Button Row
        HBox btnRow = new HBox(15);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        btnRow.setPadding(new Insets(10, 0, 0, 0));

        Button saveBtn = new Button("Apply & Save Settings");
        saveBtn.setFont(Font.font("Outfit", FontWeight.BOLD, 14));
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.setOnAction(e -> onSaveClicked());

        btnRow.getChildren().add(saveBtn);

        this.getChildren().addAll(title, grid, btnRow);
    }

    private void onSaveClicked() {
        String dir = defaultDirField.getText().trim();
        if (!dir.isEmpty() && !new File(dir).exists()) {
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Invalid Path");
            err.setHeaderText(null);
            err.setContentText("The specified default path does not exist.");
            err.showAndWait();
            return;
        }

        // Save preferences to config
        AppConfig.setDefaultScanDir(dir);
        AppConfig.setScanLimit(scanLimitCombo.getValue());
        AppConfig.setTheme(themeCombo.getValue());
        AppConfig.setConfirmDelete(confirmDeleteCheck.isSelected());

        // Notify parent window to update theme live
        mainWindow.applyLiveTheme();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Settings Saved");
        alert.setHeaderText(null);
        alert.setContentText("Your preferences have been saved and applied.");
        alert.show();
    }
}
