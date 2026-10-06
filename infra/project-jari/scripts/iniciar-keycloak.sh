#!/bin/sh
# Espera o PostgreSQL aceitar conexões TCP e então sobe o Keycloak em modo de desenvolvimento.
set -eu

until pg_isready -h localhost -p 5432 -U "$POSTGRES_USER" >/dev/null 2>&1; do
    echo "keycloak: aguardando o PostgreSQL"
    sleep 2
done

export KC_DB=postgres
export KC_DB_URL="jdbc:postgresql://localhost:5432/keycloak"
export KC_DB_USERNAME=keycloak
export KC_DB_PASSWORD="$KEYCLOAK_DB_PASSWORD"
export KC_BOOTSTRAP_ADMIN_USERNAME="$KEYCLOAK_ADMIN"
export KC_BOOTSTRAP_ADMIN_PASSWORD="$KEYCLOAK_ADMIN_PASSWORD"
export KC_HTTP_ENABLED=true
export KC_HOSTNAME_STRICT=false

exec /opt/keycloak/bin/kc.sh start-dev
