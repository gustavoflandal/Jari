package br.com.sirej.prazos;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import br.com.sirej.configuracao.Regimento;

/**
 * Motor de prazos (RN01, RN02, RN04, RN10, RN31, RN37). Funções puras sobre datas: não lê relógio, banco nem
 * configuração por conta própria. Os prazos e as antecedências dos alertas vêm do {@link Regimento}; nenhum número de
 * prazo existe neste código.
 *
 * <p>Contagem em dias corridos: o dia do termo inicial não conta e o último dia conta, então o vencimento é o termo
 * inicial mais os dias do prazo (D-54). Se o vencimento cai em dia não útil e o regimento manda prorrogar
 * ({@code calendario.prorrogaVencimentoEmDiaNaoUtil}), passa para o próximo dia útil (RN31).
 */
public final class MotorDePrazos {

    private final CalendarioDeExpediente calendario;
    private final boolean prorrogaVencimentoEmDiaNaoUtil;

    public MotorDePrazos(CalendarioDeExpediente calendario, boolean prorrogaVencimentoEmDiaNaoUtil) {
        this.calendario = Objects.requireNonNull(calendario, "calendario");
        this.prorrogaVencimentoEmDiaNaoUtil = prorrogaVencimentoEmDiaNaoUtil;
    }

    /** Motor com a política de calendário do regimento e os registros de calendário vigentes. */
    public static MotorDePrazos de(Regimento regimento, Collection<RegistroDeCalendario> registros) {
        Regimento.SecaoCalendario secao = regimento.calendario();
        return new MotorDePrazos(CalendarioDeExpediente.de(secao, registros), secao.prorrogaVencimentoEmDiaNaoUtil());
    }

    /**
     * Conta um prazo em dias corridos a partir do termo inicial.
     *
     * @throws PrazoNaoCalculavelException se o prazo não tem {@code dias} positivos (em sessões, impresso na
     *                                     notificação ou meta sem prazo legal)
     */
    public PrazoCalculado calcular(Regimento.Prazo regra, LocalDate termoInicial) {
        Objects.requireNonNull(termoInicial, "termoInicial");
        LocalDate bruto = termoInicial.plusDays(diasDe(regra));
        LocalDate vencimento = prorrogaVencimentoEmDiaNaoUtil ? calendario.proximoDiaUtil(bruto) : bruto;
        return new PrazoCalculado(termoInicial, bruto, vencimento);
    }

    /**
     * Data da ciência a considerar para abrir o prazo (RN01, RN02): a ciência efetiva, se houver e for anterior à
     * presumida; senão a presumida, que ocorre {@code prazos.cienciaPresumidaEletronica} dias depois da inclusão no
     * meio eletrônico. A data da ciência não se prorroga: só o vencimento (D-54).
     */
    public LocalDate cienciaConsiderada(Optional<LocalDate> cienciaEfetiva, LocalDate inclusaoNoMeioEletronico,
            Regimento.Prazo cienciaPresumidaEletronica) {
        Objects.requireNonNull(inclusaoNoMeioEletronico, "inclusaoNoMeioEletronico");
        LocalDate presumida = inclusaoNoMeioEletronico.plusDays(diasDe(cienciaPresumidaEletronica));
        return cienciaEfetiva.filter(efetiva -> efetiva.isBefore(presumida)).orElse(presumida);
    }

    /** Datas dos alertas de um vencimento, da mais antiga para a mais recente (RN37). */
    public static List<LocalDate> datasDeAlerta(LocalDate vencimento, Regimento.Alertas alertas) {
        return alertas.diasAntes().stream().map(vencimento::minusDays).sorted().toList();
    }

    /** Antecedências (em dias) cujo alerta cai em {@code hoje} para o vencimento informado (RN37). */
    public static List<Integer> alertasDoDia(LocalDate hoje, LocalDate vencimento, Regimento.Alertas alertas) {
        return alertas.diasAntes().stream().filter(antecedencia -> vencimento.minusDays(antecedencia).equals(hoje))
                .toList();
    }

    private static int diasDe(Regimento.Prazo regra) {
        Integer dias = regra.dias();
        if (dias == null || dias <= 0) {
            throw new PrazoNaoCalculavelException(
                    "O prazo não é contado em dias corridos (em sessões, impresso na notificação ou sem prazo legal).");
        }
        return dias;
    }
}
