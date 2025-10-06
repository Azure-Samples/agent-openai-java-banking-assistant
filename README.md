---
page_type: sample
languages:
- azdeveloper
- python
- bicep
- typescript
- html
products:
- ai-services 
- azure
- azure-openai
- active-directory
- azure-cognitive-search
- azure-container-apps
- azure-sdks
- github
- document-intelligence
- azure-monitor
- azure-pipelines
urlFragment: agent-openai-python-banking-assistant
name: Multi Agents Banking Assistant with Python and Microsoft Agent Framework
description: A Python sample app emulating a personal banking AI-powered assistant to inquire about account balances, review recent transactions, or initiate payments
---
<!-- YAML front-matter schema: https://review.learn.microsoft.com/en-us/help/contribute/samples/process/onboarding?branch=main#supported-metadata-fields-for-readmemd -->
<!-- prettier-ignore -->
<div align="center">

![](./docs/assets/robot-agents-small.png)

# Multi Agent Banking Assistant with Python and Microsoft Agent Framework


:star: If you like this sample, star it on GitHub — it helps a lot!

[Overview](#overview) • [Architecture](#agents-concepts-and-architectures) • [Get started](#getting-started) •  [Resources](#resources) • [FAQ](#faq) • [Troubleshooting](#troubleshooting)

![](./docs/assets/ui.gif)
</div>



## Overview
The core use case of this Proof of Concept (PoC) revolves around a banking personal assistant designed to revolutionize the way users interact with their bank account information, transaction history, and payment functionalities. Utilizing the power of generative AI within a multi-agent architecture, this assistant aims to provide a seamless, conversational interface through which users can effortlessly access and manage their financial data.

Instead of navigating through traditional web interfaces and menus, users can simply converse with the AI-powered assistant to inquire about their account balances, review recent transactions, or initiate payments. This approach not only enhances user experience by making financial management more intuitive and accessible but also leverages the existing workload data and APIs to ensure a reliable and secure service.

Invoices samples are included in the data folder to make it easy to explore payments feature. The payment agent equipped with OCR tools ( Azure Document Intelligence) will lead the conversation with the user to extract the invoice data and initiate the payment process. Other account fake data as transactions, payment methods and account balance are also available to be queried by the user. All data and services are exposed as external REST APIs and **MCP tools** consumed by the agents to provide the user with the requested information.

<div align="center" >
<p> <strong>This sample is powered by:</strong></p>


<img src="./docs/assets/ag-banner.png" width="400" alt="Agent Framework Banner">


</div>

<br />

For Semantic Kernel version check this [branch](https://github.com/Azure-Samples/agent-openai-python-banking-assistant/tree/semantic-kernel)


## Features 
This project provides the following features and technical patterns:
 - Simple multi-agent supervisor architecture using **gpt-4.1** on [Azure AI Foundry](https://azure.microsoft.com/en-us/products/ai-foundry)
 - Exposing your business API as MCP tools for your agents using [fastmcp](https://gofastmcp.com/getting-started/welcome)
 - Agents tools configuration and automatic tools invocations with [Agent Framework](https://learn.microsoft.com/en-us/agent-framework/overview/agent-framework-overview).
 - Chat based conversation implemented as [React Single Page Application](https://react.fluentui.dev/?path=/docs/concepts-introduction--docs) with support for images upload.Supported images are invoices, receipts, bills jpeg/png files you want your virtual banking assistant to pay on your behalf.
 - Images scanning and data extraction with Azure Document Intelligence using [prebuilt-invoice](https://learn.microsoft.com/en-us/azure/ai-services/document-intelligence/concept-invoice?view=doc-intel-4.0.0) model.
 - Add an agentic app side-by-side to your existing business microservices hosted on [Azure Container Apps](https://azure.microsoft.com/en-us/products/container-apps).
 - Automated Azure resources creation and solution deployment leveraging [Azure Developer CLI](https://learn.microsoft.com/en-us/azure/developer/azure-developer-cli/).


### Architecture
![HLA](docs/assets/HLA-Agent-Framework.png)
The personal banking assistant is designed as conversational multi-agent system with each agent specializing in a specific functional domain (e.g., account management, transaction history, payments). The architecture consists of the following key components:

- **Copilot Assistant Copilot App (Microservice)**: Serves as the central hub for processing user chat requests. It's a [FastAPI](https://fastapi.tiangolo.com/) app which uses  **agent-framework** to create Agents equipped with tools and orchestrate them using [hand-off pattern](https://learn.microsoft.com/en-us/azure/architecture/ai-ml/guide/ai-agent-design-patterns#handoff-orchestration).
    - **Supervisor Agent**: It's responsible to triage the user request, and delegate the task to the specialized domain agent. This component ensures that user queries are efficiently handled by the relevant agent. Agents are engaged by the supervisor in a single turn conversation meaning that only one is selected by the supervisor to answer to user task. It’s just doing routing logic and passing the control to the domain agent which
    will either carry-on the task in one shot or will involve user feedback if data oversight or action approval (like payment submit) is required.
    
    - **Account Agent**: Specializes in handling tasks related to banking account information, credit balance, and registered payment methods. It leverages specific Account service APIs to fetch and manage account-related data. The Microsoft Agent Framework is used to create account specific tools definition from the MCP server and automatically call the HTTP endpoint with input parameters extracted by gpt4 model from the chat conversation.

    - **Transactions Agent**: Focuses on tasks related to querying user bank movements, including income and outcome payments. This agent accesses account mcp server to retrieve accountid and transaction history mcp server to search for transactions and present them to the user.

    - **Payments Agent**: Focuses on managing tasks related to submitting payments. It interacts with multiple MCP servers and tools, such as ScanInvoice (backed by Azure Document Intelligence), Account Service to retrieve account and payment methods info, Payment Service to submit payment processing and Transaction History service to check for previous paid invoices.

- **Existing Business APIs**: Interfaces with the backend systems to perform operations related to personal banking accounts, transactions, and invoice payments. These APIs are implemented as external spring boot microservices providing the necessary data and functionality consumed by agents to execute their tasks. They are exposed as MCP endpoints using [FastMCP](https://gofastmcp.com/getting-started/welcome) to be consumed by agents.
    - **Account MCP Service (Microservice)**: Provides functionalities like retrieving account details by username, fetching payment methods, and getting registered beneficiaries. This microservice supports all 3 agents.

    - **Payments MCP Service (Microservice)**: Offers capabilities to submit payments and notify transactions. It is a critical component for the Payments Agent to execute payment-related tasks efficiently.

    - **Reporting MCP Service (Microservice)**: Enables searching transactions and retrieving transactions by recipient. This service supports the Transactions Agent in providing detailed transaction reports to the user and the Payment Agent as it needs to check if an invoice has not been already paid.

## Getting Started


### Prerequisites

* [Python >= 3.11](https://www.python.org/downloads/release/python-31113/)
* [uv](https://github.com/astral-sh/uv)
* [Azure Developer CLI](https://aka.ms/azure-dev/install)
* [Node.js](https://nodejs.org/en/download/)
* [Git](https://git-scm.com/downloads)
* [Powershell 7+ (pwsh)](https://github.com/powershell/powershell) - For Windows users only.
  * **Important**: Ensure you can run `pwsh.exe` from a PowerShell command. If this fails, you likely need to upgrade PowerShell.


> [!WARNING]
> Your Azure Account must have `Microsoft.Authorization/roleAssignments/write` permissions, such as [User Access Administrator](https://learn.microsoft.com/azure/role-based-access-control/built-in-roles#user-access-administrator) or [Owner](https://learn.microsoft.com/azure/role-based-access-control/built-in-roles#owner).  

### Starting from scratch

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


### Redeploying

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

### Running Agents locally
Once you have created the Azure resources with `azd up` or `azd provision`, you can run all the apps locally (instead of using Azure Container Apps). For more details on how to run each app check:
-  the [README.md](app/copilot/README.md) to run the copilot chat service and the front-end
-  the [README.md](app/business-api/python/README.md) to run the simulated banking mcp servers.


## Guidance

### Testing different gpt models, versions and sku.
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

### Enabling authentication

By default, the web app on ACA will have no authentication or access restrictions enabled, meaning anyone with routable network access to the web app can chat with your personal assistant.You can require authentication to your Microsoft Entra by following the [Add app authentication](https://learn.microsoft.com/en-us/azure/container-apps/authentication) tutorial and set it up against the deployed web app.


To then limit access to a specific set of users or groups, you can follow the steps from [Restrict your Microsoft Entra app to a set of users](https://learn.microsoft.com/entra/identity-platform/howto-restrict-your-app-to-a-set-of-users) by changing "Assignment Required?" option under the Enterprise Application, and then assigning users/groups access.  Users not granted explicit access will receive the error message -AADSTS50105: Your administrator has configured the application <app_name> to block users 


### Cost estimation

Pricing varies per region and usage, so it isn't possible to predict exact costs for your usage.
However, you can try the [Azure pricing calculator](https://azure.com/e/8ffbe5b1919c4c72aed89b022294df76) for the resources below.

- Azure Containers App: Consumption workload profile with 4 CPU core and 8 GB RAM. Pricing per vCPU and Memory. [Pricing](https://azure.microsoft.com/en-us/pricing/details/container-apps/)
- Azure OpenAI: Standard tier, ChatGPT and Ada models. Pricing per 1K tokens used, and at least 1K tokens are used per question. [Pricing](https://azure.microsoft.com/en-us/pricing/details/cognitive-services/openai-service/)
- Azure Document Intelligence: SO (Standard) tier using pre-built layout. [Pricing](https://azure.microsoft.com/pricing/details/form-recognizer/)

- Azure Blob Storage: Standard tier with ZRS (Zone-redundant storage). Pricing per storage and read operations. [Pricing](https://azure.microsoft.com/pricing/details/storage/blobs/)
- Azure Monitor: Pay-as-you-go tier. Costs based on data ingested. [Pricing](https://azure.microsoft.com/pricing/details/monitor/)

The first 180,000 vCPU-seconds, 360,000 GiB-seconds, and 2 million requests each month are free for ACA. To reduce costs, you can switch to free SKUs Document Intelligence by changing the parameters file under the `infra` folder. There are some limits to consider; for example, the free resource only analyzes the first 2 pages of each document. 

⚠️ To avoid unnecessary costs, remember to take down your app if it's no longer in use,
either by deleting the resource group in the Portal or running `azd down`.


## Resources

Here are some resources to learn more about multi-agent architectures and technologies used in this sample:

- [Microsoft Agent Framework](https://github.com/microsoft/agent-framework)
- [AI agents For Beginners](https://github.com/microsoft/ai-agents-for-beginners)
- [Azure AI Foundry](https://learn.microsoft.com/en-us/azure/ai-foundry/what-is-azure-ai-foundry)
- [Develop AI apps using Azure services](https://aka.ms/azai)
- [Building Effective Agents - Anthropic](https://www.anthropic.com/engineering/building-effective-agents)
- [AI agent orchestration patterns](https://learn.microsoft.com/en-us/azure/architecture/ai-ml/guide/ai-agent-design-patterns)


You can also find [more Azure Ai agents samples here](https://aka.ms/aiapps)


## Getting Help

If you get stuck or have any questions about building AI apps, join:

[![Azure AI Foundry Discord](https://img.shields.io/badge/Discord-Azure_AI_Foundry_Community_Discord-blue?style=for-the-badge&logo=discord&color=5865f2&logoColor=fff)](https://aka.ms/foundry/discord)

If you have product feedback or errors while building visit:

[![Azure AI Foundry Developer Forum](https://img.shields.io/badge/GitHub-Azure_AI_Foundry_Developer_Forum-blue?style=for-the-badge&logo=github&color=000000&logoColor=fff)](https://aka.ms/foundry/forum)

## Troubleshooting

If you have any issue when running or deploying this sample [open an issue](https://https://github.com/Azure-Samples/agent-openai-python-banking-assistant/issues) in this repository.

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a
Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us
the rights to use your contribution. For details, visit https://cla.opensource.microsoft.com.

When you submit a pull request, a CLA bot will automatically determine whether you need to provide
a CLA and decorate the PR appropriately (e.g., status check, comment). Simply follow the instructions
provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or
contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

## Trademarks

This project may contain trademarks or logos for projects, products, or services. Authorized use of Microsoft
trademarks or logos is subject to and must follow
[Microsoft's Trademark & Brand Guidelines](https://www.microsoft.com/en-us/legal/intellectualproperty/trademarks/usage/general).
Use of Microsoft trademarks or logos in modified versions of this project must not cause confusion or imply Microsoft sponsorship.
Any use of third-party trademarks or logos are subject to those third-party's policies.

[Langchain4j]: https://github.com/langchain4j/langchain4j
