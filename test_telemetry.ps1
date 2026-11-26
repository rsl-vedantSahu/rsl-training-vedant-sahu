$alice = '{"name":"Alice","email":"alice@example.com"}'
$alice | Set-Content -Path "alice.json" -Encoding ASCII

$bob = '{"name":"Bob","email":"bob@example.com"}'
$bob | Set-Content -Path "bob.json" -Encoding ASCII

$bobDup = '{"name":"Bob Duplicate","email":"bob@example.com"}'
$bobDup | Set-Content -Path "bob_dup.json" -Encoding ASCII

$proc = Start-Process -FilePath "java" -ArgumentList "-javaagent:opentelemetry-javaagent.jar", "-Dotel.service.name=crud-user-service", "-Dotel.traces.exporter=logging", "-Dotel.metrics.exporter=logging", "-Dotel.logs.exporter=logging", "-Dotel.metric.export.interval=2000", "-jar", "target\observability-skeleton-0.0.1-SNAPSHOT.jar" -PassThru -RedirectStandardOutput "telemetry_app.log" -RedirectStandardError "telemetry_err.log"

Write-Output "Waiting for application to start on port 8080..."
$ready = $false
for ($i = 0; $i -lt 30; $i++) {
    Start-Sleep -Seconds 1
    try {
        $tcp = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
        if ($tcp) {
            $ready = $true
            break
        }
    } catch {}
}

if (-not $ready) {
    Write-Output "Application did not start within 30 seconds."
    Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
    exit 1
}

Write-Output "Application is UP on port 8080!"
Start-Sleep -Seconds 2

Write-Output "`n=========================================="
Write-Output "1. POST /users (Success - Create Alice)"
Write-Output "=========================================="
curl.exe -i -X POST http://localhost:8080/users -H "Content-Type: application/json" -d "@alice.json"

Write-Output "`n=========================================="
Write-Output "2. GET /users/1 (Success - Retrieve Alice)"
Write-Output "=========================================="
curl.exe -i http://localhost:8080/users/1

Write-Output "`n=========================================="
Write-Output "3. GET /users/999 (Failure - User Not Found 404)"
Write-Output "=========================================="
curl.exe -i http://localhost:8080/users/999

Write-Output "`n=========================================="
Write-Output "4. DELETE /users/1 (Success - Delete Alice)"
Write-Output "=========================================="
curl.exe -i -X DELETE http://localhost:8080/users/1

Write-Output "`n=========================================="
Write-Output "5. DELETE /users/999 (Failure - User Not Found 404)"
Write-Output "=========================================="
curl.exe -i -X DELETE http://localhost:8080/users/999

Write-Output "`n=========================================="
Write-Output "6. POST /users (Success - Create Bob)"
Write-Output "=========================================="
curl.exe -i -X POST http://localhost:8080/users -H "Content-Type: application/json" -d "@bob.json"

Write-Output "`n=========================================="
Write-Output "7. POST /users (Failure - Duplicate Email 409 Conflict)"
Write-Output "=========================================="
curl.exe -i -X POST http://localhost:8080/users -H "Content-Type: application/json" -d "@bob_dup.json"

Write-Output "`nFlushing OpenTelemetry metrics & traces (waiting 5 seconds)..."
Start-Sleep -Seconds 5

Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue

Remove-Item -Force -ErrorAction SilentlyContinue alice.json, bob.json, bob_dup.json
Write-Output "Test execution completed successfully."
