# Dropshipping Backend

Spring Boot backend for the dropshipping automation platform.

## Features
- Order status tracking workflow
- Daily supplier buy list system
- Payment-supplier reconciliation
- Complete audit trail system
- Supplier receipt archive
- Duplicate order prevention
- Inventory verification workflow
- High-value order review
- And more...

## Technology Stack
- Spring Boot 3.2+
- Java 17
- PostgreSQL
- AWS SDK (S3, SQS, SES)
- Redis for caching
- JWT for authentication
- Stripe integration

## Setup Instructions

1. **Prerequisites**
   - Java 17 or higher
   - Maven 3.6+
   - PostgreSQL 14+
   - Redis 6+

2. **Database Setup**
   ```sql
   CREATE DATABASE dropshipping_db;
   CREATE USER dropshipping_user WITH PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE dropshipping_db TO dropshipping_user;
   ```

3. **Configuration**
   Copy `application.yml` and update with your database credentials and AWS credentials.

4. **Build and Run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. **API Documentation**
   Once running, access Swagger UI at: `http://localhost:8080/api/swagger-ui.html`

## Key Endpoints
- `GET /api/orders` - List orders
- `GET /api/admin/supplier-buy-list` - Daily supplier buy list
- `POST /api/orders` - Create new order
- `GET /api/audit/order/{orderId}` - Order audit trail

## Environment Variables
- `DATABASE_URL` - PostgreSQL connection URL
- `DATABASE_USERNAME` - Database username
- `DATABASE_PASSWORD` - Database password
- `REDIS_HOST` - Redis host
- `REDIS_PORT` - Redis port
- `AWS_REGION` - AWS region
- `AWS_S3_BUCKET` - S3 bucket for receipts
- `STRIPE_API_KEY` - Stripe API key
- `STRIPE_WEBHOOK_SECRET` - Stripe webhook secret
