package br.com.sirej.auditoria;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import br.com.sirej.auditoria.dominio.EnderecoIp;
import br.com.sirej.compartilhado.ValorInvalidoException;

/**
 * Ato a acrescentar na trilha.
 *
 * <p>O instante, o número de sequência e o encadeamento são da trilha, não do chamador. O detalhe é um
 * mapa plano de texto, ordenado pela chave, para que a forma canônica do registro seja única
 * (doc 08: {@code JSON_canonico}). Nada de dado pessoal em claro: CPF e CNPJ são recusados (D-43).
 *
 * @param ator autor identificado (RN23)
 * @param ip endereço IP de origem, ou {@code null} para rotina interna
 * @param acao código do ato, em maiúsculas (ex.: {@code DESIGNACAO_CONSULTADA})
 * @param alvo objeto do ato
 * @param detalhe pares adicionais; pode ser vazio
 */
public record NovoRegistroAuditoria(Ator ator, String ip, String acao, AlvoAuditoria alvo,
        SortedMap<String, String> detalhe) {

    private static final Pattern ACAO = Pattern.compile("[A-Z][A-Z0-9_]{2,99}");
    private static final Pattern CHAVE = Pattern.compile("[a-z][a-z0-9_]{0,62}");
    private static final int TAMANHO_MAXIMO_VALOR = 2000;

    public NovoRegistroAuditoria {
        if (ator == null) {
            throw new ValorInvalidoException("ato auditado exige autor identificado");
        }
        if (acao == null || !ACAO.matcher(acao).matches()) {
            throw new ValorInvalidoException("código da ação auditada inválido");
        }
        if (alvo == null) {
            throw new ValorInvalidoException("ato auditado exige alvo");
        }
        ip = ip == null ? null : EnderecoIp.normalizar(ip);
        detalhe = Collections.unmodifiableSortedMap(validarDetalhe(detalhe));
    }

    /** Atalho sem detalhe. */
    public NovoRegistroAuditoria(Ator ator, String ip, String acao, AlvoAuditoria alvo) {
        this(ator, ip, acao, alvo, new TreeMap<>());
    }

    /** Atalho com detalhe em qualquer {@link Map}; a ordem é a das chaves. */
    public static NovoRegistroAuditoria de(Ator ator, String ip, String acao, AlvoAuditoria alvo,
            Map<String, String> detalhe) {
        return new NovoRegistroAuditoria(ator, ip, acao, alvo,
                new TreeMap<>(Objects.requireNonNull(detalhe, "detalhe")));
    }

    private static SortedMap<String, String> validarDetalhe(SortedMap<String, String> detalhe) {
        TreeMap<String, String> copia = new TreeMap<>();
        if (detalhe == null) {
            return copia;
        }
        detalhe.forEach((chave, valor) -> {
            if (chave == null || !CHAVE.matcher(chave).matches()) {
                throw new ValorInvalidoException("chave de detalhe da auditoria inválida");
            }
            if (valor == null || valor.length() > TAMANHO_MAXIMO_VALOR || valor.indexOf('\0') >= 0) {
                throw new ValorInvalidoException("valor de detalhe da auditoria inválido");
            }
            ValidacaoDeDadoPessoal.recusarSeDocumentoPessoal(valor);
            copia.put(chave, valor);
        });
        return copia;
    }
}
