package br.com.sirej.configuracao;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import br.com.sirej.compartilhado.Hash;
import br.com.sirej.compartilhado.Imutavel;

/**
 * Versão imutável do regimento (tabela {@code configuracao.regimento_versao}, docs/dev/05). Cada mudança de
 * configuração é uma nova versão; nada é atualizado nem apagado (invariante 5).
 *
 * @param id identificador gravado nos atos como {@code config_versao}
 * @param hash SHA-256 do conteúdo canônico (JSON com chaves ordenadas, sem espaços) do regimento resolvido
 * @param vigenteDesde instante a partir do qual a versão vale
 * @param aplicadoPor quem aplicou ({@code INSTALADOR} na primeira subida; proposta aprovada a partir do PT-29)
 * @param regimento o conteúdo tipado
 */
@Imutavel
public record RegimentoVersao(UUID id, Hash hash, Instant vigenteDesde, String aplicadoPor, Regimento regimento) {

    public RegimentoVersao {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(hash, "hash");
        Objects.requireNonNull(vigenteDesde, "vigenteDesde");
        Objects.requireNonNull(aplicadoPor, "aplicadoPor");
        Objects.requireNonNull(regimento, "regimento");
    }
}
