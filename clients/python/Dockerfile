FROM python:3-alpine

RUN apk add geos

COPY . /app

WORKDIR /app

RUN pip install --no-cache-dir -r requirements.txt .

ENTRYPOINT ["python","examples/import_datasets.py"]
