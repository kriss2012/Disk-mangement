# unlock_safe.ps1
# Direct assembly loading for WinRT support in PowerShell
Add-Type -AssemblyName System.Runtime.WindowsRuntime
$result = "FAILED"

try {
    # Check if the UserConsentVerifier WinRT API is available
    $consentVerifier = [Windows.Security.Credentials.UI.UserConsentVerifier]
    if ($consentVerifier) {
        $status = [Windows.Security.Credentials.UI.UserConsentVerifier]::CheckApiSupportAsync().GetAwaiter().GetResult()
        if ($status -eq "Available") {
            # Trigger native Windows Hello Fingerprint / Face / PIN Prompt
            $verifyResult = [Windows.Security.Credentials.UI.UserConsentVerifier]::RequestVerificationAsync("Unlock your Disk Utility Pro Private Safe").GetAwaiter().GetResult()
            if ($verifyResult -eq "Allowed") {
                $result = "SUCCESS"
            }
        }
    }
} catch {
    # Fail silently to fallback verification below
}

# If Windows Hello didn't succeed, invoke credential prompt
if ($result -ne "SUCCESS") {
    try {
        $cred = $host.ui.PromptForCredential(
            "Private Safe Verification", 
            "Please enter your Windows Account Password or PIN to unlock your secure private safe.", 
            "$env:USERNAME", 
            ""
        )
        if ($cred) {
            $verified = $false
            
            # Method 1: Validate using Start-Process (handles Microsoft Accounts and domain accounts natively)
            try {
                $p = Start-Process powershell.exe -ArgumentList "-Command Exit" -Credential $cred -PassThru -WindowStyle Hidden
                $p.WaitForExit()
                if ($p.ExitCode -eq 0) {
                    $verified = $true
                }
            } catch {
                # Fallback to Method 2
            }
            
            # Method 2: LogonUser Win32 API
            if (-not $verified) {
                try {
                    $signature = @'
                    [DllImport("advapi32.dll", SetLastError = true, CharSet = CharSet.Unicode)]
                    public static extern bool LogonUser(
                        string lpszUsername,
                        string lpszDomain,
                        string lpszPassword,
                        int dwLogonType,
                        int dwLogonProvider,
                        out IntPtr phToken
                    );
'@
                    $win32Type = Add-Type -MemberDefinition $signature -Name "Win32Logon" -Namespace "Win32" -PassThru -ErrorAction SilentlyContinue
                    if (-not $win32Type) {
                        $win32Type = [Win32.Win32Logon]
                    }
                    
                    $username = $cred.UserName
                    $password = $cred.GetNetworkCredential().Password
                    $domain = "."
                    
                    if ($username.Contains("\")) {
                        $parts = $username.Split("\")
                        $domain = $parts[0]
                        $username = $parts[1]
                    }
                    
                    $token = [IntPtr]::Zero
                    $verified = $win32Type::LogonUser($username, $domain, $password, 3, 0, [ref]$token)
                } catch {
                    # Fail silently
                }
            }
            
            if ($verified) {
                $result = "SUCCESS"
            }
        }
    } catch {
        # Fail silently
    }
}

# Write status to file for Java to read
try {
    $dir = ".private_safe"
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Force -Path $dir | Out-Null
    }
    Set-Content -Path "$dir/auth_status.txt" -Value $result -Force
} catch {
    # Fail silently
}
