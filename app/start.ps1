Write-Host ""
Write-Host "Loading azd .env file from current environment"
Write-Host ""

foreach ($line in (& azd env get-values)) {
    if ($line -match "([^=]+)=(.*)") {
        $key = $matches[1]
        $value = $matches[2] -replace '^"|"$'
        Set-Item -Path "env:\$key" -Value $value
    }
}

if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to load environment variables from azd environment"
    exit $LASTEXITCODE
}


Write-Host ""
Write-Host "Restoring frontend npm packages"
Write-Host ""
Set-Location ./frontend
npm install
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to restore frontend npm packages"
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "Building frontend"
Write-Host ""
npm run build
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to build frontend"
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "Starting spring boot api backend and react spa from backend/public static content"
Write-Host ""
Set-Location ../backend
Start-Process http://localhost:8080

Start-Process -FilePath "./mvnw.cmd" -ArgumentList "spring-boot:run -Dspring-boot.run.profiles=dev" -Wait -NoNewWindow

if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to start backend"
    exit $LASTEXITCODE
}
