package br.com.sirej.prazos;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import br.com.sirej.configuracao.Regimento;

/**
 * Calendário de expediente do órgão (RN31): diz se uma data é dia útil.
 *
 * <p>Não é dia útil: sábado, domingo, feriado e ponto facultativo das abrangências listadas em
 * {@code calendario.feriados} do regimento, e qualquer dia coberto por uma suspensão de expediente (ato aprovado
 * pela administração, que vale sempre, qualquer que seja a abrangência). Registro de feriado ou ponto facultativo de
 * abrangência fora da lista é ignorado (D-54).
 *
 * <p>Imutável. Sem acesso ao relógio: recebe as datas.
 */
public final class CalendarioDeExpediente {

    private final List<RegistroDeCalendario> registros;

    public CalendarioDeExpediente(Collection<RegistroDeCalendario> registros, Set<Abrangencia> consideradas) {
        Objects.requireNonNull(registros, "registros");
        Set<Abrangencia> validas = EnumSet.noneOf(Abrangencia.class);
        validas.addAll(Objects.requireNonNull(consideradas, "consideradas"));
        this.registros = registros.stream().filter(r -> vale(r, validas)).toList();
    }

    /** Calendário com as abrangências de {@code calendario.feriados} do regimento. */
    public static CalendarioDeExpediente de(Regimento.SecaoCalendario secao, Collection<RegistroDeCalendario> registros) {
        Set<Abrangencia> consideradas = EnumSet.noneOf(Abrangencia.class);
        for (String nome : secao.feriados()) {
            consideradas.add(Abrangencia.valueOf(nome));
        }
        return new CalendarioDeExpediente(registros, consideradas);
    }

    private static boolean vale(RegistroDeCalendario registro, Set<Abrangencia> consideradas) {
        return registro.tipo() == TipoDeRegistro.SUSPENSAO_EXPEDIENTE || consideradas.contains(registro.abrangencia());
    }

    /** A data tem expediente: não é fim de semana, feriado, ponto facultativo nem suspensão. */
    public boolean ehDiaUtil(LocalDate data) {
        DayOfWeek dia = data.getDayOfWeek();
        if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) {
            return false;
        }
        return registros.stream().noneMatch(r -> r.cobre(data));
    }

    /** A própria data, se for dia útil; senão o primeiro dia útil depois dela. */
    public LocalDate proximoDiaUtil(LocalDate data) {
        LocalDate candidata = data;
        while (!ehDiaUtil(candidata)) {
            candidata = candidata.plusDays(1);
        }
        return candidata;
    }
}
