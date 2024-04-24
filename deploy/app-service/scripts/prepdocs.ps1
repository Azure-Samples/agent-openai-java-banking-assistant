Write-Host ""
Write-Host "Loading azd .env file from current environment"
Write-Host ""

$output = azd env get-values

foreach ($line in $output) {
  if (!$line.Contains('=')) {
    continue
  }

  $name, $value = $line.Split("=")
  $value = $value -replace '^\"|\"$'
  [Environment]::SetEnvironmentVariable($name, $value)
}

Write-Host "Environment variables set."


Write-Host 'Building java indexer..'
Start-Process -FilePath "mvn"  -ArgumentList "package -f ../../app/indexer/pom.xml" -Wait -NoNewWindow

Write-Host 'Running the java indexer cli.jar...'
Start-Process -FilePath "java" -ArgumentList "-jar ../../app/indexer/cli/target/cli.jar `"../../data`" --storageaccount $env:AZURE_STORAGE_ACCOUNT --container $env:AZURE_STORAGE_CONTAINER --searchservice $env:AZURE_SEARCH_SERVICE --openai-service-name $env:AZURE_OPENAI_SERVICE --openai-emb-deployment $env:AZURE_OPENAI_EMB_DEPLOYMENT --index $env:AZURE_SEARCH_INDEX --formrecognizerservice $env:AZURE_FORMRECOGNIZER_SERVICE --verbose add" -Wait -NoNewWindow