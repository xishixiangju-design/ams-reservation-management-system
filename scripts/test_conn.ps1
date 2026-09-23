$ErrorActionPreference = "Stop"
try {
    Write-Host "Trying with password '123456'..."
    mysql -u root -p123456 -e "SELECT 1"
    Write-Host "Success with '123456'"
    exit 0
} catch {
    Write-Host "Failed with '123456'"
}

try {
    Write-Host "Trying with password 'root'..."
    mysql -u root -proot -e "SELECT 1"
    Write-Host "Success with 'root'"
    exit 0
} catch {
    Write-Host "Failed with 'root'"
}

try {
    Write-Host "Trying with empty password..."
    mysql -u root -e "SELECT 1"
    Write-Host "Success with empty password"
    exit 0
} catch {
    Write-Host "Failed with empty password"
}

Write-Host "All attempts failed."
exit 1
