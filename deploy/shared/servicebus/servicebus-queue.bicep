param location string = resourceGroup().location
param namespaceName string
param skuName string = 'Basic'
param queueName string
param deadLetterQueueName string = '${queueName}-dlq'
param tags object = {}

resource serviceBusNamespace 'Microsoft.ServiceBus/namespaces@2022-10-01-preview' = {
  name: namespaceName
  location: location
  tags: tags
  sku: {
    name: skuName
  }
}

resource deadLetterQueue 'Microsoft.ServiceBus/namespaces/queues@2022-10-01-preview' = {
  name: deadLetterQueueName
  parent: serviceBusNamespace
  properties: {
    requiresDuplicateDetection: false
    requiresSession: false
    enablePartitioning: false
  }
}

resource queues 'Microsoft.ServiceBus/namespaces/queues@2022-10-01-preview' =  {
  parent: serviceBusNamespace
  name: queueName
  dependsOn: [
    deadLetterQueue
  ]
  properties: {
    lockDuration: 'PT3M'
    forwardDeadLetteredMessagesTo: deadLetterQueueName
  }
}

output name string = serviceBusNamespace.name
output skuName string = serviceBusNamespace.sku.name
output queueName string = queues.name



