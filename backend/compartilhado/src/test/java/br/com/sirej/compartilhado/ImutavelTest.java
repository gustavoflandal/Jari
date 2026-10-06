package br.com.sirej.compartilhado;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ImutavelTest {

    @Imutavel
    record RegistroDeExemplo(String id) {
    }

    record RegistroComum(String id) {
    }

    @Test
    @DisplayName("PT-02: @Imutavel fica visível em tempo de execução, só em tipos, e documentada")
    void PT02_imutavel_visivel_em_tempo_de_execucao() {
        assertThat(Imutavel.class.getAnnotation(Retention.class).value()).isEqualTo(RetentionPolicy.RUNTIME);
        assertThat(Imutavel.class.getAnnotation(Target.class).value()).containsExactly(ElementType.TYPE);
        assertThat(Imutavel.class.isAnnotationPresent(Documented.class)).isTrue();
    }

    @Test
    @DisplayName("PT-02: @Imutavel marca o tipo anotado e só ele")
    void PT02_imutavel_marca_so_o_tipo_anotado() {
        assertThat(RegistroDeExemplo.class.isAnnotationPresent(Imutavel.class)).isTrue();
        assertThat(RegistroComum.class.isAnnotationPresent(Imutavel.class)).isFalse();
    }
}
