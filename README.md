# Personal Finance Assistant with Azure OpenAI - Java

## App Continuos Integration

1. **Create a Service Principal for the github action pipeline**

    Use [az ad sp create-for-rbac](https://learn.microsoft.com/en-us/cli/azure/ad/sp#az_ad_sp_create_for_rbac) to create the service principal:
    
    ```bash
    groupId=$(az group show --name <resource-group-name>  --query id --output tsv)
    az ad sp create-for-rbac --name "virtual-ai-agent-java-pipeline-spi" --role contributor --scope $groupId --sdk-auth
    ```
    Output is similar to:
    
    ```json
    {
    "clientId": "xxxx6ddc-xxxx-xxxx-xxx-ef78a99dxxxx",
    "clientSecret": "xxxx79dc-xxxx-xxxx-xxxx-aaaaaec5xxxx",
    "subscriptionId": "xxxx251c-xxxx-xxxx-xxxx-bf99a306xxxx",
    "tenantId": "xxxx88bf-xxxx-xxxx-xxxx-2d7cd011xxxx",
    "activeDirectoryEndpointUrl": "https://login.microsoftonline.com",
    "resourceManagerEndpointUrl": "https://management.azure.com/",
    "activeDirectoryGraphResourceId": "https://graph.windows.net/",
    "sqlManagementEndpointUrl": "https://management.core.windows.net:8443/",
    "galleryEndpointUrl": "https://gallery.azure.com/",
    "managementEndpointUrl": "https://management.core.windows.net/"
    } 
    ```
    
    Save the JSON output because it is used in a later step. Also, take note of the clientId, which you need to update the service principal in the next section.

2. **Assign ACRPush permission to service Principal**
   
   This step enables the GitHub workflow to use the service principal to [authenticate with your container registry](https://learn.microsoft.com/en-us/azure/container-registry/container-registry-auth-service-principal) and to push a Docker image.
   Get the resource ID of your container registry. Substitute the name of your registry in the following az acr show command:
   ```bash
   registryId=$(az acr show --name <registry-name> --resource-group <resource-group-name> --query id --output tsv)
    ```

   Use [az role assignment create](https://learn.microsoft.com/en-us/cli/azure/role/assignment#az_role_assignment_create) to assign the AcrPush role, which gives push and pull access to the registry. Substitute the client ID of your service principal:
   ```bash
   az role assignment create --assignee <ClientId> --scope $registryId --role AcrPush
   ```

3. **Add the service principal to your GitHub environment secrets**

 - Go to your forked repository in GitHub and create an [environment]((https://docs.github.com/en/actions/deployment/targeting-different-environments/using-environments-for-deployment)) called 'Development' (yes this is the exact name; don't change it). If you want to change the environment name (also adding new branches and environments, change the current branch/env mapping) you can do that, but make sure to change the pipeline code accordingly in `.github/workflows/apps-ci.yml`.
 - Create 'Development' environment [secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets#creating-encrypted-secrets-for-a-repository) as below:
    | Secret                | Value                                                                                      |
    |-----------------------|--------------------------------------------------------------------------------------------|
    | AZURE_CREDENTIALS     | The entire JSON output from the service principal creation step                            |
3. Create 'Development' environment [variables](https://docs.github.com/en/actions/learn-github-actions/variables#creating-configuration-variables-for-an-environment) as below:
    | Variable                | Value                                                                                        |
    |---------------------------|--------------------------------------------------------------------------------------------|
    | ACR_NAME                  | The name of the Azure Container registry                                                   |
    | COPILOT_ACA_APP_NAME      | The container app name for the copilot orchestrator app                                    |
    | WEB_ACA_APP_NAME          | The container app name for the web frontend  app                                           |
    | ACCOUNTS_ACA_APP_NAME     | The container app name for the business account api                                        |
    | PAYMENTS_ACA_APP_NAME     | The container app name for the business payment api                                        |
    | TRANSACTIONS_ACA_APP_NAME | The container app name for the business payment api                                        |
    | RESOURCE_GROUP            | The name of the resource group where your Azure Container Environment has been deployed    |

