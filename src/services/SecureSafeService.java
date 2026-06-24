package services;

import java.io.*;
import java.nio.file.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import model.SafeItem;

public class SecureSafeService {
    private static final String VAULT_DIR = ".private_safe";

    public static boolean unlockSafe() {
        try {
            // Delete previous auth file to prevent stale status grants
            File authFile = new File(VAULT_DIR, "auth_status.txt");
            if (authFile.exists()) {
                authFile.delete();
            }

            // Launch powershell in a new interactive console window so that Windows Hello biometric prompts work correctly
            ProcessBuilder pb = new ProcessBuilder(
                "cmd.exe", "/c", 
                "start /wait powershell.exe -NoProfile -ExecutionPolicy Bypass -File unlock_safe.ps1"
            );
            Process process = pb.start();
            process.waitFor(); // Wait for the terminal window to close

            // Read the authentication outcome written by the script
            if (authFile.exists()) {
                byte[] bytes = Files.readAllBytes(authFile.toPath());
                String status = new String(bytes).trim();
                authFile.delete(); // Delete file after consumption
                return "SUCCESS".equalsIgnoreCase(status);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void encryptFile(File srcFile) throws Exception {
        if (!srcFile.exists() || !srcFile.isFile()) {
            throw new FileNotFoundException("Target file to encrypt not found.");
        }

        // 1. Generate strong AES 256-bit key
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey aesKey = keyGen.generateKey();
        byte[] rawKey = aesKey.getEncoded();
        String base64Key = Base64.getEncoder().encodeToString(rawKey);

        // 2. Encrypt the key with Windows Data Protection API (DPAPI)
        String protectedKey = protectKeyWithDPAPI(base64Key);

        // 3. Encrypt the file data using AES
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);

        byte[] fileData = Files.readAllBytes(srcFile.toPath());
        byte[] encryptedData = cipher.doFinal(fileData);

        // 4. Write encrypted payload to safe folder
        File vaultFolder = new File(VAULT_DIR);
        if (!vaultFolder.exists()) {
            vaultFolder.mkdirs();
        }

        String fileId = "vault_" + System.currentTimeMillis() + "_" + new SecureRandom().nextInt(10000);
        File destFile = new File(vaultFolder, fileId + ".bin");
        Files.write(destFile.toPath(), encryptedData);

        // 5. Store file metadata separately
        File metaFile = new File(vaultFolder, fileId + ".metadata");
        java.util.Properties props = new java.util.Properties();
        props.setProperty("originalName", srcFile.getName());
        props.setProperty("originalPath", srcFile.getAbsolutePath());
        props.setProperty("protectedKey", protectedKey);
        
        try (FileOutputStream fos = new FileOutputStream(metaFile)) {
            props.store(fos, "Private Safe Encrypted Metadata");
        }

        // 6. Delete original file
        srcFile.delete();
    }

    public static void decryptFile(String fileId, File extractFolder) throws Exception {
        File vaultFolder = new File(VAULT_DIR);
        File binFile = new File(vaultFolder, fileId + ".bin");
        File metaFile = new File(vaultFolder, fileId + ".metadata");

        if (!binFile.exists() || !metaFile.exists()) {
            throw new FileNotFoundException("Vault file or metadata is missing.");
        }

        // 1. Load metadata
        java.util.Properties props = new java.util.Properties();
        try (FileInputStream fis = new FileInputStream(metaFile)) {
            props.load(fis);
        }

        String originalName = props.getProperty("originalName");
        String protectedKey = props.getProperty("protectedKey");

        // 2. Decrypt the key using Windows DPAPI
        String base64Key = unprotectKeyWithDPAPI(protectedKey);
        byte[] rawKey = Base64.getDecoder().decode(base64Key);
        SecretKeySpec aesKeySpec = new SecretKeySpec(rawKey, "AES");

        // 3. Decrypt file data using AES
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKeySpec);

        byte[] encryptedData = Files.readAllBytes(binFile.toPath());
        byte[] decryptedData = cipher.doFinal(encryptedData);

        // 4. Save file to extract location
        if (!extractFolder.exists()) {
            extractFolder.mkdirs();
        }
        File destFile = new File(extractFolder, originalName);
        Files.write(destFile.toPath(), decryptedData);

        // 5. Delete safe files
        binFile.delete();
        metaFile.delete();
    }

    public static List<SafeItem> getSafeItems() {
        List<SafeItem> items = new ArrayList<>();
        File vaultFolder = new File(VAULT_DIR);
        if (vaultFolder.exists() && vaultFolder.isDirectory()) {
            File[] files = vaultFolder.listFiles((dir, name) -> name.endsWith(".metadata"));
            if (files != null) {
                for (File metaFile : files) {
                    try {
                        String name = metaFile.getName();
                        String id = name.substring(0, name.lastIndexOf(".metadata"));
                        
                        java.util.Properties props = new java.util.Properties();
                        try (FileInputStream fis = new FileInputStream(metaFile)) {
                            props.load(fis);
                        }
                        
                        long size = new File(vaultFolder, id + ".bin").length();
                        items.add(new SafeItem(
                            id,
                            props.getProperty("originalName"),
                            props.getProperty("originalPath"),
                            size
                        ));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return items;
    }

    private static String protectKeyWithDPAPI(String base64Key) throws Exception {
        String script = String.format(
            "Add-Type -AssemblyName System.Security; " +
            "[System.Convert]::ToBase64String([System.Security.Cryptography.ProtectedData]::Protect([System.Convert]::FromBase64String('%s'), $null, 'CurrentUser'))",
            base64Key
        );
        return runPowerShellCommand(script);
    }

    private static String unprotectKeyWithDPAPI(String protectedBase64Key) throws Exception {
        String script = String.format(
            "Add-Type -AssemblyName System.Security; " +
            "[System.Convert]::ToBase64String([System.Security.Cryptography.ProtectedData]::Unprotect([System.Convert]::FromBase64String('%s'), $null, 'CurrentUser'))",
            protectedBase64Key
        );
        return runPowerShellCommand(script);
    }

    private static String runPowerShellCommand(String scriptCommand) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
            "powershell.exe", 
            "-NoProfile",
            "-NonInteractive",
            "-Command", scriptCommand
        );
        Process process = pb.start();
        
        // Capture standard output
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = reader.readLine();
        
        // Capture standard error for diagnostic info
        BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        StringBuilder errorMsg = new StringBuilder();
        String errLine;
        while ((errLine = errReader.readLine()) != null) {
            errorMsg.append(errLine).append("\n");
        }
        
        int exitCode = process.waitFor();
        if (exitCode == 0 && line != null) {
            return line.trim();
        }
        
        String details = errorMsg.toString().trim();
        throw new IOException("DPAPI key protection failed via PowerShell. Exit code: " + exitCode + 
                             (details.isEmpty() ? "" : "\nDetails: " + details));
    }
}
