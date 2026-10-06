/**
 * Módulo configuracao (docs/dev/03): carga, validação e versionamento do regimento (docs/dev/06; PT-03).
 *
 * <p>API pública: {@link br.com.sirej.configuracao.RegimentoVigente}, {@link br.com.sirej.configuracao.Regimento}
 * e suas seções, {@link br.com.sirej.configuracao.RegimentoVersao}, o evento
 * {@link br.com.sirej.configuracao.RegimentoVersaoPublicada} e a extensão
 * {@link br.com.sirej.configuracao.VerificadorCofreChaves}. Os subpacotes são internos.
 */
@ApplicationModule(displayName = "Configuração")
package br.com.sirej.configuracao;

import org.springframework.modulith.ApplicationModule;
