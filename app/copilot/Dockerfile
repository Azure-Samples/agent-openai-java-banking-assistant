# Accept GitHub token as build argument
ARG GITHUB_TOKEN

# Use official Python image as base
FROM python:3.11-bullseye

# Set work directory
WORKDIR /app


# Install pipx and use it to install uv
RUN pip install --no-cache-dir pipx \
	&& pipx ensurepath \
	&& pipx install uv
ENV PATH="/root/.local/bin:$PATH"

# Configure git to use the token for GitHub
RUN git config --global url."https://${GITHUB_TOKEN}:x-oauth-basic@github.com/".insteadOf "https://github.com/"

# Install Python dependencies
COPY pyproject.toml ./
COPY uv.lock ./
RUN uv venv \
    && uv sync

# Copy application code
COPY app ./app

# Expose port
EXPOSE 8080

# Start the app with Uvicorn
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8080"]
# Start the app with Uvicorn
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8080"]
