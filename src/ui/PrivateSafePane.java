package ui;

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

    // 1. Setup View Elements
    private final VBox setupView = new VBox(15);
    private final PasswordField newPassField = new PasswordField();
    private final PasswordField confirmPassField = new PasswordField();
    private final Label setupErrorLabel = new Label();

    // 2. Login/Unlock View Elements
    private final VBox loginView = new VBox(15);
    private final PasswordField loginPassField = new PasswordField();
    private final Label loginErrorLabel = new Label();

    // 3. Unlocked View Elements
    private final VBox unlockedView = new VBox(15);
    private final TableView<SafeItem> tableView = new TableView<>();
    private final Label unlockedStatus = new Label("Safe unlocked.");
    
    private final Button addFileBtn = new Button("➕ Add File");
    private final Button extractBtn = new Button("🔓 Decrypt & Extract...");
    private final Button restoreBtn = new Button("↩ Restore to Original Path");
    private final Button lockBtn = new Button("🔒 Lock Safe");
    private final Button changePassBtn = new Button("🔑 Change Password");
    private final Button resetBtn = new Button("🗑 Reset Safe / Wipe");

    public PrivateSafePane() {
        this.setStyle("-fx-background-color: transparent;");
        this.setPadding(new Insets(25));

        // Create the three view containers
        createSetupView();
        createLoginView();
        createUnlockedView();

        // Add all to StackPane
        this.getChildren().addAll(setupView, loginView, unlockedView);
        showActiveState();
    }

    private void createSetupView() {
        setupView.setAlignment(Pos.CENTER);
        setupView.setPadding(new Insets(45));
        setupView.setMaxWidth(480);
        setupView.getStyleClass().add("glass-card");

        Label badge = new Label("🔒");
        badge.setFont(Font.font(64));

        Label header = new Label("Set Up Private Safe");
        header.setFont(Font.font("Outfit", FontWeight.BOLD, 22));
        header.getStyleClass().add("text-primary");

        Label desc = new Label(
            "Configure a custom master password for your safe.\n" +
            "This password will be securely encrypted under your Windows logon profile using enterprise-grade DPAPI."
        );
        desc.setFont(Font.font("Outfit", 12));
        desc.getStyleClass().add("text-secondary");
        desc.setAlignment(Pos.CENTER);
        desc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        newPassField.setPromptText("Enter Master Password");
        newPassField.getStyleClass().add("text-field");
        newPassField.setMaxWidth(300);

        confirmPassField.setPromptText("Confirm Master Password");
        confirmPassField.getStyleClass().add("text-field");
        confirmPassField.setMaxWidth(300);

        setupErrorLabel.setFont(Font.font("Outfit", 12));
        setupErrorLabel.setTextFill(Color.web("#ff3b30"));

        Button createBtn = new Button("Configure Safe");
        createBtn.getStyleClass().add("btn-primary");
        createBtn.setPadding(new Insets(10, 24, 10, 24));
        createBtn.setOnAction(e -> handleSetupPassword());

        setupView.getChildren().addAll(badge, header, desc, newPassField, confirmPassField, setupErrorLabel, createBtn);
    }

    private void createLoginView() {
        loginView.setAlignment(Pos.CENTER);
        loginView.setPadding(new Insets(45));
        loginView.setMaxWidth(440);
        loginView.getStyleClass().add("glass-card");

        Label badge = new Label("🛡️");
        badge.setFont(Font.font(64));

        Label header = new Label("Unlock Private Safe");
        header.setFont(Font.font("Outfit", FontWeight.BOLD, 22));
        header.getStyleClass().add("text-primary");

        Label desc = new Label("Please enter your custom Safe password to view your protected files.");
        desc.setFont(Font.font("Outfit", 13));
        desc.getStyleClass().add("text-secondary");
        desc.setAlignment(Pos.CENTER);

        loginPassField.setPromptText("Enter Safe Password");
        loginPassField.getStyleClass().add("text-field");
        loginPassField.setMaxWidth(300);
        loginPassField.setOnAction(e -> handleUnlockSafe());

        loginErrorLabel.setFont(Font.font("Outfit", 12));
        loginErrorLabel.setTextFill(Color.web("#ff3b30"));

        Button unlockBtn = new Button("Unlock Vault");
        unlockBtn.getStyleClass().add("btn-primary");
        unlockBtn.setPadding(new Insets(10, 24, 10, 24));
        unlockBtn.setOnAction(e -> handleUnlockSafe());

        loginView.getChildren().addAll(badge, header, desc, loginPassField, loginErrorLabel, unlockBtn);
    }

    private void createUnlockedView() {
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

        changePassBtn.setStyle(
            "-fx-background-color: #5856d6;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 8px 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;"
        );
        changePassBtn.setOnAction(e -> onChangePassword());

        resetBtn.setStyle(
            "-fx-background-color: #ff3b30;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 8px 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;"
        );
        resetBtn.setOnAction(e -> onResetSafe());

        unlockedStatus.setFont(Font.font("Outfit", 13));
        unlockedStatus.getStyleClass().add("text-secondary");

        Region toolbarSpacer = new Region();
        HBox.setHgrow(toolbarSpacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(addFileBtn, extractBtn, restoreBtn, changePassBtn, resetBtn, toolbarSpacer, unlockedStatus);

        unlockedView.getChildren().addAll(headerRow, tableView, toolbar);
        VBox.setVgrow(tableView, Priority.ALWAYS);
    }

    private void setupTableView() {
        TableColumn<SafeItem, String> nameCol = new TableColumn<>("Encrypted File Name");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(220);

        TableColumn<SafeItem, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(DiskManager.formatSize(data.getValue().getSizeBytes())));
        sizeCol.setPrefWidth(100);

        TableColumn<SafeItem, String> pathCol = new TableColumn<>("Original Path Reference");
        pathCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOriginalPath()));
        pathCol.setPrefWidth(380);

        tableView.getColumns().addAll(nameCol, sizeCol, pathCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            extractBtn.setDisable(!hasSelection);
            restoreBtn.setDisable(!hasSelection);
        });
    }

    private void showActiveState() {
        boolean initialized = SecureSafeService.isSafeInitialized();
        
        setupView.setVisible(!initialized);
        setupView.setManaged(!initialized);
        
        loginView.setVisible(initialized && locked);
        loginView.setManaged(initialized && locked);
        
        unlockedView.setVisible(initialized && !locked);
        unlockedView.setManaged(initialized && !locked);

        if (initialized && !locked) {
            refreshSafeList();
        }
    }

    private void handleSetupPassword() {
        String p1 = newPassField.getText().trim();
        String p2 = confirmPassField.getText().trim();

        if (p1.isEmpty()) {
            setupErrorLabel.setText("Password cannot be empty.");
            return;
        }
        if (!p1.equals(p2)) {
            setupErrorLabel.setText("Passwords do not match.");
            return;
        }

        try {
            SecureSafeService.initializeSafe(p1);
            newPassField.clear();
            confirmPassField.clear();
            setupErrorLabel.setText("");
            locked = false;
            showActiveState();
        } catch (Exception ex) {
            setupErrorLabel.setText("Failed to initialize safe:\n" + ex.getMessage());
        }
    }

    private void handleUnlockSafe() {
        String pass = loginPassField.getText().trim();
        if (pass.isEmpty()) {
            loginErrorLabel.setText("Please enter your password.");
            return;
        }

        if (SecureSafeService.verifySafePassword(pass)) {
            loginPassField.clear();
            loginErrorLabel.setText("");
            locked = false;
            showActiveState();
        } else {
            loginErrorLabel.setText("Incorrect password. Access denied.");
        }
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

    private void onChangePassword() {
        // Change password dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Change Safe Password");
        dialog.setHeaderText("Update your Private Safe master credentials.");

        ButtonType changeButtonType = new ButtonType("Change Password", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        PasswordField oldPass = new PasswordField();
        oldPass.setPromptText("Current Safe Password");
        PasswordField newPass = new PasswordField();
        newPass.setPromptText("New Master Password");
        PasswordField confirmNew = new PasswordField();
        confirmNew.setPromptText("Confirm New Master Password");

        content.getChildren().addAll(new Label("Current Password:"), oldPass, new Label("New Password:"), newPass, confirmNew);
        dialog.getDialogPane().setContent(content);

        dialog.showAndWait().ifPresent(response -> {
            if (response == changeButtonType) {
                String cur = oldPass.getText().trim();
                String n1 = newPass.getText().trim();
                String n2 = confirmNew.getText().trim();

                if (cur.isEmpty() || n1.isEmpty()) {
                    showError("Input Error", "Passwords cannot be empty.");
                    return;
                }
                if (!n1.equals(n2)) {
                    showError("Input Error", "New passwords do not match.");
                    return;
                }

                try {
                    SecureSafeService.changeSafePassword(cur, n1);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Password Changed");
                    alert.setHeaderText(null);
                    alert.setContentText("Private Safe master password updated successfully.");
                    alert.showAndWait();
                } catch (Exception ex) {
                    showError("Credential Error", "Failed to update password:\n" + ex.getMessage());
                }
            }
        });
    }

    private void onResetSafe() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Vault Wipe");
        confirm.setHeaderText("Wipe Private Safe and Reset Password?");
        confirm.setContentText(
            "WARNING: This action will PERMANENTLY erase all encrypted data in the safe and remove the password. " +
            "This operation is irreversible."
        );

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            SecureSafeService.resetSafe();
            locked = true;
            showActiveState();
            showInfo("Vault Wiped", "The private safe has been completely wiped and reset.");
        }
    }

    public void lockSafe() {
        this.locked = true;
        showActiveState();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
