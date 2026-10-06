# Infraestrutura de desenvolvimento do SIREJ num único contêiner: PostgreSQL 17, RustFS (S3 com Object Lock),
# Keycloak (OIDC) e Redis, sob o supervisord. Só para desenvolvimento: credenciais fixas e Keycloak em modo dev.
# Em produção cada componente é um serviço próprio (docs/dev/19-operacao-e-infraestrutura.md, seção 2).
#
# O MinIO de código aberto foi arquivado e não é mais distribuído; o RustFS é o substituto S3 compatível. A imagem
# fica fixada pelo digest testado (Object Lock e retenção COMPLIANCE verificados com o aws cli).

ARG RUSTFS_IMAGE=rustfs/rustfs@sha256:1803faef57627e2d9c2e7d89d655d712ddded5389040054987163043fecb6a3c
ARG KEYCLOAK_IMAGE=quay.io/keycloak/keycloak:26.4.0
ARG JAVA_IMAGE=eclipse-temurin:21-jre

FROM ${RUSTFS_IMAGE} AS rustfs
FROM ${KEYCLOAK_IMAGE} AS keycloak
FROM ${JAVA_IMAGE} AS java

FROM postgres:17-bookworm

RUN apt-get update \
    && apt-get install -y --no-install-recommends supervisor redis-server curl ca-certificates awscli \
    && rm -rf /var/lib/apt/lists/*

COPY --from=rustfs /usr/bin/rustfs /usr/local/bin/rustfs
COPY --from=java /opt/java/openjdk /opt/java/openjdk
COPY --from=keycloak /opt/keycloak /opt/keycloak

ENV JAVA_HOME=/opt/java/openjdk \
    PATH=/opt/java/openjdk/bin:$PATH

# Credenciais e nomes de desenvolvimento; sobrescreva com -e na criação do contêiner.
ENV POSTGRES_USER=sirej \
    POSTGRES_PASSWORD=sirej \
    POSTGRES_DB=sirej \
    KEYCLOAK_DB_PASSWORD=keycloak \
    RUSTFS_ACCESS_KEY=sirej \
    RUSTFS_SECRET_KEY=sirej-s3-dev \
    S3_BUCKET=sirej-documentos \
    REDIS_PASSWORD=sirej \
    KEYCLOAK_ADMIN=admin \
    KEYCLOAK_ADMIN_PASSWORD=admin

COPY supervisord.conf /etc/supervisor/project-jari.conf
COPY initdb/ /docker-entrypoint-initdb.d/
COPY scripts/ /usr/local/bin/

RUN sed -i 's/\r$//' /etc/supervisor/project-jari.conf /docker-entrypoint-initdb.d/* /usr/local/bin/*.sh \
    && chmod +x /docker-entrypoint-initdb.d/*.sh /usr/local/bin/*.sh \
    && mkdir -p /data /var/lib/redis \
    && chown redis:redis /var/lib/redis

VOLUME ["/var/lib/postgresql/data", "/data", "/var/lib/redis"]

# PostgreSQL, S3 (API e console), Keycloak, Redis
EXPOSE 5432 9000 9001 8080 6379

HEALTHCHECK --interval=15s --timeout=10s --start-period=180s --retries=5 CMD ["/usr/local/bin/saude.sh"]

ENTRYPOINT []
CMD ["/usr/bin/supervisord", "-n", "-c", "/etc/supervisor/project-jari.conf"]
