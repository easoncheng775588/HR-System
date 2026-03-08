param(
  [switch]$SkipMySQL
)

$ErrorActionPreference = "Stop"

$projectRoot = (Resolve-Path "$PSScriptRoot\..").Path
$logsDir = Join-Path $projectRoot "logs"
$toolsDir = Join-Path $projectRoot "tools"
$backendDir = Join-Path $projectRoot "backend\recruitment-system"
$frontendDir = Join-Path $projectRoot "frontend"
$pidFile = Join-Path $logsDir "project-pids.json"

New-Item -ItemType Directory -Path $logsDir -Force | Out-Null

function Test-PortListening {
  param([int]$Port)
  $listening = $false
  try {
    $conn = Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue
    if ($conn) {
      $listening = $true
    }
  } catch {}

  if (-not $listening) {
    $text = netstat -ano | Select-String "LISTENING\s+.*:$Port|:$Port\s+.*LISTENING"
    if (-not $text) {
      $text = netstat -ano | Select-String ":$Port"
    }
    $listening = [bool]$text
  }

  return $listening
}

function Wait-Port {
  param(
    [int]$Port,
    [int]$TimeoutSeconds = 90
  )
  $sw = [System.Diagnostics.Stopwatch]::StartNew()
  while ($sw.Elapsed.TotalSeconds -lt $TimeoutSeconds) {
    if (Test-PortListening -Port $Port) { return $true }
    Start-Sleep -Seconds 1
  }
  return $false
}

function Get-ListeningPid {
  param([int]$Port)
  $lines = netstat -ano | Select-String ":$Port"
  foreach ($line in $lines) {
    $parts = ($line.ToString().Trim() -split '\s+')
    if ($parts.Length -ge 5) {
      $state = $parts[3]
      $procId = $parts[4]
      if ($state -eq "LISTENING" -and $procId -match '^\d+$') {
        return [int]$procId
      }
    }
  }
  return $null
}

function Ensure-MySQL {
  $name = "hr-mysql"
  try {
    docker version 1>$null 2>$null
    if ($LASTEXITCODE -ne 0) {
      Write-Host "[WARN] Docker not accessible. Skip MySQL auto-start."
      return
    }
  } catch {
    Write-Host "[WARN] Docker not accessible. Skip MySQL auto-start."
    return
  }

  $running = $false
  try {
    $state = docker inspect -f "{{.State.Running}}" $name 2>$null
    if ($LASTEXITCODE -eq 0 -and $state -match "true") {
      $running = $true
    }
  } catch {}

  if ($running) {
    Write-Host "[OK] MySQL container '$name' already running."
    return
  }

  $exists = $false
  try {
    docker inspect $name 1>$null 2>$null
    if ($LASTEXITCODE -eq 0) { $exists = $true }
  } catch {}

  if ($exists) {
    Write-Host "[INFO] Starting MySQL container '$name'..."
    docker start $name | Out-Null
  } else {
    Write-Host "[INFO] Creating MySQL container '$name'..."
    docker run -d `
      --name $name `
      -e MYSQL_ROOT_PASSWORD="p@ssw0rd" `
      -e MYSQL_DATABASE="hr_system" `
      -p 3306:3306 `
      mysql:8.0 | Out-Null
  }
}

function Resolve-JavaHome {
  $envJava = $env:JAVA_HOME
  if ($envJava -and (Test-Path (Join-Path $envJava "bin\java.exe"))) {
    return $envJava
  }

  $msJdk = Get-ChildItem "C:\Program Files\Microsoft" -Directory -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -like "jdk-*" } |
    Sort-Object Name -Descending |
    Select-Object -First 1
  if ($msJdk) { return $msJdk.FullName }

  return $null
}

function Resolve-MavenCmd {
  $local = Join-Path $toolsDir "maven\apache-maven-3.9.9\bin\mvn.cmd"
  if (Test-Path $local) { return $local }

  $mvnCmd = (Get-Command mvn.cmd -ErrorAction SilentlyContinue)
  if ($mvnCmd) { return $mvnCmd.Source }

  return $null
}

function Start-Backend {
  if (Test-PortListening -Port 8080) {
    Write-Host "[OK] Backend already listening on 8080."
    return (Get-ListeningPid -Port 8080)
  }

  $javaHome = Resolve-JavaHome
  if (-not $javaHome) {
    throw "JDK not found. Please install JDK 17+ first."
  }

  $mvnCmd = Resolve-MavenCmd
  if (-not $mvnCmd) {
    throw "Maven not found. Expected tools\maven\apache-maven-3.9.9\bin\mvn.cmd or system mvn.cmd."
  }

  $backendOut = Join-Path $logsDir "backend.out.log"
  $backendErr = Join-Path $logsDir "backend.err.log"
  $mavenRepo = Join-Path $toolsDir ".m2\repository"
  New-Item -ItemType Directory -Path $mavenRepo -Force | Out-Null
  $cmd = "set JAVA_HOME=$javaHome&& set PATH=%JAVA_HOME%\bin;%PATH%&& `"$mvnCmd`" -Dmaven.repo.local=`"$mavenRepo`" spring-boot:run"

  Write-Host "[INFO] Starting backend..."
  $proc = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", $cmd -WorkingDirectory $backendDir -RedirectStandardOutput $backendOut -RedirectStandardError $backendErr -PassThru

  if (-not (Wait-Port -Port 8080 -TimeoutSeconds 600)) {
    throw "Backend did not listen on 8080 in time. First run may still be downloading dependencies. Check logs\backend.out.log and logs\backend.err.log"
  }
  Write-Host "[OK] Backend started on http://localhost:8080"
  return $proc.Id
}

function Start-Frontend {
  if (Test-PortListening -Port 5173) {
    Write-Host "[OK] Frontend already listening on 5173."
    return (Get-ListeningPid -Port 5173)
  }

  $npxCmd = (Get-Command npx.cmd -ErrorAction SilentlyContinue).Source
  if (-not $npxCmd) {
    throw "npx.cmd not found. Please install Node.js first."
  }

  $frontendOut = Join-Path $logsDir "frontend.out.log"
  $frontendErr = Join-Path $logsDir "frontend.err.log"

  Write-Host "[INFO] Starting frontend..."
  $proc = Start-Process -FilePath $npxCmd -ArgumentList "vite","--host","0.0.0.0","--port","5173" -WorkingDirectory $frontendDir -RedirectStandardOutput $frontendOut -RedirectStandardError $frontendErr -PassThru

  if (-not (Wait-Port -Port 5173 -TimeoutSeconds 60)) {
    throw "Frontend did not listen on 5173 in time. Check logs\frontend.err.log"
  }
  Write-Host "[OK] Frontend started on http://localhost:5173"
  return $proc.Id
}

if (-not $SkipMySQL) {
  Ensure-MySQL
}

$backendPid = Start-Backend
$frontendPid = Start-Frontend

$meta = [ordered]@{
  startedAt   = (Get-Date).ToString("s")
  backendPid  = $backendPid
  frontendPid = $frontendPid
}
$meta | ConvertTo-Json | Set-Content -Path $pidFile -Encoding UTF8

Write-Host ""
Write-Host "Project started."
Write-Host "Frontend: http://localhost:5173"
Write-Host "Backend : http://localhost:8080"
Write-Host "Logs    : $logsDir"
