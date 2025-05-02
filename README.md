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
   ```
2. Start containers:

   ```bash
   docker compose pull
   docker compose up -d
   ```


### Kubernetes

#### Setup

1. Change secrets in `k8s/cloud-storage-secrets.yaml` if needed
2. Deploy components:
   ```bash
   kubectl apply -f k8s/cloud-storage-secrets.yaml
   kubectl apply -f k8s/cloud-storage-config.yaml
   kubectl apply -f k8s/cloud-storage-postgres.yaml
   kubectl apply -f k8s/cloud-storage-redis.yaml
   kubectl apply -f k8s/cloud-storage-app.yaml
   ```
