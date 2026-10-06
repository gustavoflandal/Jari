package br.com.sirej.configuracao;

/**
 * Adaptador simulado da verificação do cofre de chaves (docs/dev/09: toda porta tem um {@code *Simulado}). Só para
 * testes e demonstração; a implementação real vem com a {@code CofreChavesPort} (PT-16).
 */
public final class VerificadorCofreChavesSimulado implements VerificadorCofreChaves {

    private final boolean disponivel;

    public VerificadorCofreChavesSimulado(boolean disponivel) {
        this.disponivel = disponivel;
    }

    /** Cofre configurado e alcançável. */
    public static VerificadorCofreChavesSimulado comCofre() {
        return new VerificadorCofreChavesSimulado(true);
    }

    /** Cofre ausente ou inalcançável. */
    public static VerificadorCofreChavesSimulado semCofre() {
        return new VerificadorCofreChavesSimulado(false);
    }

    @Override
    public boolean disponivel() {
        return disponivel;
    }
}
