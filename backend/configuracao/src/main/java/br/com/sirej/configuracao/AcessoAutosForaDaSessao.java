package br.com.sirej.configuracao;

/**
 * Acesso dos membros aos autos fora do dia e horário da reunião ({@code autos.acessoMembrosForaDaSessao}, RN29).
 * Em qualquer valor, só alcança processos já revelados: antes da revelação o sigilo da designação impede o acesso
 * (D-01). {@code MEDIANTE_AUTORIZACAO_COORDENADOR} vem do art. 29, XIII, do regimento de SP (D-26).
 */
public enum AcessoAutosForaDaSessao {
    PERMITIDO,
    MEDIANTE_AUTORIZACAO_COORDENADOR,
    PROIBIDO
}
