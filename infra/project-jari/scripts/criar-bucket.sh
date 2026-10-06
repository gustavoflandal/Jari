#!/bin/sh
# Cria o bucket de documentos com Object Lock (ADR-0005). O Object Lock só pode ser ligado na criação do bucket.
set -eu

export AWS_ACCESS_KEY_ID="$RUSTFS_ACCESS_KEY"
export AWS_SECRET_ACCESS_KEY="$RUSTFS_SECRET_KEY"
export AWS_DEFAULT_REGION=us-east-1
ENDPOINT=http://localhost:9000

until curl -s -o /dev/null "$ENDPOINT/health" || curl -s -o /dev/null "$ENDPOINT"; do
    echo "criar-bucket: aguardando o armazenamento S3"
    sleep 2
done

if aws --endpoint-url "$ENDPOINT" s3api head-bucket --bucket "$S3_BUCKET" >/dev/null 2>&1; then
    echo "criar-bucket: bucket $S3_BUCKET já existe"
else
    aws --endpoint-url "$ENDPOINT" s3api create-bucket --bucket "$S3_BUCKET" --object-lock-enabled-for-bucket >/dev/null
    echo "criar-bucket: bucket $S3_BUCKET criado (Object Lock ligado)"
fi
