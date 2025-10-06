# Banking Assistant Business API - Python FastMCP Services

A collection of Python-based FastMCP servers that provide simulated banking tools used by the Banking Assistant Copilot application. These microservices expose banking operations through the Model Context Protocol (MCP) for seamless integration with AI agents.

## 🏗️ Architecture

This business API layer implements **specialized MCP servers** for different banking domains:
- **Account Service**: Manages account details, payment methods, and beneficiaries
- **Payment Service**: Processes payment requests and transactions
- **Transaction Service**: Handles transaction history and search operations

Each service runs as an independent FastMCP server exposing banking tools through HTTP endpoints that the copilot agents can consume.

## 🚀 Quick Start

### Prerequisites

- Python 3.11 or higher
- [uv](https://docs.astral.sh/uv/) package manager
- Git


> [!WARNING]
> To avoid Python virtual environment conflicts when running all apps locally, it's highly recommended to open **business-api** folder in a separate VS Code window and set the python interpreter to the python/transaction/venv/Scripts/python.exe python installation. This ensures the copilot and business-api run in isolated environments.


## 🔧 Service Setup

Each service follows the same setup pattern. Navigate to the specific service directory and follow below steps. Examples are shown for account service, make sure to adjust paths and environment variables for payment and transaction services:

### 1. Account Service Setup

```powershell
cd app/business-api/python/account
```

#### Install dependencies using uv and run

```powershell
# Install uv if you don't have it
pip install uv

# Create a virtual environment
uv venv

# Activate the virtual environment
.\.venv\Scripts\Activate.ps1

# Install all dependencies
uv sync

# Run the Account MCP Server (FastAPI + MCP)

$env:PROFILE="dev"
python main.py
```

The Account service will be available at: **http://localhost:8070**

---

### 2. Transaction Service Setup

```powershell
cd app/business-api/python/transaction
```

#### Install dependencies and run

```powershell
# Create virtual environment and install dependencies
uv venv
.\.venv\Scripts\Activate.ps1
uv sync

# Run the Transaction MCP Server (FastAPI + MCP)
$env:PROFILE="dev"
python main.py
```

The Transaction service will be available at: **http://localhost:8071**

---
### 2. Payment Service Setup

```powershell
cd app/business-api/python/payment
```

#### Install dependencies and run

```powershell
# Create virtual environment and install dependencies
uv venv
.\.venv\Scripts\Activate.ps1
uv sync

# Run the Payment MCP Server
$env:PROFILE="dev"
$env:TRANSACTIONS_API_URL="http://localhost:8071"
python main.py
```

The Payment service will be available at: **http://localhost:8072**


## 🛠️ Available Banking Tools

### Account Service (Port 8070)
Exposes the following MCP tools:

- **`getAccountsByUserName`** - Get all accounts for a specific user
- **`getAccountDetails`** - Get account details and available payment methods
- **`getPaymentMethodDetails`** - Get payment method details with available balance
- **`getRegisteredBeneficiary`** - Get registered beneficiaries for an account

### Payment Service (Port 8072)
Exposes the following MCP tools:

- **`processPayment`** - Submit and process payment requests with full transaction details

### Transaction Service (Port 8071)
Exposes the following MCP tools:

- **`getTransactionsByRecipientName`** - Search transactions by recipient name
- **`getLastTransactions`** - Get recent transaction history for an account

Additionally provides REST API endpoints at `/api/transactions` for direct HTTP access.

### Port Configuration

Services use different ports based on the `PROFILE` environment variable:

| Service     | Development Port | Production Port |
|-------------|------------------|-----------------|
| Account     | 8070            | 8080            |
| Transaction | 8071            | 8080            |
| Payment     | 8072            | 8080            |



## 📁 Service Structure

Each service follows a consistent structure:

```
service-name/
├── main.py                 # FastMCP server entry point
├── mcp_tools.py           # MCP tool definitions (@mcp.tool decorators)
├── services.py            # Business logic and data access
├── models.py              # Pydantic data models
├── logging_config.py      # Logging configuration
├── pyproject.toml         # Project dependencies
├── uv.lock               # Lock file for reproducible builds
├── Dockerfile            # Container configuration
└── README.md             # Service-specific documentation
```


## 🔌 Integration with Copilot

The Banking Assistant Copilot connects to these services via MCP URLs configured in its environment:

```env
# MCP Server URLs (from copilot/.env.dev)
ACCOUNT_MCP_URL=http://localhost:8070
TRANSACTION_MCP_URL=http://localhost:8071
PAYMENT_MCP_URL=http://localhost:8072
```

The copilot's specialized agents use these tools to:
- **Account Agent**: Query account details and payment methods
- **Transaction Agent**: Search and retrieve transaction history
- **Payment Agent**: Process payment requests and validations


## 🐛 Development & Debugging

You can run each service and debug them in VS code using the launch configurations provided in  `business-api/.vscode/launch.json`. This allows you to set breakpoints and step through the code.
The available commands are:

- **Account MCP: DEV (Python Main)**
- **Transaction MCP: DEV (Python Main)**
- **Payment MCP: DEV (Python Main)**



## 🚢 Containerization

Each service includes a `Dockerfile` for containerized deployment. Build and run any service:

```powershell
# Build container
docker build -t banking-account-service .

# Run container and expose a service on a specific port like 8070
docker run -p 8070:8080 banking-account-service
```


## 📋 Next Steps

1. **Start all three services** in separate VS Code windows
2. **Configure the copilot** with the MCP URLs pointing to your local services
3. **Test the integration** by running the copilot and asking banking-related questions
4. **Monitor logs** to see how agents interact with the banking tools

The services provide simulated banking data for development and testing purposes. In a production environment, these would connect to real banking systems and databases.