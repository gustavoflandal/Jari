package br.com.sirej.configuracao;

/**
 * Verificação de que a instalação tem cofre de chaves (HSM/KMS) configurado e alcançável, exigida pela regra de
 * consistência 7 do doc 06 quando {@code designacao.modo = SIGILOSO}.
 *
 * <p>Interface de extensão: a implementação real vem de quem usa o cofre ({@code distribuicao}, sobre a
 * {@code CofreChavesPort} do doc 09, PT-16). Sem nenhuma implementação registrada, o cofre conta como ausente e um
 * regimento sigiloso impede a subida (falha fechada; D-49). Em testes, use {@code VerificadorCofreChavesSimulado}.
 */
@FunctionalInterface
public interface VerificadorCofreChaves {

    /** {@code true} se o cofre está configurado e respondeu à verificação de conectividade. */
    boolean disponivel();
}
