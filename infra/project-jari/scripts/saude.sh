#!/bin/sh
# Saudável só quando os quatro serviços respondem e o bucket de documentos existe.
set -eu

export AWS_ACCESS_KEY_ID="$RUSTFS_ACCESS_KEY"
export AWS_SECRET_ACCESS_KEY="$RUSTFS_SECRET_KEY"
export AWS_DEFAULT_REGION=us-east-1

pg_isready -h localhost -p 5432 -U "$POSTGRES_USER" >/dev/null
aws --endpoint-url http://localhost:9000 s3api head-bucket --bucket "$S3_BUCKET" >/dev/null 2>&1
REDISCLI_AUTH="$REDIS_PASSWORD" redis-cli ping | grep -q PONG
curl -sf http://localhost:8080/realms/master >/dev/null
