-- PT-03: versões imutáveis do regimento (docs/dev/05, schema configuracao; docs/dev/06, "Ciclo de vida").
-- Tabela [imutável]: nenhuma linha é alterada nem apagada (invariante 5). Mudança de configuração = nova linha.

CREATE SCHEMA IF NOT EXISTS configuracao;

CREATE TABLE configuracao.regimento_versao (
    id            uuid        PRIMARY KEY,
    conteudo      jsonb       NOT NULL,
    hash          text        NOT NULL CHECK (hash ~ '^[0-9a-f]{64}$'),
    vigente_desde timestamptz NOT NULL,
    aplicado_por  text        NOT NULL,
    proposta_id   uuid,
    criado_em     timestamptz NOT NULL,
    criado_por    text        NOT NULL
);

COMMENT ON TABLE configuracao.regimento_versao IS
    'Versões imutáveis do regimento (docs/dev/05). hash = SHA-256 do JSON canônico de conteudo. Sem UPDATE/DELETE.';

CREATE INDEX regimento_versao_vigencia_idx ON configuracao.regimento_versao (vigente_desde, criado_em);

-- Imutabilidade garantida no banco, também para o dono da tabela (docs/dev/05 e docs/dev/12).
CREATE FUNCTION configuracao.recusar_alteracao_imutavel() RETURNS trigger
    LANGUAGE plpgsql AS
$$
BEGIN
    RAISE EXCEPTION 'tabela imutável %.%: % recusado (invariante 5; correção é novo registro)',
        TG_TABLE_SCHEMA, TG_TABLE_NAME, TG_OP
        USING ERRCODE = 'restrict_violation';
END;
$$;

CREATE TRIGGER regimento_versao_sem_update_delete
    BEFORE UPDATE OR DELETE ON configuracao.regimento_versao
    FOR EACH ROW EXECUTE FUNCTION configuracao.recusar_alteracao_imutavel();

CREATE TRIGGER regimento_versao_sem_truncate
    BEFORE TRUNCATE ON configuracao.regimento_versao
    FOR EACH STATEMENT EXECUTE FUNCTION configuracao.recusar_alteracao_imutavel();

-- Papel de banco da aplicação (mesmo padrão do PT-04): só lê e inclui.
DO
$$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'sirej_aplicacao') THEN
        CREATE ROLE sirej_aplicacao NOLOGIN;
    END IF;
END;
$$;

GRANT USAGE ON SCHEMA configuracao TO sirej_aplicacao;
GRANT SELECT, INSERT ON configuracao.regimento_versao TO sirej_aplicacao;
