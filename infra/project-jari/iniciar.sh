#!/usr/bin/env bash
# Constrói a imagem e (re)cria o contêiner project-jari. Os dados ficam em volumes e sobrevivem à recriação.
# Uso: infra/project-jari/iniciar.sh
set -euo pipefail

cd "$(dirname "$0")"

docker build -t project-jari .

if docker inspect project-jari >/dev/null 2>&1; then
    docker rm -f project-jari >/dev/null
fi

docker run -d \
    --name project-jari \
    --restart unless-stopped \
    -p 5432:5432 \
    -p 9000:9000 \
    -p 9001:9001 \
    -p 8080:8080 \
    -p 6379:6379 \
    -v project-jari-pgdata:/var/lib/postgresql/data \
    -v project-jari-s3:/data \
    -v project-jari-redis:/var/lib/redis \
    project-jari

echo "project-jari criado. Acompanhe com: docker logs -f project-jari"
echo "Saúde: docker inspect --format '{{.State.Health.Status}}' project-jari"
