FROM python:3.12-slim

WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements first for better caching
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy source code (includes pre-generated protobuf files)
COPY src/ ./src/

# Set working directory to src for imports
WORKDIR /app/src

# Expose gRPC port
EXPOSE 50051

# Run the server
CMD ["python", "server.py"]
