-- PT-04: trilha de auditoria append-only, encadeada por hash e particionada por mês (docs/dev/05 e 08).
--
-- Proteções no banco (além da aplicação):
--   * o papel da aplicação (sirej_aplicacao) só tem SELECT e INSERT; nunca UPDATE, DELETE ou TRUNCATE;
--   * um trigger recusa UPDATE, DELETE e TRUNCATE para qualquer usuário, inclusive o dono das tabelas;
--   * o que um superusuário ainda consegue fazer (desligar triggers) é detectado pela verificação da
--     cadeia e pelas âncoras diárias exportadas para fora da produção.

CREATE SCHEMA IF NOT EXISTS auditoria;

-- Papel de banco da aplicação. O instalador cria o usuário de login e o torna membro deste papel;
-- se o papel ainda não existir, é criado sem login.
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'sirej_aplicacao') THEN
        CREATE ROLE sirej_aplicacao NOLOGIN;
    END IF;
END
$$;

REVOKE ALL ON SCHEMA auditoria FROM PUBLIC;
GRANT USAGE ON SCHEMA auditoria TO sirej_aplicacao;

-- Recusa qualquer alteração ou remoção (invariantes 5 e 6), inclusive do dono.
CREATE FUNCTION auditoria.recusar_alteracao() RETURNS trigger
    LANGUAGE plpgsql AS
$$
BEGIN
    RAISE EXCEPTION 'auditoria.%: % recusado, tabela append-only (invariantes 5 e 6)', TG_TABLE_NAME, TG_OP
        USING ERRCODE = 'insufficient_privilege';
END
$$;

CREATE SEQUENCE auditoria.registro_auditoria_seq AS bigint;

CREATE TABLE auditoria.registro_auditoria (
    seq           bigint      NOT NULL DEFAULT nextval('auditoria.registro_auditoria_seq'),
    em            timestamptz NOT NULL,
    ator_id       text        NOT NULL CHECK (ator_id ~ '^[A-Za-z0-9][A-Za-z0-9._:-]{0,99}$'),
    ator_papel    text        NOT NULL CHECK (ator_papel ~ '^[A-Z][A-Z0-9_]{0,59}$'),
    ip            inet,
    acao          text        NOT NULL CHECK (acao ~ '^[A-Z][A-Z0-9_]{2,99}$'),
    alvo_tipo     text        NOT NULL CHECK (alvo_tipo ~ '^[a-z][a-z0-9_]{0,59}$'),
    alvo_id       text        NOT NULL,
    detalhe       jsonb       NOT NULL DEFAULT '{}'::jsonb CHECK (jsonb_typeof(detalhe) = 'object'),
    hash_anterior text        NOT NULL CHECK (hash_anterior ~ '^[0-9a-f]{64}$'),
    hash          text        NOT NULL CHECK (hash ~ '^[0-9a-f]{64}$'),
    PRIMARY KEY (seq, em)
) PARTITION BY RANGE (em);

ALTER SEQUENCE auditoria.registro_auditoria_seq OWNED BY auditoria.registro_auditoria.seq;

CREATE INDEX registro_auditoria_alvo ON auditoria.registro_auditoria (alvo_tipo, alvo_id);
CREATE INDEX registro_auditoria_em ON auditoria.registro_auditoria (em);

-- Linhas: o trigger de linha do pai vale para todas as partições.
CREATE TRIGGER registro_auditoria_sem_alteracao
    BEFORE UPDATE OR DELETE ON auditoria.registro_auditoria
    FOR EACH ROW EXECUTE FUNCTION auditoria.recusar_alteracao();
-- TRUNCATE: no pai e, abaixo, em cada partição (TRUNCATE direto na partição não dispara o do pai).
CREATE TRIGGER registro_auditoria_sem_truncate
    BEFORE TRUNCATE ON auditoria.registro_auditoria
    FOR EACH STATEMENT EXECUTE FUNCTION auditoria.recusar_alteracao();

REVOKE ALL ON auditoria.registro_auditoria FROM PUBLIC;
GRANT SELECT, INSERT ON auditoria.registro_auditoria TO sirej_aplicacao;
REVOKE ALL ON SEQUENCE auditoria.registro_auditoria_seq FROM PUBLIC;
GRANT USAGE ON SEQUENCE auditoria.registro_auditoria_seq TO sirej_aplicacao;

-- Cria, se faltar, a partição mensal (limites em UTC) que contém o dia informado.
-- SECURITY DEFINER: a aplicação pode garantir partições futuras sem ter CREATE no schema.
CREATE FUNCTION auditoria.garantir_particao(dia date) RETURNS void
    LANGUAGE plpgsql
    SECURITY DEFINER
    SET search_path = pg_catalog, pg_temp AS
$$
DECLARE
    inicio date := date_trunc('month', dia)::date;
    fim    date := (date_trunc('month', dia) + interval '1 month')::date;
    nome   text := 'registro_auditoria_' || to_char(inicio, 'YYYYMM');
BEGIN
    PERFORM pg_advisory_xact_lock(hashtext('auditoria.garantir_particao'));
    IF to_regclass('auditoria.' || nome) IS NOT NULL THEN
        RETURN;
    END IF;
    EXECUTE format('CREATE TABLE auditoria.%I PARTITION OF auditoria.registro_auditoria '
                       || 'FOR VALUES FROM (%L) TO (%L)',
                   nome, inicio::text || ' 00:00:00+00', fim::text || ' 00:00:00+00');
    EXECUTE format('CREATE TRIGGER %I BEFORE TRUNCATE ON auditoria.%I '
                       || 'FOR EACH STATEMENT EXECUTE FUNCTION auditoria.recusar_alteracao()',
                   nome || '_sem_truncate', nome);
    EXECUTE format('REVOKE ALL ON auditoria.%I FROM PUBLIC', nome);
END
$$;

REVOKE ALL ON FUNCTION auditoria.garantir_particao(date) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION auditoria.garantir_particao(date) TO sirej_aplicacao;

-- Partições iniciais: do mês anterior até 12 meses à frente. A rotina de manutenção garante as seguintes.
SELECT auditoria.garantir_particao(m::date)
FROM generate_series(date_trunc('month', now()) - interval '1 month',
                     date_trunc('month', now()) + interval '12 months',
                     interval '1 month') AS m;

CREATE TABLE auditoria.ancora_diaria (
    dia             date        PRIMARY KEY,
    seq_final       bigint      NOT NULL CHECK (seq_final >= 0),
    hash_final      text        NOT NULL CHECK (hash_final ~ '^[0-9a-f]{64}$'),
    carimbo_tempo   text        NOT NULL,
    carimbo_emissor text        NOT NULL,
    carimbo_em      timestamptz NOT NULL,
    exportado_em    timestamptz NOT NULL,
    destino         text        NOT NULL
);

CREATE TRIGGER ancora_diaria_sem_alteracao
    BEFORE UPDATE OR DELETE ON auditoria.ancora_diaria
    FOR EACH ROW EXECUTE FUNCTION auditoria.recusar_alteracao();
CREATE TRIGGER ancora_diaria_sem_truncate
    BEFORE TRUNCATE ON auditoria.ancora_diaria
    FOR EACH STATEMENT EXECUTE FUNCTION auditoria.recusar_alteracao();

REVOKE ALL ON auditoria.ancora_diaria FROM PUBLIC;
GRANT SELECT, INSERT ON auditoria.ancora_diaria TO sirej_aplicacao;
