package br.com.sirej.configuracao;

import java.util.Optional;
import java.util.UUID;

/**
 * API pública do regimento (docs/dev/06, "Acesso no código"). Único ponto pelo qual os módulos leem regra que
 * varia entre órgãos: nenhum módulo lê o YAML diretamente.
 *
 * <p>Todo ato que depende de regra grava {@link RegimentoVersao#id()} da versão vigente como {@code config_versao};
 * a reconstituição de ato antigo usa {@link #versao(UUID)}, nunca a vigente.
 */
public interface RegimentoVigente {

    /** Regimento da versão vigente agora. */
    default Regimento regimento() {
        return versao().regimento();
    }

    /** Versão vigente agora (a de vigência mais recente já iniciada). */
    RegimentoVersao versao();

    /** Uma versão qualquer, vigente ou histórica, pelo id gravado no ato ({@code config_versao}). */
    Optional<RegimentoVersao> versao(UUID id);
}
