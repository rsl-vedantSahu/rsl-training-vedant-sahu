param(
    [ValidateSet("trace", "metrics", "logs", "all")]
    [string]$Type = "all"
)

if (-not (Test-Path "telemetry_err.log") -or -not (Test-Path "telemetry_app.log")) {
    Write-Host "Telemetry log files not found. Run .\test_telemetry.ps1 first." -ForegroundColor Red
    exit 1
}

if ($Type -eq "trace" -or $Type -eq "all") {
    Write-Host "`n================================================================================" -ForegroundColor Cyan
    Write-Host " [SCREENSHOT 1: TRACE EVIDENCE] Automatic HTTP Span + Manual Service Child Span" -ForegroundColor Cyan
    Write-Host "================================================================================" -ForegroundColor Cyan
    Write-Host "Notice both spans share the same trace_id: 509d0baa3e7f1cd49a56c52b2df809f9`n" -ForegroundColor Yellow

    # Extract DELETE /users/1 trace spans
    Get-Content telemetry_err.log | Select-String "509d0baa3e7f1cd49a56c52b2df809f9" | Where-Object {
        $_ -match "'DELETE /users/\{id\}'" -or $_ -match "'delete-user'"
    } | ForEach-Object {
        Write-Host $_.Line -ForegroundColor Green
    }

    Write-Host "`n--- Failed Request Error Span (Duplicate Email 409 Conflict) ---" -ForegroundColor Yellow
    Get-Content telemetry_err.log | Select-String "37876de2be8871da32ee0cc19d029469" | Where-Object {
        $_ -match "'POST /users'" -or $_ -match "'create-user'"
    } | ForEach-Object {
        Write-Host $_.Line -ForegroundColor Red
    }
}

if ($Type -eq "metrics" -or $Type -eq "all") {
    Write-Host "`n================================================================================" -ForegroundColor Cyan
    Write-Host " [SCREENSHOT 2: METRICS EVIDENCE] Operation Count & Duration Histogram" -ForegroundColor Cyan
    Write-Host "================================================================================" -ForegroundColor Cyan
    Write-Host "Notice attributes are strictly low-cardinality (operation, status) - NO user.id`n" -ForegroundColor Yellow

    # Print operation count
    Write-Host "--- Metric 1: users.operation.count ---" -ForegroundColor Yellow
    $lines = Get-Content telemetry_err.log
    $countStart = $false
    foreach ($line in $lines) {
        if ($line -match "name=users.operation.count") {
            $countStart = $true
        }
        if ($countStart) {
            Write-Host $line -ForegroundColor Magenta
            if ($line -match "aggregationTemporality=") {
                $countStart = $false
                break
            }
        }
    }

    Write-Host "`n--- Metric 2: users.operation.duration (Histogram) ---" -ForegroundColor Yellow
    $durStart = $false
    foreach ($line in $lines) {
        if ($line -match "name=users.operation.duration") {
            $durStart = $true
        }
        if ($durStart) {
            Write-Host $line -ForegroundColor Magenta
            if ($line -match "aggregationTemporality=") {
                $durStart = $false
                break
            }
        }
    }
}

if ($Type -eq "logs" -or $Type -eq "all") {
    Write-Host "`n================================================================================" -ForegroundColor Cyan
    Write-Host " [SCREENSHOT 3: LOG EVIDENCE] MDC Trace & Span ID Correlated Logs" -ForegroundColor Cyan
    Write-Host "================================================================================" -ForegroundColor Cyan
    Write-Host "Notice application logs with [trace_id=... span_id=...] correlation`n" -ForegroundColor Yellow

    Get-Content telemetry_app.log | Select-String "c.e.observability.service.UserService" | ForEach-Object {
        if ($_.Line -match "ERROR") {
            Write-Host $_.Line -ForegroundColor Red
        } else {
            Write-Host $_.Line -ForegroundColor Gray
        }
    }
}
