#!/bin/bash
# Prepara o contêiner das sessões do Claude Code na nuvem para compilar e testar o SIREJ.
# Idempotente. Só roda em sessão remota (no computador de quem desenvolve, nada muda).
set -euo pipefail

if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

JDK_DIR=/opt/jdk-25
JDK_URL="https://github.com/adoptium/temurin25-binaries/releases/download/jdk-25%2B36/OpenJDK25U-jdk_x64_linux_hotspot_25_36.tar.gz"

# 1. JDK 25 (a imagem traz só o 21; adoptium.net é bloqueado pelo proxy, o GitHub não).
if [ ! -x "$JDK_DIR/bin/java" ]; then
  tmp=$(mktemp -d)
  curl -fsSL -o "$tmp/jdk.tgz" "$JDK_URL"
  mkdir -p "$JDK_DIR"
  tar -xzf "$tmp/jdk.tgz" -C "$JDK_DIR" --strip-components=1
  rm -rf "$tmp"
fi
if [ -n "${CLAUDE_ENV_FILE:-}" ]; then
  echo "export JAVA_HOME=$JDK_DIR" >> "$CLAUDE_ENV_FILE"
  echo "export PATH=$JDK_DIR/bin:\$PATH" >> "$CLAUDE_ENV_FILE"
fi

# 2. Docker para os testes com Testcontainers.
if command -v dockerd >/dev/null 2>&1 && ! docker info >/dev/null 2>&1; then
  (dockerd >/tmp/dockerd.log 2>&1 &)
  for _ in $(seq 1 30); do docker info >/dev/null 2>&1 && break; sleep 1; done
fi

# 3. Dependências do backend e do frontend (ficam no cache do contêiner).
cd "$CLAUDE_PROJECT_DIR"
if [ -x ./mvnw ]; then
  JAVA_HOME=$JDK_DIR PATH=$JDK_DIR/bin:$PATH ./mvnw -B -q -ntp -DskipTests dependency:go-offline >/dev/null || true
fi
if [ -f frontend/package.json ]; then
  (cd frontend && npm install --no-audit --no-fund --silent) || true
fi
