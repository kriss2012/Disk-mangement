package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import services.DiskManager;
import services.SecureSafeService;

public class FileManagerPane extends VBox {
    private File currentDir;
    private final TextField pathField = new TextField();
    private final TableView<FileRow> tableView = new TableView<>();
    private final Label adminBadge = new Label();
    private final Label statusLabel = new Label("Ready");

    private final Button upBtn = new Button("▲ Up");
    private final Button refreshBtn = new Button("🔄 Refresh");
    private final Button newFolderBtn = new Button("📁 New Folder");
    private final Button deleteBtn = new Button("🗑 Delete");
    private final Button sendToSafeBtn = new Button("🔒 Send to Safe");

    public FileManagerPane() {
        this.setSpacing(15);
        this.setPadding(new Insets(25));
        this.setStyle("-fx-background-color: transparent;");

        // Set start directory to user home
        String userHome = System.getProperty("user.home");
        currentDir = userHome != null ? new File(userHome) : new File(".");

        // Header Row
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Live File Manager");
        title.setFont(Font.font("Outfit", FontWeight.BOLD, 26));
        title.getStyleClass().add("text-primary");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Detect Admin Access
        boolean isAdmin = checkAdminPrivileges();
        adminBadge.setText(isAdmin ? "🛡 ADMIN ACCESS" : "👤 STANDARD ACCESS");
        adminBadge.setFont(Font.font("Outfit", FontWeight.BOLD, 11));
        adminBadge.setStyle(
            "-fx-padding: 4px 8px;" +
            "-fx-background-radius: 4px;" +
            "-fx-text-fill: white;" +
            "-fx-background-color: " + (isAdmin ? "#ff3b30;" : "#5856d6;") // Red for Admin, Purple for User
        );

        header.getChildren().addAll(title, spacer, adminBadge);

        // Navigation Row
        HBox navRow = new HBox(10);
        navRow.setAlignment(Pos.CENTER_LEFT);

        upBtn.getStyleClass().add("btn-secondary");
        upBtn.setOnAction(e -> navigateUp());

        pathField.setFont(Font.font("Outfit", 13));
        pathField.getStyleClass().add("text-field");
        HBox.setHgrow(pathField, Priority.ALWAYS);
        pathField.setOnAction(e -> navigateTo(pathField.getText()));

        Button goBtn = new Button("Go");
        goBtn.getStyleClass().add("btn-primary");
        goBtn.setOnAction(e -> navigateTo(pathField.getText()));

        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setOnAction(e -> loadDirectory(currentDir));

        navRow.getChildren().addAll(upBtn, pathField, goBtn, refreshBtn);

        // Setup File Table
        setupTableView();

        // Actions Row
        HBox actionsRow = new HBox(10);
        actionsRow.setAlignment(Pos.CENTER_LEFT);

        newFolderBtn.getStyleClass().add("btn-secondary");
        newFolderBtn.setOnAction(e -> onCreateFolder());

        deleteBtn.getStyleClass().add("btn-danger");
        deleteBtn.setDisable(true);
        deleteBtn.setOnAction(e -> onDeleteItem());

        sendToSafeBtn.setStyle(
            "-fx-background-color: #ff9500;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 8px 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;"
        );
        sendToSafeBtn.setDisable(true);
        sendToSafeBtn.setOnAction(e -> onSendToSafe());

        statusLabel.setFont(Font.font("Outfit", 13));
        statusLabel.getStyleClass().add("text-secondary");

        Region actionSpacer = new Region();
        HBox.setHgrow(actionSpacer, Priority.ALWAYS);

        actionsRow.getChildren().addAll(newFolderBtn, deleteBtn, sendToSafeBtn, actionSpacer, statusLabel);

        this.getChildren().addAll(header, navRow, tableView, actionsRow);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        // Initial Load
        loadDirectory(currentDir);
    }

    private void setupTableView() {
        TableColumn<FileRow, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(250);

        TableColumn<FileRow, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getType()));
        typeCol.setPrefWidth(100);

        TableColumn<FileRow, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSize()));
        sizeCol.setPrefWidth(120);

        TableColumn<FileRow, String> dateCol = new TableColumn<>("Last Modified");
        dateCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getModified()));
        dateCol.setPrefWidth(180);

        tableView.getColumns().addAll(nameCol, typeCol, sizeCol, dateCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Double-click folder navigation
        tableView.setRowFactory(tv -> {
            TableRow<FileRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    FileRow clicked = row.getItem();
                    if (clicked.getFile().isDirectory()) {
                        loadDirectory(clicked.getFile());
                    }
                }
            });
            return row;
        });

        // Listen for table selection to enable/disable buttons
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            deleteBtn.setDisable(!hasSelection);
            
            // Only allow sending FILES to the private safe
            boolean isFileSelected = hasSelection && newVal.getFile().isFile();
            sendToSafeBtn.setDisable(!isFileSelected);
        });
    }

    private void loadDirectory(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            statusLabel.setText("Invalid directory path.");
            return;
        }

        currentDir = dir;
        pathField.setText(dir.getAbsolutePath());
        tableView.getItems().clear();

        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                tableView.getItems().add(new FileRow(file));
            }
        }
        statusLabel.setText("Loaded: " + (files != null ? files.length : 0) + " items");
    }

    private void navigateTo(String path) {
        File target = new File(path.trim());
        if (target.exists() && target.isDirectory()) {
            loadDirectory(target);
        } else {
            statusLabel.setText("Path not found.");
        }
    }

    private void navigateUp() {
        File parent = currentDir.getParentFile();
        if (parent != null) {
            loadDirectory(parent);
        } else {
            statusLabel.setText("Already at system root.");
        }
    }

    private void onCreateFolder() {
        TextInputDialog dialog = new TextInputDialog("New Folder");
        dialog.setTitle("Create Folder");
        dialog.setHeaderText("Create a new folder in:\n" + currentDir.getAbsolutePath());
        dialog.setContentText("Folder Name:");

        dialog.showAndWait().ifPresent(name -> {
            File newFolder = new File(currentDir, name.trim());
            if (newFolder.exists()) {
                showWarning("Creation Warning", "A folder or file with this name already exists.");
            } else {
                if (newFolder.mkdir()) {
                    statusLabel.setText("Folder created successfully.");
                    loadDirectory(currentDir);
                } else {
                    showError("Access Error", "Could not create folder. Elevation/Admin privileges may be required.");
                }
            }
        });
    }

    private void onDeleteItem() {
        FileRow selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        File file = selected.getFile();
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Are you sure you want to delete this item?");
        confirm.setContentText(file.getAbsolutePath() + "\nThis action cannot be undone.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (deleteRecursive(file)) {
                statusLabel.setText("Successfully deleted: " + file.getName());
                loadDirectory(currentDir);
            } else {
                showError("Access Error", "Could not delete item. File may be in use or requires Administrator credentials.");
            }
        }
    }

    private void onSendToSafe() {
        FileRow selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null || !selected.getFile().isFile()) return;

        File file = selected.getFile();
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Encrypt & Move");
        confirm.setHeaderText("Encrypt and send file to Private Safe?");
        confirm.setContentText(file.getName() + " will be encrypted using AES-256 and stored in the secure safe. The original file will be deleted.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                SecureSafeService.encryptFile(file);
                statusLabel.setText("File encrypted and moved to safe successfully.");
                loadDirectory(currentDir);
            } catch (Exception ex) {
                showError("Encryption Error", "Failed to encrypt and store file:\n" + ex.getMessage());
            }
        }
    }

    private boolean deleteRecursive(File f) {
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            if (children != null) {
                for (File c : children) {
                    if (!deleteRecursive(c)) return false;
                }
            }
        }
        return f.delete();
    }

    private boolean checkAdminPrivileges() {
        try {
            // Check elevated rights using net session
            Process p = Runtime.getRuntime().exec("net session");
            p.waitFor();
            return p.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Row representation
    public static class FileRow {
        private final File file;
        private final String name;
        private final String type;
        private final String size;
        private final String modified;

        public FileRow(File file) {
            this.file = file;
            this.name = file.getName();
            this.type = file.isDirectory() ? "Folder" : "File";
            this.size = file.isDirectory() ? "--" : DiskManager.formatSize(file.length());
            this.modified = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(file.lastModified()));
        }

        public File getFile() { return file; }
        public String getName() { return name; }
        public String getType() { return type; }
        public String getSize() { return size; }
        public String getModified() { return modified; }
    }
}
