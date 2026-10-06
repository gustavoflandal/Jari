#!/bin/sh
# Cria o banco do Keycloak na primeira inicialização do PostgreSQL (o do SIREJ vem de POSTGRES_DB).
set -eu

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres <<EOSQL
CREATE ROLE keycloak LOGIN PASSWORD '${KEYCLOAK_DB_PASSWORD}';
CREATE DATABASE keycloak OWNER keycloak;
EOSQL
