# Cloud Storage Application

## Deployment Options

### Docker Compose

#### Setup

1. Create `.env` file in the root:
   ```bash
   REDIS_PASSWORD=your_redis_password
   POSTGRES_PASSWORD=your_db_password
   POSTGRES_USER=your_db_user
   POSTGRES_DB=your_db_name
   MINIO_ROOT_USER=your_minio_user
   MINIO_ROOT_PASSWORD=your_minio_password
   ```
2. Start containers:

   ```bash
   docker compose pull
   docker compose up -d
   ```

### Kubernetes (api only)

#### Setup

1. Change secrets in `k8s/cloud-storage-secrets.yaml` if needed
2. Deploy components:
   ```bash
   kubectl apply -f k8s/cloud-storage-secrets.yaml
   kubectl apply -f k8s/cloud-storage-config.yaml
   kubectl apply -f k8s/cloud-storage-postgres.yaml
   kubectl apply -f k8s/cloud-storage-redis.yaml
   kubectl apply -f k8s/cloud-storage-minio.yaml
   kubectl apply -f k8s/cloud-storage-app.yaml
   ```
