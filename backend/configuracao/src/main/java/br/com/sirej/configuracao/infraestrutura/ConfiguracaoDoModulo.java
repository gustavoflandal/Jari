package br.com.sirej.configuracao.infraestrutura;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Beans internos do módulo. Os regimentos vêm de {@code sirej.regimentos.local} e, se não estiverem lá, dos arquivos
 * de {@code config/regimentos} empacotados em {@code classpath:regimentos/} (o "YAML do pacote" do doc 18, seção 6);
 * assim um regimento local pode herdar de {@code sp}. O esquema é do produto e vem sempre do pacote.
 */
@Configuration(proxyBeanMethods = false)
class ConfiguracaoDoModulo {

    /** Onde o build empacota {@code config/regimentos} (pom do módulo). */
    static final String PACOTE = "classpath:regimentos/";

    @Bean
    LeitorDeRegimento leitorDeRegimento(ResourceLoader recursos,
            @Value("${sirej.regimentos.local:classpath:regimentos/}") String local) {
        String base = local.endsWith("/") ? local : local + "/";
        return new LeitorDeRegimento(nomeDoArquivo -> {
            Optional<String> doPacote = ler(recursos.getResource(PACOTE + nomeDoArquivo));
            if (ValidadorDeEsquema.ARQUIVO.equals(nomeDoArquivo)) {
                return doPacote;
            }
            return ler(recursos.getResource(base + nomeDoArquivo)).or(() -> doPacote);
        });
    }

    private static Optional<String> ler(Resource recurso) {
        if (!recurso.exists()) {
            return Optional.empty();
        }
        try (InputStream entrada = recurso.getInputStream()) {
            return Optional.of(new String(entrada.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException("falha ao ler " + recurso.getDescription(), e);
        }
    }
}
