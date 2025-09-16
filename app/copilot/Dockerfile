
# Use official Python image as base
FROM python:3.11-bullseye



# Set work directory
WORKDIR /app


# Install Python dependencies
COPY requirements.txt ./
RUN pip install --upgrade pip \
	&& pip install -r requirements.txt

# Copy application code
COPY app ./app

# Expose port
EXPOSE 8080

# Start the app with Uvicorn
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8080"]
