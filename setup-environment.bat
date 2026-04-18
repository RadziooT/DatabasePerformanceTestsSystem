@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
set "PS1=%SCRIPT_DIR%setup-environment.ps1"

if not exist "%PS1%" (
    echo [ERROR] Could not find "%PS1%".
    pause
    exit /b 1
)

where pwsh >nul 2>nul
if not %ERRORLEVEL%==0 (
    echo [ERROR] PowerShell 7 ^(pwsh^) is required but was not found in PATH.
    echo [INFO] Install PowerShell 7: https://aka.ms/powershell-release?tag=stable
    pause
    exit /b 1
)

pwsh -NoProfile -ExecutionPolicy Bypass -File "%PS1%" %*
set "EXITCODE=%ERRORLEVEL%"

if not "%EXITCODE%"=="0" (
    echo [ERROR] Setup script failed with exit code %EXITCODE%.
    pause
) else (
    echo [INFO] Setup script completed successfully.
)

exit /b %EXITCODE%
