targetScope = 'subscription'

@minLength(1)
@maxLength(64)
@description('Name of the the environment which is used to generate a short unique hash used in all resources.')
param environmentName string

@minLength(1)
@description('Primary location for all resources')
param location string

param resourceGroupName string = ''

param applicationInsightsName string = ''
param logAnalyticsName string = ''

param storageAccountName string = ''
param storageResourceGroupName string = ''
param storageResourceGroupLocation string = location
param storageContainerName string = 'content'
param storageSkuName string // Set in main.parameters.json

@description('The Azure AI Foundry resource group name. If ommited will be the same as the main resource group')
param foundryResourceGroupName string = ''
@description('The Azure AI Foundry resource name. If ommited will be generated')
param foundryResourceName string = ''
@description('The Azure AI Foundry Project name. If ommited will be generated')
param aiProjectName string = ''


// Look for the desired model in availability table. Default model is gpt-4o-mini:
// https://learn.microsoft.com/azure/ai-services/openai/concepts/models#standard-deployment-model-availability
@description('Location for the Foundry resource group')
@allowed([
  'australiaeast'
  'brazilsouth'
  'canadaeast'
  'eastus'
  'eastus2'
  'francecentral'
  'germanywestcentral'
  'japaneast'
  'koreacentral'
  'northcentralus'
  'norwayeast'
  'polandcentral'
  'southafricanorth'
  'southcentralus'
  'southindia'
  'spaincentral'
  'swedencentral'
  'switzerlandnorth'
  'uksouth'
  'westeurope'
  'westu'
  'westus3'
])
@metadata({
  azd: {
    type: 'location'
  }
})
param foundryResourceGroupLocation string = 'eastus'
param customFoundryResourceGroupLocation string = ''

@description('Array of models to deploy')
param models array = [
  {
    deploymentName: 'gpt-4.1'
    name: 'gpt-4.1'
    format: 'OpenAI'
    version: '2025-04-14'
    skuName: 'GlobalStandard'
    capacity: 120
  }

]


param documentIntelligenceServiceName string = ''
param documentIntelligenceResourceGroupName string = ''
//Document Intelligence new rest api available in eastus, westus2, westeurope. https://learn.microsoft.com/en-us/azure/ai-services/document-intelligence/sdk-overview-v4-0?view=doc-intel-4.0.0&tabs=csharp
@allowed(['eastus', 'westus2', 'westeurope'])
param documentIntelligenceResourceGroupLocation string = 'eastus'
param documentIntelligenceSkuName string = 'S0'

param containerAppsEnvironmentName string = ''
param containerRegistryName string = ''

param copilotContainerAppName string = ''
param webContainerAppName string = ''
param accountContainerAppName string = ''
param transactionContainerAppName string = ''
param paymentContainerAppName string = ''
param copilotAppExists bool = false
param webAppExists bool = false
param accountAppExists bool = false
param paymentAppExists bool = false
param transactionAppExists bool = false


var abbrs = loadJsonContent('shared/abbreviations.json')
var resourceToken = toLower(uniqueString(subscription().id, environmentName, location))
var tags = { 'azd-env-name': environmentName, 'assignedTo': environmentName }

// Organize resources in a resource group
resource resourceGroup 'Microsoft.Resources/resourceGroups@2021-04-01' = {
  name: !empty(resourceGroupName) ? resourceGroupName : '${abbrs.resourcesResourceGroups}${environmentName}'
  location: location
  tags: tags
}

resource foundryResourceGroup 'Microsoft.Resources/resourceGroups@2021-04-01' existing = if (!empty(foundryResourceGroupName)) {
  name: !empty(foundryResourceGroupName) ? foundryResourceGroupName : resourceGroup.name
}

resource documentIntelligenceResourceGroup 'Microsoft.Resources/resourceGroups@2021-04-01' existing = if (!empty(documentIntelligenceResourceGroupName)) {
  name: !empty(documentIntelligenceResourceGroupName) ? documentIntelligenceResourceGroupName : resourceGroup.name
}



resource storageResourceGroup 'Microsoft.Resources/resourceGroups@2021-04-01' existing = if (!empty(storageResourceGroupName)) {
  name: !empty(storageResourceGroupName) ? storageResourceGroupName : resourceGroup.name
}

// Monitor application with Azure Monitor
module monitoring 'shared/monitor/monitoring.bicep' = {
  name: 'monitoring'
  scope: resourceGroup
  params: {
    location: location
    tags: tags
    applicationInsightsName: !empty(applicationInsightsName) ? applicationInsightsName : '${abbrs.insightsComponents}${resourceToken}'
    logAnalyticsName: !empty(logAnalyticsName) ? logAnalyticsName : '${abbrs.operationalInsightsWorkspaces}${resourceToken}'
  }
}


module containerApps 'shared/host/container-apps.bicep' = {
  name: 'container-apps'
  scope: resourceGroup
  params: {
    name: 'app'
    location: location
    tags: tags
    containerAppsEnvironmentName: !empty(containerAppsEnvironmentName) ? containerAppsEnvironmentName : '${abbrs.appManagedEnvironments}${resourceToken}'
    containerRegistryName: !empty(containerRegistryName) ? containerRegistryName : '${abbrs.containerRegistryRegistries}${resourceToken}'
    logAnalyticsWorkspaceName: monitoring.outputs.logAnalyticsWorkspaceName
    applicationInsightsName: monitoring.outputs.applicationInsightsName
  }
}

// Copilot backend
module copilot 'app/copilot.bicep' = {
  name: 'copilot'
  scope: resourceGroup
  params: {
    name: !empty(copilotContainerAppName) ? copilotContainerAppName : '${abbrs.appContainerApps}copilot-${resourceToken}'
    location: location
    tags: tags
    identityName: '${abbrs.managedIdentityUserAssignedIdentities}copilot-${resourceToken}'
    applicationInsightsName: monitoring.outputs.applicationInsightsName
    containerAppsEnvironmentName: containerApps.outputs.environmentName
    containerRegistryName: containerApps.outputs.registryName
    corsAcaUrl: ''
    exists: copilotAppExists
    env: [
      {
        name: 'AZURE_STORAGE_ACCOUNT'
        value: storage.outputs.name
      }
      {
        name: 'AZURE_STORAGE_CONTAINER'
        value: storageContainerName
      }
     
      {
        name: 'FOUNDRY_PROJECT_ENDPOINT'
        value:  '${aiFoundry.outputs.endpoint}api/projects/${aiFoundry.outputs.aiProjectName}/'
      }
      {
        name: 'FOUNDRY_MODEL_DEPLOYMENT_NAME'
        value: models[0].deploymentName
      }
      {
        name: 'AZURE_OPENAI_ENDPOINT'
        value:  aiFoundry.outputs.openAIEndpoint
      }
      {
        name: 'AZURE_OPENAI_CHAT_DEPLOYMENT_NAME'
        value: models[0].deploymentName
      }
      {
        name: 'AZURE_DOCUMENT_INTELLIGENCE_SERVICE'
        value: documentIntelligence.outputs.name
      }
      {
        name: 'TRANSACTION_MCP_URL'
        value: '${transaction.outputs.SERVICE_API_URI}/mcp'
      }
      {
        name: 'PAYMENT_MCP_URL'
        value: payment.outputs.SERVICE_API_URI
      }
      {
        name: 'ACCOUNT_MCP_URL'
        value: account.outputs.SERVICE_API_URI
      }
      {
        name: 'APPLICATIONINSIGHTS_CONNECTION_STRING'
        value: monitoring.outputs.applicationInsightsInstrumentationKey
      }
     
    ]
  }
}

// Business Account Api
module account 'app/account.bicep' = {
  name: 'account'
  scope: resourceGroup
  params: {
    name: !empty(accountContainerAppName) ? accountContainerAppName : '${abbrs.appContainerApps}account-${resourceToken}'
    location: location
    tags: tags
    identityName: '${abbrs.managedIdentityUserAssignedIdentities}account-${resourceToken}'
    applicationInsightsName: monitoring.outputs.applicationInsightsName
    containerAppsEnvironmentName: containerApps.outputs.environmentName
    containerRegistryName: containerApps.outputs.registryName
    corsAcaUrl: ''
    exists: accountAppExists
   
  }
}

// Business Transactions Api
module transaction 'app/transaction.bicep' = {
  name: 'transaction'
  scope: resourceGroup
  params: {
    name: !empty(transactionContainerAppName) ? transactionContainerAppName : '${abbrs.appContainerApps}transaction-${resourceToken}'
    location: location
    tags: tags
    identityName: '${abbrs.managedIdentityUserAssignedIdentities}transaction-${resourceToken}'
    applicationInsightsName: monitoring.outputs.applicationInsightsName
    containerAppsEnvironmentName: containerApps.outputs.environmentName
    containerRegistryName: containerApps.outputs.registryName
    corsAcaUrl: ''
    exists: transactionAppExists
   
  }
}

// Business Payment Api
module payment 'app/payment.bicep' = {
  name: 'payment'
  scope: resourceGroup
  params: {
    name: !empty(paymentContainerAppName) ? paymentContainerAppName : '${abbrs.appContainerApps}payment-${resourceToken}'
    location: location
    tags: tags
    identityName: '${abbrs.managedIdentityUserAssignedIdentities}payment-${resourceToken}'
    applicationInsightsName: monitoring.outputs.applicationInsightsName
    containerAppsEnvironmentName: containerApps.outputs.environmentName
    containerRegistryName: containerApps.outputs.registryName
    corsAcaUrl: ''
    exists: paymentAppExists
    env: [
      {
        name: 'TRANSACTIONS_API_SERVER_URL'
        value: transaction.outputs.SERVICE_API_URI
      }
     
    ]
   
  }
}

module web 'app/web.bicep' = {
  name: 'web'
  scope: resourceGroup
  params: {
    name: !empty(webContainerAppName) ? webContainerAppName : '${abbrs.appContainerApps}web-${resourceToken}'
    location: location
    tags: tags
    identityName: '${abbrs.managedIdentityUserAssignedIdentities}web-${resourceToken}'
    apiBaseUrl:  copilot.outputs.SERVICE_API_URI
    applicationInsightsName: monitoring.outputs.applicationInsightsName
    containerAppsEnvironmentName: containerApps.outputs.environmentName
    containerRegistryName: containerApps.outputs.registryName
    exists: webAppExists
  }
}


// module openAi 'shared/ai/cognitiveservices.bicep' =  {
//   name: 'openai'
//   scope: openAiResourceGroup
//   params: {
//     name: !empty(openAiServiceName) ? openAiServiceName : '${abbrs.cognitiveServicesAccounts}${resourceToken}'
//     location: !empty(customOpenAiResourceGroupLocation) ? customOpenAiResourceGroupLocation : openAiResourceGroupLocation
//     tags: tags
//     sku: {
//       name: openAiSkuName
//     }
//     deployments: [
//       {
//         name: chatGptDeploymentName
//         model: {
//           format: 'OpenAI'
//           name: chatGptModelName
//           version: chatGptModelVersion
//         }
//         sku: {
//           name: chatGptDeploymentSkuName
//           capacity: chatGptDeploymentCapacity
//         }
//       }
      
//     ]
//   }
// }

module aiFoundry 'shared/ai/foundry.bicep' = {
 name: 'ai-foundry'
 scope: foundryResourceGroup
  params: {
    aiProjectName: !empty(aiProjectName) ? aiProjectName : 'proj-${resourceToken}'
    aiProjectFriendlyName: 'Banking Assistant Project'
    aiProjectDescription: 'Project for the Banking Assistant Copilot using Azure AI Foundry'
    foundryResourceName: !empty(foundryResourceName) ? foundryResourceName : 'foundry-${resourceToken}'
    location: foundryResourceGroupLocation
    tags: tags
  }
}

@batchSize(1)
module foundryModelDeployments 'shared/ai/foundry-model-deployment.bicep' = [for (model, index) in models: {
  name: 'foundry-model-deployment-${model.name}-${index}'
  scope: foundryResourceGroup
   params: {
    foundryResourceName: aiFoundry.outputs.accountName
    deploymentName: model.deploymentName
    modelName: model.name
    modelFormat: model.format
    modelVersion: model.version
    modelSkuName: model.skuName
    modelCapacity: model.capacity
    tags: tags
  }
}]



module documentIntelligence 'shared/ai/cognitiveservices.bicep' = {
  name: 'documentIntelligence'
  scope: documentIntelligenceResourceGroup
  params: {
    name: !empty(documentIntelligenceServiceName) ? documentIntelligenceServiceName : '${abbrs.cognitiveServicesFormRecognizer}${resourceToken}'
    kind: 'FormRecognizer'
    location: documentIntelligenceResourceGroupLocation
    tags: tags
    sku: {
      name: documentIntelligenceSkuName
    }
  }
}



module storage 'shared/storage/storage-account.bicep' = {
  name: 'storage'
  scope: storageResourceGroup
  params: {
    name: !empty(storageAccountName) ? storageAccountName : '${abbrs.storageStorageAccounts}${resourceToken}'
    location: storageResourceGroupLocation
    tags: tags
    allowBlobPublicAccess: false
    publicNetworkAccess: 'Enabled'
    sku: {
      name: storageSkuName
    }
    deleteRetentionPolicy: {
      enabled: true
      days: 2
    }
    containers: [
      {
        name: storageContainerName
        publicAccess: 'None'
      }
    ]
  }
}




// SYSTEM IDENTITIES

module foundryCognitiveUserRoleBackend 'shared/security/role.bicep' =  {
  scope: foundryResourceGroup
  name: 'foundry-cognitive-user-role-backend'
  params: {
    principalId: copilot.outputs.SERVICE_API_IDENTITY_PRINCIPAL_ID
    roleDefinitionId: '5e0bd9bd-7b93-4f28-af87-19fc36ad61bd'
    principalType: 'ServicePrincipal'
  }
}

module foundryAIDeveloperRoleBackend 'shared/security/role.bicep' =  {
  scope: foundryResourceGroup
  name: 'foundry-ai-developerrole-backend'
  params: {
    principalId: copilot.outputs.SERVICE_API_IDENTITY_PRINCIPAL_ID
    roleDefinitionId: '64702f94-c441-49e6-a78b-ef80e0188fee'
    principalType: 'ServicePrincipal'
  }
}

module storageRoleBackend 'shared/security/role.bicep' = {
  scope: storageResourceGroup
  name: 'storage-role-backend'
  params: {
    principalId: copilot.outputs.SERVICE_API_IDENTITY_PRINCIPAL_ID
    roleDefinitionId: 'ba92f5b4-2d11-453d-a403-e96b0029c9fe'
    principalType: 'ServicePrincipal'
  }
}

module documentIntelligenceRoleCopilot 'shared/security/role.bicep' = {
  scope: documentIntelligenceResourceGroup
  name: 'documentIntelligence-role-copilot'
  params: {
    principalId: copilot.outputs.SERVICE_API_IDENTITY_PRINCIPAL_ID
    roleDefinitionId: 'a97b65f3-24c7-4388-baec-2e87135dc908'
    principalType: 'ServicePrincipal'
  }
}

output AZURE_LOCATION string = location
output AZURE_TENANT_ID string = tenant().tenantId
output AZURE_RESOURCE_GROUP string = resourceGroup.name


output AZURE_CONTAINER_ENVIRONMENT_NAME string = containerApps.outputs.environmentName
output AZURE_CONTAINER_REGISTRY_ENDPOINT string = containerApps.outputs.registryLoginServer
output AZURE_CONTAINER_REGISTRY_NAME string = containerApps.outputs.registryName

// Shared by all OpenAI deployments


// Specific to Azure Foundry
output FOUNDRY_PROJECT_ENDPOINT string =  '${aiFoundry.outputs.endpoint}api/projects/${aiFoundry.outputs.aiProjectName}/'
output FOUNDRY_RESOURCE_NAME string = aiFoundry.outputs.accountName
output FOUNDRY_CHATGPT_DEPLOYMENT string = models[0].deploymentName


output AZURE_DOCUMENT_INTELLIGENCE_SERVICE string = documentIntelligence.outputs.name
output AZURE_DOCUMENT_INTELLIGENCE_RESOURCE_GROUP string = documentIntelligenceResourceGroup.name


output AZURE_STORAGE_ACCOUNT string = storage.outputs.name
output AZURE_STORAGE_CONTAINER string = storageContainerName
output AZURE_STORAGE_RESOURCE_GROUP string = storageResourceGroup.name



// output BACKEND_URI string = backend.outputs.uri
// output INDEXER_FUNCTIONAPP_NAME string = indexer.outputs.name
