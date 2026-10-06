package br.com.sirej.configuracao;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * PT-03 (critério de saída da A0): todo valor do {@code sp.yaml} tem comentário com o artigo de origem, a norma
 * externa ou a dúvida {@code D-xx}; e toda {@code D-xx} citada existe no doc 16.
 */
class SpYamlRastreavelTest {

    /** Linha que só abre um bloco (chave sem valor), sem contar o comentário. */
    private static final Pattern ABRE_BLOCO = Pattern.compile("^\\s*[A-Za-z0-9_]+:\\s*$");

    /** Fonte aceita: artigo, D-xx, norma externa, regra do enunciado ou ADR. */
    private static final Pattern FONTE = Pattern.compile(
            "arts?\\. ?\\d|D-\\d+|CTB|CONTRAN|Lei \\d|Decreto|Edital|RN\\d+|ADR-\\d+");

    private static final Pattern DUVIDA = Pattern.compile("D-(\\d+)");

    @Test
    @DisplayName("PT-03: cada valor do sp.yaml cita artigo, norma ou D-xx")
    void PT03_cada_valor_do_sp_yaml_cita_artigo_ou_duvida() throws IOException {
        List<String> semFonte = new ArrayList<>();
        List<String> linhas = Files.readAllLines(RaizDoRepositorio.caminho("config/regimentos/sp.yaml"));
        for (int i = 0; i < linhas.size(); i++) {
            String linha = linhas.get(i);
            int cerquilha = linha.indexOf('#');
            String valor = cerquilha < 0 ? linha : linha.substring(0, cerquilha);
            if (valor.isBlank() || ABRE_BLOCO.matcher(valor).matches()) {
                continue;
            }
            String comentario = cerquilha < 0 ? "" : linha.substring(cerquilha);
            if (!FONTE.matcher(comentario).find()) {
                semFonte.add((i + 1) + ": " + linha.strip());
            }
        }
        assertThat(semFonte).as("linhas de valor do sp.yaml sem artigo, norma ou D-xx").isEmpty();
    }

    @Test
    @DisplayName("PT-03: toda D-xx citada no sp.yaml existe no doc 16")
    void PT03_duvidas_citadas_no_sp_yaml_existem() throws IOException {
        String sp = Files.readString(RaizDoRepositorio.caminho("config/regimentos/sp.yaml"));
        String doc16 = Files.readString(RaizDoRepositorio.caminho("docs/dev/16-duvidas-abertas.md"));
        Matcher m = DUVIDA.matcher(sp);
        List<String> ausentes = new ArrayList<>();
        while (m.find()) {
            if (!doc16.contains("| " + m.group() + " |")) {
                ausentes.add(m.group());
            }
        }
        assertThat(ausentes).isEmpty();
    }
}
