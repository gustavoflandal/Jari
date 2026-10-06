package br.com.sirej.configuracao;

import java.time.Instant;
import java.util.UUID;

import br.com.sirej.compartilhado.Hash;

/**
 * Evento de domínio (docs/dev/10): uma nova {@link RegimentoVersao} foi gravada. Publicado na mesma transação da
 * gravação, de forma síncrona; módulos com cache de configuração o invalidam e a auditoria o registra (D-48).
 * Não leva o conteúdo, só a identificação da versão.
 *
 * @param configVersao id da nova versão
 * @param hash hash do conteúdo canônico
 * @param vigenteDesde início da vigência
 * @param ator quem aplicou a versão
 * @param ocorridoEm instante da gravação
 */
public record RegimentoVersaoPublicada(UUID configVersao, Hash hash, Instant vigenteDesde, String ator,
        Instant ocorridoEm) {
}
