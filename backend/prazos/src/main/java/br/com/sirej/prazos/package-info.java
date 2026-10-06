/**
 * Módulo prazos (docs/dev/03): calendário de expediente e motor de prazos (RN01, RN02, RN04, RN10, RN31, RN37; PT-05).
 *
 * <p>API pública: {@link br.com.sirej.prazos.MotorDePrazos}, {@link br.com.sirej.prazos.CalendarioDeExpediente},
 * {@link br.com.sirej.prazos.RegistroDeCalendario} e os tipos de resultado. Parte 1 do PT-05: funções puras sobre
 * datas. Persistência dos prazos, alertas emitidos e eventos {@code PrazoAlertado}/{@code PrazoVencido} vêm na parte 2.
 */
@ApplicationModule(displayName = "Prazos")
package br.com.sirej.prazos;

import org.springframework.modulith.ApplicationModule;
