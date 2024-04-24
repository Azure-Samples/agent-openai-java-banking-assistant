metadata description = 'Creates an event grid system topic and Azure storage account event listener.'


param location string = resourceGroup().location
param systemTopicName string 
param subscriptionName string
param storageAccountName string
param serviceBusNamespaceName string
param queueName string


resource serviceBusNamespace 'Microsoft.ServiceBus/namespaces@2022-10-01-preview' existing = {
  name: serviceBusNamespaceName
}

resource queue 'Microsoft.ServiceBus/namespaces/queues@2022-10-01-preview' existing = {
  name: queueName
  parent: serviceBusNamespace
}

resource storage 'Microsoft.Storage/storageAccounts@2021-09-01' existing = {
  name: storageAccountName
}

resource eventgridSystemTopic 'Microsoft.EventGrid/systemTopics@2023-12-15-preview' = {
  name: systemTopicName
  location: location
  properties: {
    source: storage.id
    topicType: 'Microsoft.Storage.StorageAccounts'
  }
}

resource serviceBusEventGridSubscription 'Microsoft.EventGrid/systemTopics/eventSubscriptions@2023-12-15-preview' = {
  parent: eventgridSystemTopic
  name: subscriptionName
  properties: {
    destination: {
      properties: {
        resourceId: queue.id
      }
      endpointType: 'ServiceBusQueue'
    }
    filter: {
      
      //subjectBeginsWith: '/blobServices/default/containers/content'
      includedEventTypes: [
        'Microsoft.Storage.BlobCreated'
      ]
      enableAdvancedFilteringOnArrays: true
    }
    labels: []
    eventDeliverySchema: 'EventGridSchema'
    retryPolicy: {
      maxDeliveryAttempts: 30
      eventTimeToLiveInMinutes: 1440
    }
  }
}
