# Deployment Guide

## **🚀 Quick Start**
You can clone this repo and change directory to the root of the repo. Or you can run `azd init -t Azure-Samples/agent-openai-python-banking-assistant`.

Once you have the project available locally, run the following commands if you don't have any pre-existing Azure services and want to start from a fresh deployment.

1. Run 

    ```shell
    azd auth login
    ```

2. Run 

    ```shell
    azd up
    ```
    
    * This will provision Azure resources and deploy this sample to those resources.
    * The project has been tested with gpt-4o and gpt-4.1 model which is currently available with several deployment options these regions. The default is global standard. For more info on deployments and updated region availability check [here](https://learn.microsoft.com/en-us/azure/ai-foundry/foundry-models/concepts/models-sold-directly-by-azure?pivots=azure-openai&tabs=global-standard-aoai%2Cstandard-chat-completions%2Cglobal-standard#model-summary-table-and-region-availability)


3. After the application has been successfully deployed you will see a web app URL printed to the console.  Click that URL to interact with the application in your browser.  

It will look like the following:

!['Output from running azd up'](docs/assets/azd-success.png)

### **Important: Note for PowerShell Users**

If you encounter issues running PowerShell scripts due to the policy of not being digitally signed, you can temporarily adjust the `ExecutionPolicy` by running the following command in an elevated PowerShell session:

```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
```

This will allow the scripts to run for the current session without permanently changing your system's policy.

## 🛠️ Troubleshooting & Common Issues

**Before starting deployment**, be aware of these common issues and solutions:

| **Common Issue** | **Quick Solution** | **Full Guide Link** |
|-----------------|-------------------|---------------------|
| **ReadOnlyDisabledSubscription** | Check if you have an active subscription | [Troubleshooting Guide](./troubleshooting.md#readonlydisabledsubscription) |
| **InsufficientQuota** | Verify quota availability | [Quota Check Guide](./quota_check.md) |
| **ResourceGroupNotFound** | Create new environment with `azd env new` | [Troubleshooting Guide](./troubleshooting.md#resourcegroupnotfound) |
| **InvalidParameter (Workspace Name)** | Use compliant names (3-33 chars, alphanumeric) | [Troubleshooting Guide](./troubleshooting.md#workspace-name---invalidparameter) |
| **ResourceNameInvalid** | Follow Azure naming conventions | [Troubleshooting Guide](./troubleshooting.md#resourcenameinvalid) |

> **If you encounter deployment errors:** Refer to the [complete troubleshooting guide](./troubleshooting.md) with comprehensive error solutions.

## Redeploying Infra or App Code Changes

If you've only changed the backend/frontend code in the `app` folder, then you don't need to re-provision the Azure resources. You can just run:

```shell
azd deploy
```

If you've changed the infrastructure files (`infra` folder or `azure.yaml`), then you'll need to re-provision the Azure resources. You can do that by running:

```shell
azd up
```
 > [!WARNING]
 > When you run `azd up` multiple times to redeploy infrastructure, make sure to set the following parameters in `infra/main.parameters.json` to `true` to avoid container apps images from being overridden with default "mcr.microsoft.com/azuredocs/containerapps-helloworld" image:

```json
 "copilotAppExists": {
      "value": false
    },
    "webAppExists": {
      "value": false
    },
    "accountAppExists": {
      "value": false
    },
    "paymentAppExists": {
      "value": false
    },
    "transactionAppExists": {
      "value": false
    }
```

## Testing different gpt models, versions and sku.
The default LLM used in this project is *gpt-4.1* deployed with global standard on Azure AI Foundry.
You can test different models and versions by changing the model sections in the [infra/main.parameters.json](infra/main.parameters.json). An example:

```shell
"models": {
      "value": [
        {
          "deploymentName": "gpt-4.1",
          "name": "gpt-4.1",
          "format": "OpenAI",
          "version": "2025-04-14",
          "skuName": "GlobalStandard",
          "capacity": 80
        }
      ]
    }
```

## Running Agents locally
Once you have created the Azure resources with `azd up` or `azd provision`, you can run all the apps locally (instead of using Azure Container Apps). For more details on how to run each app check:
-  the [README.md](app/backend/README.md) to run the agents backend and the front-end
-  the [README.md](app/business-api/python/README.md) to run the simulated banking mcp servers.