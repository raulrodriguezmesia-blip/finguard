param location string = 'eastus'
param appName string = '${{env.AZD_ENV_NAME}}'
var sqlPassword = 'ChangeMe!123' // En producción, usar Key Vault o parámetros seguros

@description('Application Insights instance for monitoring.')
resource appInsights 'Microsoft.Insights/components@2020-02-02' = {
  name: '${appName}-insights'
  location: location
  kind: 'web'
  properties: {
    Application_Type: 'web'
    Flow_Type: 'Bluefield'
    RetentionInDays: 90
    DisableIpMasking: false
    ImmediatePurgeDataOn30Days: false
    SamplingPercentage: 100
  }
}

@description('Log Analytics workspace for centralized logging.')
resource logAnalyticsWorkspace 'Microsoft.OperationalInsights/workspaces@2021-06-01' = {
  name: '${appName}-logs'
  location: location
  sku: {
    name: 'PerGB2018'
  }
  properties: {
    retentionInDays: 30
  }
}

@description('Key Vault for storing secrets.')
resource keyVault 'Microsoft.KeyVault/vaults@2022-07-01' = {
  name: '${appName}-kv'
  location: location
  properties: {
    tenantId: subscription().tenantId
    sku: {
      family: 'A'
      name: 'standard'
    }
    accessPolicies: []
    enabledForDiskEncryption: true
    enabledForDeployment: true
    enabledForTemplateDeployment: true
    enableRbacAuthorization: false
    networkAcls: {
      defaultAction: 'Allow'
      bypass: 'AzureServices'
    }
  }
}

@description('Azure Container Registry for storing container images.')
resource containerRegistry 'Microsoft.ContainerRegistry/registries@2023-07-02' = {
  name: '${appName}cr'
  location: location
  sku: {
    name: 'Basic'
  }
  properties: {
    adminUserEnabled: true
  }
}

@description('Azure SQL Database for transactional data.')
resource sqlServer 'Microsoft.Sql/servers@2022-05-01-preview' = {
  name: '${appName}-sql'
  location: location
  properties: {
    administratorLogin: 'sqladmin'
    administratorLoginPassword: sqlPassword
    version: '12.0'
  }
}

resource sqlDatabase 'Microsoft.Sql/servers/databases@2022-05-01-preview' = {
  name: '${appName}-db'
  location: location
  properties: {
    collation: 'SQL_Latin1_General_CP1_CI_AS'
    maxSizeGB: 10
    sku: {
      name: 'Basic'
      tier: 'Basic'
    }
  }
  parent: sqlServer
}

@description('Azure Cache for Redis for caching layer.')
resource redisCache 'Microsoft.Cache/redis@2023-08-01' = {
  name: '${appName}-redis'
  location: location
  properties: {
    sku: {
      name: 'Basic'
      family: 'C'
      capacity: 1
    }
    redisConfiguration: {
      maxmemoryPolicy: 'allkeys-lru'
      maxmemoryReserved: '0'
    }
    enableNonSslPort: false
    tenantSettings: {}
    shardCount: 0
  }
}

@description('Service Bus for event streaming and messaging.')
resource serviceBusNamespace 'Microsoft.ServiceBus/namespaces@2022-10-01-preview' = {
  name: '${appName}-sb'
  location: location
  sku: {
    name: 'Basic'
    tier: 'Standard'
  }
  properties: {
    zoneRedundant: false
  }
}

resource serviceBusQueue 'Microsoft.ServiceBus/namespaces/queues@2022-10-01-preview' = {
  name: '${appName}-queue'
  location: location
  properties: {
    maxSizeInMegabytes: 1024
    requiresDuplicateDetection: false
    defaultMessageTimeToLive: 'PT1440M'
    deadLetteringOnMessageExpiration: false
    duplicateDetectionHistoryTimeWindow: 'PT10M'
    maxDeliveryCount: 10
    lockDuration: 'PT30S'
    requireSession: false
    enablePartitioning: false
    enableExpress: false
    forwardDeadLetteredMessagesTo: ''
    forwardTo: ''
    autoDeleteOnIdle: 'P10675199DT2H48M5.4775807S'
  }
  parent: serviceBusNamespace
}

@description('App Service Plan for hosting web applications.')
resource appServicePlan 'Microsoft.Web/serverfarms@2022-03-01' = {
  name: '${appName}-plan'
  location: location
  properties: {
    reserved: true
    sku: {
      name: 'B1'
      tier: 'Basic'
      size: 'B1'
      family: 'B'
    }
  }
}

@description('Web App for hosting the API.')
resource webApp 'Microsoft.Web/sites@2022-03-01' = {
  name: appName
  location: location
  kind: 'app,linux,container'
  properties: {
    serverFarmId: appServicePlan.id
    siteConfig: {
      linuxFxVersion: 'DOCKER|acrlogin.azurecr.io/${appName}cr/${appName}:latest'
      alwaysOn: true
      ftpsState: 'Disabled'
      minTlsVersion: '1.2'
      http20Enabled: true
      websocketsEnabled: true
      applicationInsights: {
        connectionString: appInsights.properties.ConnectionString
      }
      appSettings: [
        {
          name: 'JAVA_OPTS'
          value: '-Xmx512m -Xms256m'
        }
        {
          name: 'SPRING_PROFILES_ACTIVE'
          value: 'prod'
        }
        {
          name: 'APPLICATIONINSIGHTS_CONNECTION_STRING'
          value: appInsights.properties.ConnectionString
        }
        {
          name: 'LOGGING_LEVEL_ROOT'
          value: 'INFO'
        }
        {
          name: 'KEYVAULT_NAME'
          value: keyVault.name
        }
        {
          name: 'REDIS_HOST'
          value: redisCache.properties.hostName
        }
        {
          name: 'REDIS_PORT'
          value: redisCache.properties.port
        }
        {
          name: 'SPRING_DATASOURCE_URL'
          value: 'jdbc:sqlserver://${sqlServer.name}.database.windows.net:1433;database=${sqlDatabase.name};encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;'
        }
        {
          name: 'SPRING_DATASOURCE_USERNAME'
          value: 'sqladmin'
        }
        {
          name: 'SPRING_DATASOURCE_PASSWORD'
          value: sqlPassword
        }
        {
          name: 'SERVICE_BUS_CONNECTION_STRING'
          value: listKeys(serviceBusNamespace.id, '2022-10-01-preview').primaryConnectionString
        }
        {
          name: 'SERVICE_BUS_QUEUE_NAME'
          value: serviceBusQueue.name
        }
      ]
    }
    identity: {
      type: 'SystemAssigned'
    }
  }
}