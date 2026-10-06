/**
 * Módulo auditoria (docs/dev/03 e docs/dev/08): trilha append-only encadeada por hash, verificação da
 * cadeia e ancoragem diária com carimbo do tempo.
 *
 * <p>API pública (este pacote): {@link br.com.sirej.auditoria.TrilhaAuditoria} para os outros módulos
 * gravarem, sempre dentro da transação do próprio ato, e as portas
 * {@link br.com.sirej.auditoria.CarimboTempoPort} e {@link br.com.sirej.auditoria.ArmazenamentoAncoraPort},
 * implementadas fora daqui (doc 09). Os subpacotes {@code dominio}, {@code aplicacao} e
 * {@code infraestrutura} são internos.
 *
 * <p>O módulo não depende de nenhum módulo de negócio (doc 03, regra 3) e não lê a designação
 * (invariante 1): quem consulta a designação é o {@code distribuicao}, que grava aqui o registro da consulta.
 */
@ApplicationModule(displayName = "Auditoria")
package br.com.sirej.auditoria;

import org.springframework.modulith.ApplicationModule;
