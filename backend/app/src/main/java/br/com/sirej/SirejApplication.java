package br.com.sirej;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

/**
 * Ponto de entrada do monólito modular. Módulo técnico de inicialização (docs/dev/03): não contém
 * regra de negócio; só reúne os módulos de contexto, cada um num subpacote direto de {@code br.com.sirej}.
 */
@SpringBootApplication
@Modulithic(systemName = "SIREJ")
public class SirejApplication {

    public static void main(String[] args) {
        SpringApplication.run(SirejApplication.class, args);
    }
}
