package br.com.sirej.compartilhado;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca o tipo que representa uma tabela <b>[imutável]</b> do doc 05 (documento, movimentação, voto,
 * registro de auditoria, versão do regimento etc.). Invariante 5: nada é apagado; correção é novo
 * registro.
 *
 * <p>Consequências verificadas no build (docs/dev/12, regra 5): nenhum repositório de tipo marcado
 * expõe método {@code save*}, {@code delete*}, {@code remove*}, {@code update*} ou {@code merge*}.
 * A inclusão usa método de inserção explícito (por exemplo {@code inserir}), nunca {@code save}, que
 * no JPA também atualiza. No banco, {@code UPDATE} e {@code DELETE} falham pelo usuário da aplicação
 * (doc 12, "Testes de imutabilidade").
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Imutavel {
}
