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
    # Explicitly specify "Password (not PIN)" in the message to guide the user
    $cred = $host.ui.PromptForCredential(
        "Private Safe Verification", 
        "Please enter your Windows Account Password (not PIN) to unlock your secure private safe.", 
        "$env:USERNAME", 
        ""
    )
    if ($cred) {
        $username = $cred.UserName
        $password = $cred.GetNetworkCredential().Password
        
        $domain = "."
        # Strip domain or computer prefixes if present (e.g. COMPUTER\user -> user)
        if ($username.Contains("\")) {
            $parts = $username.Split("\")
            $domain = $parts[0]
            $username = $parts[1]
        }
        
        $verified = $false
        
        # Method 1: Native Win32 LogonUser API (Highest reliability for local and Microsoft accounts)
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
            # Add type dynamically. If type already exists in session, catch the error and reuse it.
            $win32Type = [Win32.Win32Logon]
            if (-not $win32Type) {
                $win32Type = Add-Type -MemberDefinition $signature -Name "Win32Logon" -Namespace "Win32" -PassThru
            }
            
            $token = [IntPtr]::Zero
            # LogonType 3 = Network Logon (works without needing interactive logon rights)
            $verified = $win32Type::LogonUser($username, $domain, $password, 3, 0, [ref]$token)
        } catch {
            # Fallback to Method 2 if Win32 invocation fails
        }
        
        # Method 2: PrincipalContext DirectoryServices API
        if (-not $verified) {
            try {
                Add-Type -AssemblyName System.DirectoryServices.AccountManagement
                $pc = New-Object System.DirectoryServices.AccountManagement.PrincipalContext([System.DirectoryServices.AccountManagement.ContextType]::Machine)
                $verified = $pc.ValidateCredentials($username, $password)
            } catch {
                # Try domain context
                try {
                    $pcDomain = New-Object System.DirectoryServices.AccountManagement.PrincipalContext([System.DirectoryServices.AccountManagement.ContextType]::Domain)
                    $verified = $pcDomain.ValidateCredentials($username, $password)
                } catch {
                    # Ignore
                }
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
