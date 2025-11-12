# Use official Python image as base
FROM python:3.11-bullseye

# Set work directory
WORKDIR /app

# Install pipx and use it to install uv
RUN pip install --no-cache-dir pipx \
	&& pipx ensurepath \
	&& pipx install uv
ENV PATH="/root/.local/bin:$PATH"

# Copy application code
COPY . .

# Create virtual environment and install dependencies
RUN uv venv  
#RUN source .venv/bin/activate 
RUN uv sync --active

# Expose port
EXPOSE 8080

# Start the app with Python using the virtual environment
CMD [".venv/bin/python", "main.py"]
