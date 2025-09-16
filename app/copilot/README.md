# Minimal FastAPI async microservice scaffold

This folder contains a minimal, self-contained FastAPI async microservice example with an in-memory repository and tests.

Quick start (PowerShell):

```powershell
python -m venv .venv
. .venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn app.main:app --reload
```

Run tests:

```powershell
. .venv\Scripts\Activate.ps1
pytest -q
```
