// ai-services.bicep
// This file contains the AI services resources for the agent workshop

// Parameters
@description('Name for the project')
param aiProjectName string

@description('Friendly name for your Azure AI resource')
param aiProjectFriendlyName string = 'Foundry Project Basic Setup'

@description('Description of your Azure AI resource dispayed in Azure AI Foundry')
param aiProjectDescription string = 'Foundry Project for AI Services'

@description('Set of tags to apply to all resources.')
param tags object = {}

@description('Location for the Azure AI Foundry resource')
param location string

@description('Name of the Azure AI Foundry account')
@minLength(3)
@maxLength(24)
param foundryResourceName string

resource account 'Microsoft.CognitiveServices/accounts@2025-04-01-preview' = {
  name: foundryResourceName
  location: location
  sku: {
    name: 'S0'
  }
  kind: 'AIServices'
  identity: {
    type: 'SystemAssigned'
  }
  properties: {
    apiProperties: {}
    allowProjectManagement: true
    customSubDomainName: foundryResourceName
    networkAcls: {
      defaultAction: 'Allow'
      virtualNetworkRules: []
      ipRules: []
    }
    publicNetworkAccess: 'Enabled'
    disableLocalAuth: true
    defaultProject: aiProjectName
    associatedProjects: [aiProjectName]
  }
  tags: tags
}

resource project 'Microsoft.CognitiveServices/accounts/projects@2025-04-01-preview' = {
  parent: account
  name: aiProjectName
  location: location
  identity: {
    type: 'SystemAssigned'
  }
  properties: {
    description: aiProjectDescription
    displayName: aiProjectFriendlyName
    // Note: Direct Application Insights telemetry configuration is not yet supported 
    // in the current API version. Manual configuration required in Azure portal.
  }
  tags: tags
}


output accountName string = account.name
output endpoint string = account.properties.endpoints['AI Foundry API']
output aiProjectId string = project.id
output aiProjectName string = project.name
