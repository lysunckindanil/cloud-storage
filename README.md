# Cloud Storage Application

## Deployment Options

### Docker Compose

#### Setup

1. Create `.env` file:
   ```bash
   echo "POSTGRES_DB=your_db_name" >> .env
   echo "POSTGRES_USER=your_db_user" >> .env
   echo "POSTGRES_PASSWORD=your_db_password" >> .env
   echo "REDIS_PASSWORD=your_redis_password" >> .env
   ```
2. Start containers:

   ```bash
   docker compose pull
   docker compose up -d
   ```

### Kubernetes

#### Setup

1. Deploy components:
   ```bash
   kubectl apply -f k8s/cloud-storage-secrets.yaml
   kubectl apply -f k8s/cloud-storage-config.yaml
   kubectl apply -f k8s/cloud-storage-postgres.yaml
   kubectl apply -f k8s/cloud-storage-redis.yaml
   kubectl apply -f k8s/cloud-storage-app.yaml
   ```
