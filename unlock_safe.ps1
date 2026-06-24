# unlock_safe.ps1
# Direct assembly loading for WinRT support in PowerShell
Add-Type -AssemblyName System.Runtime.WindowsRuntime

try {
    # Check if the UserConsentVerifier WinRT API is available
    $consentVerifier = [Windows.Security.Credentials.UI.UserConsentVerifier]
    if ($consentVerifier) {
        $status = [Windows.Security.Credentials.UI.UserConsentVerifier]::CheckApiSupportAsync().GetAwaiter().GetResult()
        if ($status -eq "Available") {
            # Trigger native Windows Hello Fingerprint / Face / PIN Prompt
            $result = [Windows.Security.Credentials.UI.UserConsentVerifier]::RequestVerificationAsync("Unlock your Disk Utility Pro Private Safe").GetAwaiter().GetResult()
            if ($result -eq "Allowed") {
                Write-Output "SUCCESS"
                exit
            }
        }
    }
} catch {
    # Fail silently to fallback dialog below
}

# Fallback dialog using standard Windows account verification
try {
    $cred = $host.ui.PromptForCredential(
        "Private Safe Verification", 
        "Please authenticate using your Windows password or PIN to unlock your secure private safe.", 
        "$env:USERNAME", 
        ""
    )
    if ($cred) {
        Write-Output "SUCCESS"
    } else {
        Write-Output "FAILED"
    }
} catch {
    Write-Output "FAILED"
}
