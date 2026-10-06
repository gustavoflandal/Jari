-- Papéis de banco dos testes do PT-04, criados pelo superusuário do contêiner (o "DBA").
-- sirej_dono: dono dos objetos; roda as migrações Flyway. Não é superusuário.
-- sirej_app_teste: login da aplicação, membro de sirej_aplicacao (só SELECT e INSERT na trilha).
CREATE ROLE sirej_dono LOGIN PASSWORD 'dono';
ALTER DATABASE test OWNER TO sirej_dono;
CREATE ROLE sirej_aplicacao NOLOGIN;
CREATE ROLE sirej_app_teste LOGIN PASSWORD 'app' IN ROLE sirej_aplicacao;
