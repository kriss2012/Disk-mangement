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
        "Please enter your Windows password or PIN to unlock your secure private safe.", 
        "$env:USERNAME", 
        ""
    )
    if ($cred) {
        # Load DirectoryServices.AccountManagement to securely authenticate credentials
        Add-Type -AssemblyName System.DirectoryServices.AccountManagement
        
        $username = $cred.UserName
        # Strip domain/computer prefixes if present (e.g. COMPUTER\user -> user)
        if ($username.Contains("\")) {
            $username = $username.Split("\")[1]
        }
        
        $password = $cred.GetNetworkCredential().Password
        
        # Validate against Local Computer Accounts
        $pc = New-Object System.DirectoryServices.AccountManagement.PrincipalContext([System.DirectoryServices.AccountManagement.ContextType]::Machine)
        $verified = $pc.ValidateCredentials($username, $password)
        
        # If not verified and domain is active, check Domain controller
        if (-not $verified) {
            try {
                $pcDomain = New-Object System.DirectoryServices.AccountManagement.PrincipalContext([System.DirectoryServices.AccountManagement.ContextType]::Domain)
                $verified = $pcDomain.ValidateCredentials($username, $password)
            } catch {
                # Ignore domain validation failures if offline
            }
        }
        
        if ($verified) {
            Write-Output "SUCCESS"
        } else {
            Write-Output "FAILED"
        }
    } else {
        Write-Output "FAILED"
    }
} catch {
    Write-Output "FAILED"
}
