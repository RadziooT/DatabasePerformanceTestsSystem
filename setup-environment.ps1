#Requires -Version 7
<#
.SYNOPSIS
    This script sets up the environment for the project by installing necessary tools and dependencies.
#>

$ErrorActionPreference = "Stop"

function Write-Log
{
    param (
        [ValidateSet("INFO", "WARN", "ERROR", "STEP", "SUCCESS")]
        [string]$Level,
        [string]$Message
    )

    $color = switch ($Level)
    {
        "INFO" { "Cyan" }
        "WARN" { "Yellow" }
        "ERROR" { "Red" }
        "STEP" { "Magenta" }
        "SUCCESS" { "Green" }
    }

    Write-Host "[$Level] $Message" -ForegroundColor $color
}

function Write-PhaseStartBox
{
    param (
        [string]$PhaseName
    )

    $title = " $PhaseName "
    $innerWidth = [Math]::Max(44, $title.Length + 2)
    $leftPad = [Math]::Floor(($innerWidth - $title.Length) / 2)
    $rightPad = $innerWidth - $title.Length - $leftPad

    $topLeft = "+"
    $topRight = "+"
    $bottomLeft = "+"
    $bottomRight = "+"
    $horizontal = "-"
    $vertical = "|"

    $topBorder = "$topLeft" + (($horizontal.ToString()) * $innerWidth) + "$topRight"
    $emptyLine = "$vertical" + (" " * $innerWidth) + "$vertical"
    $titleLine = "$vertical" + (" " * $leftPad) + $title + (" " * $rightPad) + "$vertical"
    $bottomBorder = "$bottomLeft" + (($horizontal.ToString()) * $innerWidth) + "$bottomRight"

    Write-Host $topBorder -ForegroundColor Cyan
    Write-Host $emptyLine -ForegroundColor Cyan
    Write-Host $titleLine -ForegroundColor Cyan
    Write-Host $emptyLine -ForegroundColor Cyan
    Write-Host $bottomBorder -ForegroundColor Cyan
}

function Invoke-DockerPull
{
    param (
        [string]$ImageName
    )

    if (Test-DockerImageExists -ImageName $ImageName)
    {
        Write-Log -Level "INFO" -Message "Image '$ImageName' already exists locally. Skipping pull."
        return
    }

    docker pull $ImageName
    if ($LASTEXITCODE -ne 0)
    {
        throw "Failed to pull image $ImageName. Please check your Docker setup and network connection."
    }
}

function Test-DockerImageExists
{
    param (
        [string]$ImageName
    )

    docker image inspect $ImageName > $null 2>&1
    return ($LASTEXITCODE -eq 0)
}

function Invoke-DockerBuild
{
    param (
        [string]$ImageName,
        [string]$Folder
    )

    if (Test-DockerImageExists -ImageName $ImageName)
    {
        Write-Log -Level "INFO" -Message "Image '$ImageName' already exists locally. Skipping build."
        return
    }

    if (-not (Test-Path $Folder))
    {
        throw "Folder not found at expected location: $Folder"
    }

    try
    {
        Push-Location $Folder
        Write-Log -Level "INFO" -Message "Building Docker image '$ImageName' from folder: $Folder"
        docker build -t $ImageName .
        if ($LASTEXITCODE -ne 0)
        {
            throw "Failed to build Docker image '$ImageName'. Please check the Dockerfile and build context."
        }
    }
    finally
    {
        Pop-Location
    }
}

function Invoke-Step
{
    param (
        [string]$Name,
        [scriptblock]$Action
    )
    Write-Log -Level "STEP" -Message $Name
    try
    {
        & $Action
        Write-Log -Level "SUCCESS" -Message "Completed: $Name"
    }
    catch
    {
        Write-Log -Level "ERROR" -Message "Failed: $Name. Error: $($_.Exception.Message)"
        exit 1
    }
}

function Open-NewTab
{
    param (
        [string]$Title,
        [string]$Command
    )

    Write-Log -Level "INFO" -Message "Opening new tab: $Title"

    $encodedCommand = [Convert]::ToBase64String([System.Text.Encoding]::Unicode.GetBytes($Command))
    if ($wtAvailable)
    {
        try
        {
            Start-Process -FilePath $wtExePath -ArgumentList @("new-tab", "--title", $Title, $pwshExePath, "-NoExit", "-EncodedCommand", $encodedCommand) -ErrorAction Stop
            return
        }
        catch
        {
            Write-Log -Level "WARN" -Message "Failed to open Windows Terminal tab. Falling back to standalone PowerShell window. Error: $($_.Exception.Message)"
        }
    }

    Start-Process -FilePath $pwshExePath -ArgumentList "-NoExit", "-EncodedCommand", $encodedCommand
}

function Compare-Versions
{
    param (
        [string]$Actual,
        [string]$Required
    )
    $normalizedActual = ($Actual.Trim() -replace '^v', '') -replace '-.*$', ''
    $normalizedRequired = ($Required.Trim() -replace '^v', '') -replace '-.*$', ''

    try
    {
        $actualVersion = [version]$normalizedActual
        $requiredVersion = [version]$normalizedRequired
    }
    catch
    {
        throw "Unable to compare versions. Actual='$Actual', Required='$Required'."
    }

    return ($actualVersion -ge $requiredVersion)
}

# --------------------------------------------------------------------------
# Get Basic Directory
# --------------------------------------------------------------------------
$requiredFolders = @("DockerManagementUI", "DockerManager", "PerformanceTests", "DataGenerator", "MockApp")
$scriptDir = (Resolve-Path $PSScriptRoot).Path
$baseDir = $scriptDir
Write-Log -Level "INFO" -Message "Setting up environment in directory: $baseDir"

$pwshCmd = Get-Command pwsh -ErrorAction SilentlyContinue
if (-not $pwshCmd)
{
    throw "PowerShell 7 (pwsh) is required. Please install it: https://aka.ms/powershell-release?tag=stable"
}

$pwshExePath = if ($pwshCmd.Path) { $pwshCmd.Path } else { $pwshCmd.Source }

# --------------------------------------------------------------------------
# Detect Windows Terminal
# --------------------------------------------------------------------------
$wtCmd = Get-Command wt -ErrorAction SilentlyContinue
$wtAvailable = $null -ne $wtCmd
if ($wtAvailable)
{
    $wtExePath = if ($wtCmd.Path) { $wtCmd.Path } else { $wtCmd.Source }
    Write-Log -Level "INFO" -Message "Windows Terminal detected. Setting up environment for Windows Terminal."
    # Additional setup for Windows Terminal can be added here
}
else
{
    $wtExePath = $null
    Write-Log -Level "WARN" -Message "Windows Terminal not detected. A new PowerShell window will be used for environment setup."
}

# ==========================================================================
# Initial Verification
# ==========================================================================
Write-PhaseStartBox -PhaseName "Phase 1: Initial Verification"

Invoke-Step "Verify npm >= 9.8.0" {
    if (-not (Get-Command npm -ErrorAction SilentlyContinue))
    {
        throw "npm is not installed or not in PATH. Please install Node.js (https://nodejs.org/) which includes npm."
    }

    $raw = (& npm --version 2>&1 | Out-String).Trim()
    if (($LASTEXITCODE -ne 0) -or (-not $raw))
    {
        throw "npm is not installed or not in PATH. Please install Node.js (https://nodejs.org/) which includes npm."
    }
    if (-not (Compare-Versions -Actual $raw -Required "9.8.0"))
    {
        throw "npm version $raw is less than required 9.8.0. Please update npm."
    }

    Write-Log -Level "INFO" -Message "Detected npm version: $raw"
}

Invoke-Step "Verify node >= 20" {
    if (-not (Get-Command node -ErrorAction SilentlyContinue))
    {
        throw "Node.js is not installed or not in PATH. Please install Node.js (https://nodejs.org/)."
    }

    $raw = (& node --version 2>&1 | Out-String).Trim()
    if (($LASTEXITCODE -ne 0) -or (-not $raw))
    {
        throw "Node.js is not installed or not in PATH. Please install Node.js (https://nodejs.org/)."
    }
    $cleaned = $raw -replace '^v', ''
    Write-Log -Level "INFO" -Message "Detected Node.js version: $cleaned"
    if (-not (Compare-Versions -Actual $cleaned -Required "20.0.0"))
    {
        throw "Node.js version $cleaned is less than required 20.0.0. Please update Node.js."
    }
}

Invoke-Step "Verify Java == 21" {
    if (-not (Get-Command java -ErrorAction SilentlyContinue))
    {
        throw "Java is not installed or not in PATH. Please install Java (https://www.oracle.com/java/technologies/downloads/)."
    }

    $raw = (& java -version 2>&1 | Out-String)
    if (($LASTEXITCODE -ne 0) -or (-not $raw))
    {
        throw "Java is not installed or not in PATH. Please install Java (https://www.oracle.com/java/technologies/downloads/)."
    }
    $match = [regex]::Match($raw, 'version "(\d+\.\d+\.\d+)"')
    if (-not $match.Success)
    {
        throw "Unable to parse Java version from output: $raw"
    }
    $cleaned = $match.Groups[1].Value
    Write-Log -Level "INFO" -Message "Detected Java version: $cleaned"
    $javaMajorVersion = ([version]$cleaned).Major
    if ($javaMajorVersion -ne 21)
    {
        throw "Java version $cleaned does not match required major version 21. Please update Java."
    }

    if (-not (Get-Command mvn -ErrorAction SilentlyContinue))
    {
        throw "Maven is not installed or not in PATH. Please install Maven (https://maven.apache.org/download.cgi)."
    }

    $mvnRaw = (& mvn --version 2>&1 | Out-String)
    if ($LASTEXITCODE -ne 0)
    {
        throw "Unable to execute 'mvn --version'. Please ensure Maven is installed and accessible."
    }

    if ($mvnRaw -match 'Apache Maven\s+([0-9]+\.[0-9]+\.[0-9]+)')
    {
        $mavenVersion = $matches[1]
        Write-Log -Level "INFO" -Message "Detected Maven version: $mavenVersion"
        if (-not (Compare-Versions -Actual $mavenVersion -Required "3.9.0"))
        {
            throw "Maven version $mavenVersion is less than required 3.9.0. Please update Maven."
        }
    }
    else
    {
        throw "Unable to detect Maven version from 'mvn --version' output."
    }
}

Invoke-Step "Verify Docker is present and daemon active" {
    $dockerCmd = Get-Command docker -ErrorAction SilentlyContinue
    if (-not $dockerCmd)
    {
        throw "Docker is not installed or not in PATH. Please install Docker (https://www.docker.com/get-started)."
    }
    try
    {
        docker info > $null 2>&1
        if ($LASTEXITCODE -ne 0)
        {
            throw "Docker daemon is not running or not accessible. Please start Docker and ensure it is running."
        }
    }
    catch
    {
        throw "Docker daemon is not running or not accessible. Please start Docker and ensure it is running."
    }
}

Invoke-Step "Validate folders exist" {
    foreach ($folder in $requiredFolders)
    {
        $path = Join-Path $baseDir $folder
        if (-not (Test-Path $path))
        {
            throw "Required folder '$folder' not found at expected location: $path"
        }
        else
        {
            Write-Log -Level "INFO" -Message "Found required folder: $path"
        }
    }
}

# ==========================================================================
# Phase 2: Image Pulling
# ==========================================================================
Write-PhaseStartBox -PhaseName "Phase 2: Image Pulling"

Invoke-Step "Pull image maven:3.9.9-eclipse-temurin-21-alpine" {
    Invoke-DockerPull -ImageName "maven:3.9.9-eclipse-temurin-21-alpine"
}

Invoke-Step "Pull image mysql:8.3.0" {
    Invoke-DockerPull -ImageName "mysql:8.3.0"
}

Invoke-Step "Pull image postgres:16.2-alpine" {
    Invoke-DockerPull -ImageName "postgres:16.2-alpine"
}

Invoke-Step "Pull image gvenzl/oracle-free:latest" {
    Invoke-DockerPull -ImageName "gvenzl/oracle-free:latest"
}

Invoke-Step "Pull image mcr.microsoft.com/mssql/server:2022-latest" {
    Invoke-DockerPull -ImageName "mcr.microsoft.com/mssql/server:2022-latest"
}

# ==========================================================================
# Phase 3: Image Building
# ==========================================================================
Write-PhaseStartBox -PhaseName "Phase 3: Image Building"

Invoke-Step "Build image docker-manager-performance-tests" {
    $folder = Join-Path $baseDir "PerformanceTests"
    Invoke-DockerBuild -ImageName "docker-manager-performance-tests" -Folder $folder
}

Invoke-Step "Build image docker-manager-data-generator" {
    $folder = Join-Path $baseDir "DataGenerator"
    Invoke-DockerBuild -ImageName "docker-manager-data-generator" -Folder $folder
}

Invoke-Step "Build image docker-manager-mock-app" {
    $folder = Join-Path $baseDir "MockApp"
    Invoke-DockerBuild -ImageName "docker-manager-mock-app" -Folder $folder
}

# ==========================================================================
# Phase 4: Environment Variables Setup
# ==========================================================================
Write-PhaseStartBox -PhaseName "Phase 4: Environment Variables Setup"

Invoke-Step "Set environment variables" {
    $env:DOCKER_HOST = "tcp://localhost:2375"
    $env:DOCKER_MANAGER_DB_NAME = "testDb"
    $env:DOCKER_MANAGER_DB_PASSWORD = "Password123!"
    $env:DOCKER_MANAGER_DB_USERNAME = "sa"

    Write-Log -Level "INFO" -Message "Environment variables set for docker manager: dockerHost, dbName, dbUser, dbPassword"
}

# ==========================================================================
# Phase 5: Environment Start
# ==========================================================================
Write-PhaseStartBox -PhaseName "Phase 5: Environment Start"

Invoke-Step "Launch Frontend" {
    $dir = Join-Path $baseDir "DockerManagementUI"
    try
    {
        Push-Location $dir

        npm install
        if ($LASTEXITCODE -ne 0)
        {
            throw "Failed to install frontend dependencies."
        }

        npm run build -- --configuration production
        if ($LASTEXITCODE -ne 0)
        {
            throw "Failed to build frontend production bundle."
        }
    }
    finally
    {
        Pop-Location
    }

    $cmd = "Set-Location '$dir'; npm run serve:static"
    Open-NewTab -Title "Frontend" -Command $cmd
}

Invoke-Step "Launch Backend" {
    $dir = Join-Path $baseDir "DockerManager"

    $dbName = $env:dbName
    $dbUser = $env:dbUser
    $dbPassword = $env:dbPassword
    $mvnDbArgs = "-DdbName='$dbName' -DdbUser='$dbUser' -DdbPassword='$dbPassword'"

    $cmd = "Set-Location '$dir'; mvn clean install $mvnDbArgs; if (`$LASTEXITCODE -ne 0) { exit `$LASTEXITCODE }; mvn spring-boot:run $mvnDbArgs"
    Open-NewTab -Title "Backend" -Command $cmd
}

# ==========================================================================
# Finalization
# ==========================================================================
Write-Log -Level "SUCCESS" -Message "Environment setup complete. Frontend and Backend should now be running in separate tabs."
