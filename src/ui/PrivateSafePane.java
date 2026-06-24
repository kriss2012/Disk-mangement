package ui;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;
import model.SafeItem;
import services.DiskManager;
import services.SecureSafeService;

public class PrivateSafePane extends StackPane {
    private boolean locked = true;

    // Locked UI elements
    private final VBox lockedView = new VBox(20);
    private final Button unlockBtn = new Button("Unlock Private Safe");
    private final Label lockedStatus = new Label("Secure Safe Locked");
    private final ProgressIndicator authProgress = new ProgressIndicator();

    // Unlocked UI elements
    private final VBox unlockedView = new VBox(15);
    private final TableView<SafeItem> tableView = new TableView<>();
    private final Label unlockedStatus = new Label("Safe unlocked.");
    
    private final Button addFileBtn = new Button("➕ Add File");
    private final Button extractBtn = new Button("🔓 Decrypt & Extract...");
    private final Button restoreBtn = new Button("↩ Restore to Original Path");
    private final Button lockBtn = new Button("🔒 Lock Safe");

    public PrivateSafePane() {
        this.setStyle("-fx-background-color: transparent;");
        this.setPadding(new Insets(25));

        // Setup both views
        setupLockedView();
        setupUnlockedView();

        // Add both to StackPane and toggle initial state
        this.getChildren().addAll(lockedView, unlockedView);
        toggleViewState();
    }

    private void setupLockedView() {
        lockedView.setAlignment(Pos.CENTER);
        lockedView.setPadding(new Insets(40));

        Label lockIcon = new Label("🔒");
        lockIcon.setFont(Font.font(72));

        Label header = new Label("Private Security Safe");
        header.setFont(Font.font("Outfit", FontWeight.BOLD, 22));
        header.getStyleClass().add("text-primary");

        Label desc = new Label(
            "Protect your sensitive documents with enterprise-grade AES-256 encryption.\n" +
            "Access requires authentication via Windows Hello (Fingerprint, Face, PIN) or your Windows credentials."
        );
        desc.setFont(Font.font("Outfit", 13));
        desc.getStyleClass().add("text-secondary");
        desc.setAlignment(Pos.CENTER);
        desc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        lockedStatus.setFont(Font.font("Outfit", FontWeight.SEMI_BOLD, 14));
        lockedStatus.getStyleClass().add("text-secondary");

        unlockBtn.setFont(Font.font("Outfit", FontWeight.BOLD, 15));
        unlockBtn.getStyleClass().add("btn-primary");
        unlockBtn.setPadding(new Insets(12, 30, 12, 30));
        unlockBtn.setOnAction(e -> triggerUnlockAuthentication());

        authProgress.setVisible(false);
        authProgress.setMaxSize(30, 30);

        lockedView.getChildren().addAll(lockIcon, header, desc, unlockBtn, authProgress, lockedStatus);
    }

    private void setupUnlockedView() {
        unlockedView.setAlignment(Pos.CENTER);

        // Header Row
        HBox headerRow = new HBox(15);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Private Vault Explorer");
        title.setFont(Font.font("Outfit", FontWeight.BOLD, 22));
        title.getStyleClass().add("text-primary");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        lockBtn.getStyleClass().add("btn-danger");
        lockBtn.setOnAction(e -> lockSafe());

        headerRow.getChildren().addAll(title, spacer, lockBtn);

        // Setup File list Table
        setupTableView();

        // Action Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        addFileBtn.getStyleClass().add("btn-primary");
        addFileBtn.setOnAction(e -> onAddFile());

        extractBtn.getStyleClass().add("btn-secondary");
        extractBtn.setDisable(true);
        extractBtn.setOnAction(e -> onExtractSelected());

        restoreBtn.getStyleClass().add("btn-secondary");
        restoreBtn.setDisable(true);
        restoreBtn.setOnAction(e -> onRestoreSelected());

        unlockedStatus.setFont(Font.font("Outfit", 13));
        unlockedStatus.getStyleClass().add("text-secondary");

        Region toolbarSpacer = new Region();
        HBox.setHgrow(toolbarSpacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(addFileBtn, extractBtn, restoreBtn, toolbarSpacer, unlockedStatus);

        unlockedView.getChildren().addAll(headerRow, tableView, toolbar);
        VBox.setVgrow(tableView, Priority.ALWAYS);
    }

    private void setupTableView() {
        TableColumn<SafeItem, String> nameCol = new TableColumn<>("Encrypted File Name");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(250);

        TableColumn<SafeItem, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(DiskManager.formatSize(data.getValue().getSizeBytes())));
        sizeCol.setPrefWidth(120);

        TableColumn<SafeItem, String> pathCol = new TableColumn<>("Original Path Reference");
        pathCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOriginalPath()));
        pathCol.setPrefWidth(350);

        tableView.getColumns().addAll(nameCol, sizeCol, pathCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            extractBtn.setDisable(!hasSelection);
            restoreBtn.setDisable(!hasSelection);
        });
    }

    private void toggleViewState() {
        lockedView.setVisible(locked);
        lockedView.setManaged(locked);
        unlockedView.setVisible(!locked);
        unlockedView.setManaged(!locked);

        if (!locked) {
            refreshSafeList();
        }
    }

    private void triggerUnlockAuthentication() {
        unlockBtn.setDisable(true);
        authProgress.setVisible(true);
        lockedStatus.setText("Waiting for Windows Hello / PIN verification...");

        Task<Boolean> authTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return SecureSafeService.unlockSafe();
            }
        };

        authTask.setOnSucceeded(e -> {
            boolean success = authTask.getValue();
            unlockBtn.setDisable(false);
            authProgress.setVisible(false);

            if (success) {
                locked = false;
                toggleViewState();
                unlockedStatus.setText("Safe unlocked.");
            } else {
                lockedStatus.setText("Access Denied. Windows Hello verification failed.");
            }
        });

        authTask.setOnFailed(e -> {
            unlockBtn.setDisable(false);
            authProgress.setVisible(false);
            lockedStatus.setText("Error during Windows Hello request.");
        });

        new Thread(authTask).start();
    }

    private void refreshSafeList() {
        tableView.getItems().clear();
        List<SafeItem> items = SecureSafeService.getSafeItems();
        tableView.getItems().setAll(items);
        unlockedStatus.setText("Vault contains " + items.size() + " protected items.");
    }

    private void onAddFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select File to Lock in Safe");
        File selected = chooser.showOpenDialog(this.getScene().getWindow());

        if (selected != null) {
            try {
                SecureSafeService.encryptFile(selected);
                unlockedStatus.setText("File encrypted and moved to safe.");
                refreshSafeList();
            } catch (Exception ex) {
                showError("Vault Error", "Failed to encrypt and store file:\n" + ex.getMessage());
            }
        }
    }

    private void onExtractSelected() {
        SafeItem selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose Extract Destination Folder");
        File dest = chooser.showDialog(this.getScene().getWindow());

        if (dest != null) {
            try {
                SecureSafeService.decryptFile(selected.getId(), dest);
                unlockedStatus.setText("Decrypted and extracted: " + selected.getName());
                refreshSafeList();
            } catch (Exception ex) {
                showError("Vault Decryption Error", "Failed to decrypt and extract file:\n" + ex.getMessage());
            }
        }
    }

    private void onRestoreSelected() {
        SafeItem selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        File originalFile = new File(selected.getOriginalPath());
        File parentDir = originalFile.getParentFile();

        try {
            SecureSafeService.decryptFile(selected.getId(), parentDir);
            unlockedStatus.setText("Decrypted and restored to: " + selected.getOriginalPath());
            refreshSafeList();
        } catch (Exception ex) {
            showError("Vault Decryption Error", "Failed to restore file:\n" + ex.getMessage());
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void lockSafe() {
        this.locked = true;
        toggleViewState();
        this.lockedStatus.setText("Safe locked.");
    }
}
