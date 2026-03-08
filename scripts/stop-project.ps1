param(
  [switch]$StopMySQL
)

$ErrorActionPreference = "Stop"

$projectRoot = (Resolve-Path "$PSScriptRoot\..").Path
$logsDir = Join-Path $projectRoot "logs"
$pidFile = Join-Path $logsDir "project-pids.json"

function Stop-ByPort {
  param([int]$Port)
  $lines = netstat -ano | Select-String ":$Port"
  if (-not $lines) { return }

  $pids = @()
  foreach ($line in $lines) {
    $parts = ($line.ToString().Trim() -split '\s+')
    if ($parts.Length -ge 5) {
      $state = $parts[3]
      $procId = $parts[4]
      if ($state -eq "LISTENING" -and $procId -match '^\d+$') {
        $pids += [int]$procId
      }
    }
  }
  $pids = $pids | Select-Object -Unique

  foreach ($procId in $pids) {
    try {
      Stop-Process -Id $procId -Force -ErrorAction Stop
      Write-Host "[OK] Stopped process $procId on port $Port."
    } catch {
      try {
        taskkill /PID $procId /T /F | Out-Null
        Write-Host "[OK] taskkill stopped process $procId on port $Port."
      } catch {}
    }
  }
}

function Stop-ByPidFile {
  if (-not (Test-Path $pidFile)) { return }
  try {
    $data = Get-Content $pidFile -Raw | ConvertFrom-Json
    foreach ($name in @("backendPid","frontendPid")) {
      $procId = $data.$name
      if ($procId) {
        try {
          Stop-Process -Id $procId -Force -ErrorAction Stop
          Write-Host "[OK] Stopped $name ($procId)."
        } catch {
          try {
            taskkill /PID $procId /T /F | Out-Null
            Write-Host "[OK] taskkill stopped $name ($procId)."
          } catch {}
        }
      }
    }
  } catch {}
  Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
}

Stop-ByPort -Port 5173
Stop-ByPort -Port 8080
Stop-ByPidFile

if ($StopMySQL) {
  try {
    docker stop hr-mysql | Out-Null
    Write-Host "[OK] Stopped MySQL container hr-mysql."
  } catch {}
}

Write-Host "Project stop completed."
