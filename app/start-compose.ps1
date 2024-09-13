$output = azd -C ..\ env get-values

foreach ($line in $output) {
  $name, $value = $line.Split("=")
  $value = $value -replace '^\"|\"$'
  [Environment]::SetEnvironmentVariable($name, $value)
}

Write-Host "Environment variables set."
$roles = @(
    "a97b65f3-24c7-4388-baec-2e87135dc908",
    "5e0bd9bd-7b93-4f28-af87-19fc36ad61bd",
    "ba92f5b4-2d11-453d-a403-e96b0029c9fe"
)

# Check if service principal exists
$servicePrincipal = $(az ad sp list --display-name "agent-java-banking-spi" --query [].appId --output tsv)

if ([string]::IsNullOrEmpty($servicePrincipal)) {
    Write-Host "Service principal not found. Creating service principal.."
    $servicePrincipal = $(az ad sp create-for-rbac --name "agent-java-banking-spi" --role reader --scopes "/subscriptions/$($env:AZURE_SUBSCRIPTION_ID)/resourceGroups/$($env:AZURE_RESOURCE_GROUP)" --query appId --output tsv)
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Failed to create service principal"
        exit $LASTEXITCODE
    }
    $servicePrincipalObjectId = $(az ad sp show --id $servicePrincipal --query id --output tsv)
    Write-Host "Assigning Roles to service principal agent-java-banking-spi with principal id: $servicePrincipal and object id[$servicePrincipalObjectId]"
    foreach ($role in $roles) {
        Write-Host "Assigning Role[$role] to principal id[$servicePrincipal] for resource[/subscriptions/$($env:AZURE_SUBSCRIPTION_ID)/resourceGroups/$($env:AZURE_RESOURCE_GROUP)]"
        az role assignment create `
            --role $role `
            --assignee-object-id $servicePrincipalObjectId `
            --scope "/subscriptions/$($env:AZURE_SUBSCRIPTION_ID)/resourceGroups/$($env:AZURE_RESOURCE_GROUP)" `
            --assignee-principal-type ServicePrincipal
    }
}

$servicePrincipalPassword = $(az ad sp credential reset --id $servicePrincipal --query password --output tsv)
$servicePrincipalTenant = $(az ad sp show --id $servicePrincipal --query appOwnerOrganizationId --output tsv)

# Set environment variables
[Environment]::SetEnvironmentVariable("servicePrincipal", $servicePrincipal)
[Environment]::SetEnvironmentVariable("servicePrincipalPassword", $servicePrincipalPassword)
[Environment]::SetEnvironmentVariable("servicePrincipalTenant", $servicePrincipalTenant)


Write-Host ""
Write-Host "Starting solution locally using docker compose."
Write-Host ""

docker compose -f ./compose.yaml up