# Multi-Agent Banking Assistant - Technical Documentation

## Technical Stack

### Backend Technologies

#### Core Framework
- **Java 17+**: Primary programming language for backend services
- **Spring Boot 3.3.6**: Application framework for microservices architecture
- **Spring Security**: Authentication and authorization implementation
- **Maven**: Build and dependency management tool

#### AI & Agent Framework
- **Langchain4j 1.0.0-beta2**: Agent orchestration and tool invocation framework
  - Multi-agent supervisor pattern implementation
  - Automatic tool binding and execution
  - Conversation memory and context management
- **Azure OpenAI Service**: LLM provider
  - Models: GPT-4o-mini or GPT-4o
  - Chat completion API integration
- **Spring AI MCP (Model Context Protocol)**: Framework for exposing business APIs as agent tools
  - Automatic tool registration from REST APIs
  - Tool parameter extraction and validation

#### Azure Services
- **Azure Document Intelligence**: OCR and document data extraction
  - Prebuilt invoice model for payment processing
- **Azure Container Apps**: Microservices hosting platform
- **Azure Container Registry**: Docker image repository
- **Azure Key Vault**: Secrets and configuration management
- **Azure Application Insights**: Application performance monitoring and telemetry
- **Azure Log Analytics**: Centralized logging and diagnostics
- **Azure Cognitive Search**: (Infrastructure provisioned for future enhancements)

#### Infrastructure & DevOps
- **Azure Bicep**: Infrastructure as Code (IaC) templates
- **Azure Developer CLI (azd)**: Deployment automation and environment management
- **Docker & Docker Compose**: Container orchestration for local development
- **GitHub Actions**: CI/CD pipeline automation

### Frontend Technologies

#### Core Stack
- **React 18**: UI library for building component-based interfaces
- **TypeScript 5.2**: Type-safe JavaScript for development
- **Vite 6.3**: Modern build tool and development server

#### UI Components & Libraries
- **Fluent UI v8 & v9**: Microsoft's design system
  - @fluentui/react: UI components library
  - @fluentui/react-components: Next-generation components
  - @fluentui/react-icons: Icon library
- **React Router DOM 6**: Client-side routing
- **@react-spring/web**: Animation library

#### Authentication
- **Azure MSAL (Microsoft Authentication Library)**
  - @azure/msal-browser: Browser-based authentication
  - @azure/msal-react: React integration for authentication flows

#### Build & Development
- **Node.js >=14.0.0**: JavaScript runtime
- **Nginx**: Web server for production deployment
- **Prettier**: Code formatting

### Testing & Quality
- **JUnit 5**: Unit testing framework
- **Mockito**: Mocking framework for Java tests
- **AssertJ**: Fluent assertion library
- **WireMock**: HTTP service mocking for integration tests

---

## Project Repository Structure

### Root Level

#### Configuration Files
- **`azure.yaml`**: Azure Developer CLI configuration defining services, languages, and hosting targets
- **`compose.yaml`**: Docker Compose orchestration for local multi-container development
- **`README.md`**: Project overview, architecture, and getting started guide
- **`CHANGELOG.md`**: Version history and release notes
- **`CONTRIBUTING.md`**: Contribution guidelines for developers
- **`SECURITY.md`**: Security policies and vulnerability reporting
- **`LICENSE`** / **`LICENSE.md`**: MIT license terms
- **`CODEOWNERS`**: Code ownership and review assignment

### `/app` - Application Source Code

Main application directory containing all microservices and frontend code.

#### `/app/copilot` - AI Agent Backend Service
Multi-module Maven project implementing the multi-agent banking assistant.

- **`/copilot-backend`**: Spring Boot application serving as the main copilot service
  - REST API endpoints for chat interactions
  - Supervisor agent orchestration
  - Integration with Azure OpenAI and Document Intelligence
  - `/manifests`: Kubernetes deployment configurations for AKS
    - Backend deployment templates
    - Service definitions
    - Ingress rules
    - Environment configuration maps

- **`/langchain4j-agents`**: Agent implementation module
  - Domain-specific agents (Account, Transactions, Payments)
  - Langchain4j integration and configuration
  - MCP tool bindings and execution logic
  - Agent conversation flows and tool invocation

- **`/copilot-common`**: Shared utilities and domain models
  - Common DTOs and domain entities
  - Utility classes and helpers
  - Shared configuration

- **Root files**:
  - `pom.xml`: Parent POM coordinating all copilot modules
  - `Dockerfile`: Container image build instructions
  - `applicationinsights.json`: Application Insights configuration
  - `mvnw` / `mvnw.cmd`: Maven wrapper scripts

#### `/app/business-api` - Microservices Backend

##### `/app/business-api/account` - Account Service
- **Purpose**: Manages user account information, balances, and payment methods
- **Exposed as**: REST API and MCP tools
- **Features**:
  - Retrieve account details by username
  - Get credit balance information
  - Manage registered payment methods
  - List beneficiaries
- **Technology**: Spring Boot microservice with Maven build
- **Files**: `pom.xml`, `Dockerfile`, `applicationinsights.json`, Maven wrapper
- **Source**: `/src/main` (application code), `/src/test` (tests)

##### `/app/business-api/payment` - Payment Service
- **Purpose**: Handles payment submission and processing
- **Exposed as**: REST API and MCP tools
- **Features**:
  - Submit payment requests
  - Notify transaction status
  - Payment validation and processing
- **Technology**: Spring Boot microservice with Maven build
- **Files**: `pom.xml`, `Dockerfile`, `applicationinsights.json`, Maven wrapper
- **Source**: `/src/main` (application code)

##### `/app/business-api/transactions-history` - Transactions Service
- **Purpose**: Provides transaction history and reporting
- **Exposed as**: REST API and MCP tools
- **Features**:
  - Search transaction history
  - Query transactions by recipient
  - Income and outcome payment tracking
  - Duplicate payment detection
- **Technology**: Spring Boot microservice with Maven build
- **Files**: `pom.xml`, `Dockerfile`, `applicationinsights.json`, Maven wrapper
- **Source**: `/src/main` (application code)

#### `/app/frontend` - Web UI Application
- **Purpose**: Chat-based user interface for the banking assistant
- **Technology**: React SPA with TypeScript, Vite build, Fluent UI components
- **Features**:
  - Conversational chat interface
  - Image upload support (invoices, receipts, bills)
  - Authentication with Azure AD
  - Real-time agent responses
- **Structure**:
  - `/src`: TypeScript/React source code
    - `/api`: API client and models
    - `/components`: Reusable UI components (AnalysisPanel, etc.)
    - `/pages`: Application pages/routes
    - `/assets`: Static assets
  - `/public`: Public static files
  - `/manifests`: Kubernetes deployment configurations
  - `/nginx`: Nginx web server configuration
  - `Dockerfile` / `Dockerfile-aks`: Container configurations
  - `package.json`: Node.js dependencies and scripts
  - `vite.config.ts`: Vite bundler configuration
  - `tsconfig.json`: TypeScript compiler options

#### `/app/start-compose.sh` / `/app/start-compose.ps1`
Helper scripts to start all services locally using Docker Compose.

---

### `/data` - Sample Data
Test data and sample files for development and demonstration.
- **Contents**: Sample invoices, receipts, and mock banking data
- **Purpose**: Enable testing payment OCR features and agent interactions

---

### `/docs` - Documentation
Project documentation and guides.
- **`faq.md`**: Frequently asked questions
- **`troubleshooting.md`**: Common issues and solutions
- **`kusto-queries.md`**: Azure Monitor/Log Analytics query examples
- **`/assets`**: Documentation images and diagrams
- **`/multi-agents`**: Multi-agent architecture documentation
  - `introduction.md`: Detailed agent architecture explanations

---

### `/infra` - Infrastructure as Code (Bicep)
Azure infrastructure definitions using Bicep templates.

#### Root Infrastructure Files
- **`main.bicep`**: Main infrastructure orchestration template
- **`main.parameters.json`**: Environment-specific parameter values

#### `/infra/app` - Application Service Definitions
Bicep modules for each application service:
- **`account.bicep`**: Account service Container App
- **`payment.bicep`**: Payment service Container App
- **`transaction.bicep`**: Transaction service Container App
- **`copilot.bicep`**: Copilot agent service Container App
- **`web.bicep`**: Frontend web application Container App

#### `/infra/shared` - Shared Infrastructure Components

##### `/infra/shared/ai`
- **`cognitiveservices.bicep`**: Azure OpenAI and Document Intelligence provisioning

##### `/infra/shared/host`
- **`container-apps-environment.bicep`**: Container Apps environment setup
- **`container-app.bicep`**: Base container app template
- **`container-app-upsert.bicep`**: Container app update/create logic
- **`container-apps.bicep`**: Multi-container app orchestration
- **`container-registry.bicep`**: Azure Container Registry setup

##### `/infra/shared/monitor`
- **`monitoring.bicep`**: Monitoring infrastructure orchestration
- **`loganalytics.bicep`**: Log Analytics workspace
- **`applicationinsights.bicep`**: Application Insights instance
- **`applicationinsights-dashboard.bicep`**: Custom monitoring dashboard
- **`backend-dashboard.bicep`**: Backend-specific monitoring dashboard

##### `/infra/shared/security`
- **`keyvault.bicep`**: Azure Key Vault provisioning
- **`keyvault-access.bicep`**: Key Vault access policies
- **`keyvault-secret.bicep`**: Secret management
- **`registry-access.bicep`**: Container registry access control
- **`role.bicep`**: RBAC role assignments

##### `/infra/shared/storage`
- **`storage-account.bicep`**: Azure Storage account provisioning

##### `/infra/shared/abbreviations.json`
Resource naming conventions and abbreviations for consistent Azure resource names.

---

## Architecture Overview

### Agent Architecture
The system implements a **vertical multi-agent supervisor pattern**:

1. **Supervisor Agent**: Routes user requests to specialized domain agents based on intent
2. **Account Agent**: Handles account balance, payment methods, and beneficiary queries
3. **Transactions Agent**: Manages transaction history searches and reporting
4. **Payments Agent**: Processes payment submissions with invoice OCR capabilities

### Technology Flow
```
User → React Frontend → Copilot Backend (Spring Boot)
                              ↓
                      Supervisor Agent (Langchain4j)
                              ↓
                    ┌─────────┴─────────┐
                    ↓         ↓         ↓
              Account     Transactions  Payments
               Agent        Agent        Agent
                    ↓         ↓         ↓
              MCP Tools (Spring AI MCP)
                    ↓         ↓         ↓
              Business APIs (Spring Boot Microservices)
                    ↓         ↓         ↓
              Account    Transaction   Payment
              Service      Service     Service
```

### Deployment Architecture
- **Local Development**: Docker Compose orchestrates all services
- **Cloud Deployment**: Azure Container Apps with:
  - Automatic scaling
  - Zero-downtime deployments
  - Managed identity for service authentication
  - Application Insights for observability
  - Azure OpenAI for LLM capabilities
  - Document Intelligence for OCR

---

## Key Design Patterns

### Model Context Protocol (MCP)
Business APIs are exposed as agent tools using Spring AI MCP, enabling:
- Automatic tool registration from OpenAPI specifications
- Type-safe parameter binding
- Standardized tool invocation protocol

### Agent Tool Invocation
Langchain4j manages:
- Automatic tool selection based on conversation context
- Parameter extraction from natural language
- Function calling with Azure OpenAI
- Response synthesis and error handling

### Microservices Architecture
- **Domain-driven design**: Each service owns its business domain
- **API-first**: REST APIs with OpenAPI specifications
- **Containerized deployment**: Docker images for consistent environments
- **Observability**: Distributed tracing with Application Insights

---

## Development Workflow

### Local Development
1. Start all services: `./app/start-compose.sh` (Linux/Mac) or `./app/start-compose.ps1` (Windows)
2. Access frontend: `http://localhost:8081`
3. Backend services run on assigned ports with live reload

### Cloud Deployment
1. Provision infrastructure: `azd provision`
2. Deploy services: `azd deploy`
3. Monitor: Azure Portal → Application Insights dashboards

### Testing
- Unit tests: Maven test phase in each service
- Integration tests: WireMock for API mocking
- E2E testing: Manual testing through frontend

---

## Security & Authentication

- **Azure AD Authentication**: Frontend uses MSAL for user authentication
- **Managed Identity**: Services authenticate to Azure resources without credentials
- **Key Vault Integration**: Secrets stored securely and injected at runtime
- **API Security**: Spring Security protects backend endpoints

---

## Monitoring & Observability

- **Application Insights**: Real-time performance monitoring, distributed tracing
- **Log Analytics**: Centralized logging with Kusto query language
- **Custom Dashboards**: Pre-built dashboards for backend and agent metrics
- **Telemetry**: Automatic instrumentation of HTTP calls, dependencies, and exceptions
