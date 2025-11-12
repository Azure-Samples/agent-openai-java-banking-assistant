# Use official Python image as base
FROM python:3.11-bullseye

# Set work directory
WORKDIR /app


# Install pipx and use it to install uv
RUN pip install --no-cache-dir pipx \
	&& pipx ensurepath \
	&& pipx install uv
ENV PATH="/root/.local/bin:$PATH"


# Install pinned dependencies using uv pip
COPY pyproject.toml ./
COPY uv.lock ./
RUN uv pip install --system -r pyproject.toml --prerelease=allow

# Copy application code
COPY app ./app

# Expose port
EXPOSE 8080


# Start the app with Uvicorn
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8080"]
