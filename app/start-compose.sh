echo ""
echo "Loading azd .env file from current environment"
echo ""

while IFS='=' read -r key value; do
    value=$(echo "$value" | sed 's/^"//' | sed 's/"$//')
    export "$key=$value"
    echo "export $key=$value"
done <<EOF
$(azd -C ../ env get-values)
EOF

if [ $? -ne 0 ]; then
    echo "Failed to load environment variables from azd environment"
    exit $?
fi

echo ""
echo "Checking Service Principal and get password"
echo ""

roles=(
    "a97b65f3-24c7-4388-baec-2e87135dc908"
    "5e0bd9bd-7b93-4f28-af87-19fc36ad61bd"
    "ba92f5b4-2d11-453d-a403-e96b0029c9fe"
    "7f951dda-4ed3-4680-a7ca-43fe172d538d"
)

#Check if service principal exists
export servicePrincipal=$(az ad sp list --display-name "agent-java-banking-spi" --query [].appId --output tsv)

if [ -z "$servicePrincipal" ]; then
    echo "Service principal not found. Creating service principal..."
    export servicePrincipal=$(az ad sp create-for-rbac --name "agent-java-banking-spi" --role reader --scopes /subscriptions/"$AZURE_SUBSCRIPTION_ID"/resourceGroups/"$AZURE_RESOURCE_GROUP" --query appId --output tsv)
    if [ $? -ne 0 ]; then
        echo "Failed to create service principal"
        exit $?
    fi
    export servicePrincipalObjectId=$(az ad sp show --id "$servicePrincipal" --query id --output tsv)
    echo "Assigning Roles to service principal agent-java-banking-spi with principal id:$servicePrincipal and object id[$servicePrincipalObjectId]"
    for role in "${roles[@]}"; do
        
        echo "Assigning Role[$role] to principal id[$servicePrincipal] for resource[/subscriptions/"$AZURE_SUBSCRIPTION_ID"/resourceGroups/"$AZURE_RESOURCE_GROUP"] "
        az role assignment create \
            --role "$role" \
            --assignee-object-id "$servicePrincipalObjectId" \
            --scope /subscriptions/"$AZURE_SUBSCRIPTION_ID"/resourceGroups/"$AZURE_RESOURCE_GROUP" \
            --assignee-principal-type ServicePrincipal
    done
fi

export servicePrincipalPassword=$(az ad sp credential reset --id "$servicePrincipal"  --query password --output tsv)
export servicePrincipalTenant=$(az ad sp show --id "$servicePrincipal" --query appOwnerOrganizationId --output tsv)

echo ""
echo "Starting solution locally using docker compose. "
echo ""

docker compose -f ./compose.yaml up

