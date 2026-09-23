# Initialization script for AMS Database
# Usage: ./scripts/init_db.ps1

Write-Host "Checking MySQL availability..."
# Ensure mysql is in PATH or find it
$mysql_cmd = "mysql"
if (Get-Command mysql -ErrorAction SilentlyContinue) {
    $mysql_cmd = (Get-Command mysql).Source
} else {
    Write-Error "MySQL client not found in PATH."
    exit 1
}

$DB_NAME = "trae_ams"
$DB_USER = "root"
$DB_PASS = "123456"

# 0. Ensure Database Exists
Write-Host "Ensuring database '$DB_NAME' exists..."
try {
    # Use explicit argument array for safety
    $create_args = @("-u", "$DB_USER", "-p$DB_PASS", "-e", "CREATE DATABASE IF NOT EXISTS $DB_NAME DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;")
    & $mysql_cmd $create_args
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to create database."
    }
} catch {
    Write-Error "Failed to check/create database: $_"
    exit 1
}

# Function to execute SQL file using PowerShell piping with explicit UTF8 encoding
function Run-SqlFile {
    param (
        [string]$FilePath
    )
    # Convert to absolute path
    $AbsPath = Resolve-Path $FilePath
    Write-Host "Running $AbsPath..."
    
    # Set output encoding to UTF8 to ensure pipes work correctly with mysql
    $OutputEncoding = [System.Text.Encoding]::UTF8
    
    # Use explicit arguments for mysql
    # Note: We pass DB_NAME here to select the database
    $mysql_args = @("-u", "$DB_USER", "-p$DB_PASS", "$DB_NAME", "--default-character-set=utf8mb4")

    # Use Get-Content with UTF8 encoding and pipe to mysql
    try {
        Get-Content -Path $AbsPath -Encoding UTF8 | & $mysql_cmd $mysql_args
        
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Failed to execute $FilePath (Exit Code: $LASTEXITCODE)"
            exit $LASTEXITCODE
        }
        Write-Host "$FilePath imported successfully."
    } catch {
        Write-Error "Error executing $FilePath : $_"
        exit 1
    }
}

# 1. Run Schema
Run-SqlFile "docs/DB_SCHEMA_v2.sql"

# 2. Run Data
Run-SqlFile "docs/DB_SCHEMA_v2_data.sql"

# 3. Verification
Write-Host "Running verification queries..."
$verify_sql = @"
SELECT '--- 1. Appointment Details ---' as '';
SELECT a.id, a.customer_id, s.name as service_name, a.status, a.total_amount 
FROM ams_appointment a
JOIN ams_appointment_item ai ON a.id = ai.appt_id
JOIN ams_service s ON ai.service_id = s.id
WHERE a.store_id = 1;

SELECT '--- 2. Technician Schedule ---' as '';
SELECT t.real_name, s.date, s.status, s.start_time, s.end_time
FROM ams_technician_schedule s
JOIN ams_technician_info t ON s.tech_id = t.user_id
WHERE s.date >= CURDATE();

SELECT '--- 3. Financial Summary ---' as '';
SELECT type, pay_method, SUM(amount) as total
FROM ams_transaction
GROUP BY type, pay_method;
"@

# Run verification via pipe
# Ensure OutputEncoding is UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
$verify_args = @("-u", "$DB_USER", "-p$DB_PASS", "$DB_NAME", "--default-character-set=utf8mb4", "-t")
$verify_sql | & $mysql_cmd $verify_args

Write-Host "Database initialization complete."
