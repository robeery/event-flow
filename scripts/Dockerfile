FROM python:3.11-slim

WORKDIR /app

RUN pip install --no-cache-dir requests

COPY init_db.py .

CMD ["python", "init_db.py"]
