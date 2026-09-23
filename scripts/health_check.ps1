$backendUrl = "http://localhost:8080/api/health"
$frontendUrl = "http://localhost:3000"

function Check-Service ($url, $name) {
    try {
        $response = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 5
        if ($response.StatusCode -eq 200) {
            Write-Host "[$name] OK - Status: $($response.StatusCode)" -ForegroundColor Green
            return $true
        } else {
            Write-Host "[$name] ERROR - Status: $($response.StatusCode)" -ForegroundColor Red
            return $false
        }
    } catch {
        Write-Host "[$name] ERROR - Connection Failed: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

Write-Host "Checking Services..."
$backendStatus = Check-Service $backendUrl "Backend"
$frontendStatus = Check-Service $frontendUrl "Frontend"

if ($backendStatus -and $frontendStatus) {
    Write-Host "All services are running correctly." -ForegroundColor Green
} else {
    Write-Host "Some services are down. Please check logs." -ForegroundColor Red
}
Pause
