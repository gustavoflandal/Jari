-- Banco do Keycloak, criado na primeira inicialização do PostgreSQL (o banco do SIREJ vem de POSTGRES_DB).
-- Credencial de desenvolvimento; o mesmo valor está em KC_DB_PASSWORD no docker-compose.yml.
CREATE ROLE keycloak LOGIN PASSWORD 'keycloak';
CREATE DATABASE keycloak OWNER keycloak;
