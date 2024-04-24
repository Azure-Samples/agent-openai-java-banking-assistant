echo ""
echo "Loading azd .env file from current environment"
echo ""

while IFS='=' read -r key value; do
    value=$(echo "$value" | sed 's/^"//' | sed 's/"$//')
    export "$key=$value"
    echo "export $key=$value"
done <<EOF
$(azd env get-values)
EOF

if [ $? -ne 0 ]; then
    echo "Failed to load environment variables from azd environment"
    exit $?
fi

echo ""
echo "Checking Service Principal and get password"
echo ""

roles=(
    "5e0bd9bd-7b93-4f28-af87-19fc36ad61bd"
    "a97b65f3-24c7-4388-baec-2e87135dc908"
    "2a2b9908-6ea1-4ae2-8e65-a410df84e7d1"
    "ba92f5b4-2d11-453d-a403-e96b0029c9fe"
    "1407120a-92aa-4202-b7e9-c0e197c71c8f"
    "8ebe5a00-799e-43f5-93ac-243d3dce84a7"
    "7ca78c08-252a-4471-8644-bb5ff32d4ba0"
    "4f6d3b9b-027b-4f4c-9142-0e5a2a2247e0"
)

#Check if service principal exists
export servicePrincipal=$(az ad sp list --display-name "azure-ai-chat-java-spi" --query [].appId --output tsv)

if [ -z "$servicePrincipal" ]; then
    echo "Service principal not found. Creating service principal"
    export servicePrincipal=$(az ad sp create-for-rbac --name "azure-ai-chat-java-spi" --role reader --scopes /subscriptions/"$AZURE_SUBSCRIPTION_ID"/resourceGroups/"$AZURE_RESOURCE_GROUP" --query appId --output tsv)
    if [ $? -ne 0 ]; then
        echo "Failed to create service principal"
        exit $?
    fi
    export servicePrincipalObjectId=$(az ad sp show --id "$servicePrincipal" --query id --output tsv)
    echo "Assigning Roles to service principal azure-ai-chat-java-spi with principal id:$servicePrincipal and object id[$servicePrincipalObjectId]"
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