package br.com.sirej.configuracao;

/**
 * Modo de designação (docs/dev/02; seção {@code designacao.modo} do regimento, RN20).
 *
 * <p>{@code SIGILOSO}: junta, posição, relator, revisor e 3º membro ficam selados até a abertura da sessão
 * (invariante 1) e exigem cofre de chaves na instalação (regra de consistência 7). {@code ABERTO}: a designação é
 * pública desde o lote.
 */
public enum ModoDesignacao {
    SIGILOSO,
    ABERTO
}
