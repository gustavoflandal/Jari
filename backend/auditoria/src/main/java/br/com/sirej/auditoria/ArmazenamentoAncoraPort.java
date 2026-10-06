package br.com.sirej.auditoria;

/**
 * Porta de exportação da âncora diária para armazenamento WORM fora do ambiente de produção
 * (docs/dev/08 e docs/dev/19).
 *
 * <p>A implementação real fica em {@code integracao}; em desenvolvimento, testes e demonstração vale o
 * adaptador {@code ArmazenamentoAncoraSimulado}. Falha na exportação faz a ancoragem do dia falhar fechada.
 */
public interface ArmazenamentoAncoraPort {

    /**
     * Exporta a âncora e devolve a referência do destino (ex.: chave do objeto no armazenamento WORM).
     */
    String exportar(AncoraExportavel ancora);
}
