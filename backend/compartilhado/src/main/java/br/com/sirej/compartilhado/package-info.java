/**
 * Módulo compartilhado (docs/dev/03-arquitetura.md): tipos de valor ({@link br.com.sirej.compartilhado.Cpf},
 * {@link br.com.sirej.compartilhado.Cnpj}, {@link br.com.sirej.compartilhado.Placa},
 * {@link br.com.sirej.compartilhado.Hash}), o {@link br.com.sirej.compartilhado.Relogio} injetável, a marca
 * {@link br.com.sirej.compartilhado.Imutavel} e o erro base {@link br.com.sirej.compartilhado.ValorInvalidoException}.
 *
 * <p>Sem regra de negócio e sem dependência de Spring, JPA ou biblioteca externa: só o JDK (verificado
 * pelos testes de arquitetura em {@code backend/app}). Esta anotação de módulo é a única exceção.
 */
@ApplicationModule(displayName = "Compartilhado")
package br.com.sirej.compartilhado;

import org.springframework.modulith.ApplicationModule;
