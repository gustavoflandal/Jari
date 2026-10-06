package br.com.sirej.auditoria;

import br.com.sirej.compartilhado.Hash;

/**
 * Porta do carimbo do tempo usado na ancoragem diária da trilha (docs/dev/08 e docs/dev/09).
 *
 * <p>A implementação real (ACT credenciada) fica em {@code integracao}; em desenvolvimento, testes e
 * demonstração vale o adaptador {@code CarimboTempoSimulado}. Indisponibilidade da porta faz a ancoragem
 * do dia falhar fechada: nada é gravado e a rotina tenta de novo na execução seguinte.
 */
public interface CarimboTempoPort {

    /** Obtém um carimbo do tempo sobre o resumo informado. */
    CarimboTempo carimbar(Hash resumo);
}
