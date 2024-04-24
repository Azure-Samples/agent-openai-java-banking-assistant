metadata description = 'Creates an Azure Container Apps environment.'
param name string
param location string = resourceGroup().location
param tags object = {}

@description('Name of the Application Insights resource')
param applicationInsightsName string = ''

@description('Specifies if Dapr is enabled')
param daprEnabled bool = false

@description('Name of the Log Analytics workspace')
param logAnalyticsWorkspaceName string

@description('Optional, default value is false. Sets if the environment will use availability zones. Your Container App Environment and the apps in it will be zone redundant. This requieres vNet integration.')
param zoneRedundant bool = false

@description('Optional, the workload profiles required by the end user. The default is "Consumption", and is automatically added whether workload profiles are specified or not.')
param workloadProfiles array = []
// Example of a workload profile below:
// [ {
//     workloadProfileType: 'D4'  // available types can be found here: https://learn.microsoft.com/en-us/azure/container-apps/workload-profiles-overview#profile-types
//     name: '<name of the workload profile>'
//     minimumCount: 1
//     maximumCount: 3
//   }
// ]

var defaultWorkloadProfile = [
  {
    workloadProfileType: 'Consumption'
    name: 'Consumption'
  }
]

var effectiveWorkloadProfiles = workloadProfiles != [] ? concat(defaultWorkloadProfile, workloadProfiles) : defaultWorkloadProfile

resource containerAppsEnvironment 'Microsoft.App/managedEnvironments@2023-04-01-preview' = {
  name: name
  location: location
  tags: tags
  properties: {
    zoneRedundant: zoneRedundant
    appLogsConfiguration: {
      destination: 'log-analytics'
      logAnalyticsConfiguration: {
        customerId: logAnalyticsWorkspace.properties.customerId
        sharedKey: logAnalyticsWorkspace.listKeys().primarySharedKey
      }
    }
    workloadProfiles: effectiveWorkloadProfiles
    daprAIInstrumentationKey: daprEnabled && !empty(applicationInsightsName) ? applicationInsights.properties.InstrumentationKey : ''
  }
}

resource logAnalyticsWorkspace 'Microsoft.OperationalInsights/workspaces@2022-10-01' existing = {
  name: logAnalyticsWorkspaceName
}

resource applicationInsights 'Microsoft.Insights/components@2020-02-02' existing = if (daprEnabled && !empty(applicationInsightsName)) {
  name: applicationInsightsName
}

output defaultDomain string = containerAppsEnvironment.properties.defaultDomain
output id string = containerAppsEnvironment.id
output name string = containerAppsEnvironment.name
