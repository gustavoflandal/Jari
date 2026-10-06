package br.com.sirej.configuracao;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.sirej.compartilhado.Hash;
import br.com.sirej.configuracao.dominio.JsonCanonico;
import br.com.sirej.configuracao.infraestrutura.LeitorDeRegimento.RegimentoLido;

/** PT-03: hash SHA-256 do conteúdo canônico do regimento (via {@link Hash} do compartilhado). */
class HashDoRegimentoTest {

    @Test
    @DisplayName("PT-03: JSON canônico ordena chaves, não tem espaços e escapa só o necessário")
    void PT03_json_canonico() {
        Map<String, Object> arvore = new LinkedHashMap<>();
        arvore.put("b", List.of(1, true));
        arvore.put("a", Map.of("z", "Tráfego \"x\"\n", "y", Arrays.asList((Object) null)));
        assertThat(JsonCanonico.de(arvore)).isEqualTo("{\"a\":{\"y\":[null],\"z\":\"Tráfego \\\"x\\\"\\n\"},\"b\":[1,true]}");
    }

    @Test
    @DisplayName("PT-03: o hash da versão é o SHA-256 do conteúdo canônico")
    void PT03_hash_e_sha256_do_conteudo_canonico() {
        RegimentoLido sp = RegimentoFixtures.lido("sp");
        assertThat(sp.hash()).isEqualTo(Hash.sha256(sp.conteudoCanonico().getBytes(StandardCharsets.UTF_8)));
        assertThat(sp.conteudoCanonico()).startsWith("{\"administracao\":").doesNotContain("herda").doesNotContain(" #");
    }

    @Test
    @DisplayName("PT-03: mesmo conteúdo com outra ordem de chaves e outros comentários tem o mesmo hash")
    void PT03_hash_independe_de_ordem_e_comentarios() {
        String reescrito = """
                # outro arquivo, mesmo conteúdo que o sp resolvido
                herda: sp
                turmas:
                  presidenteEViceEmTurmasDiferentes: false   # comentário diferente
                  membrosPorTurma: 3
                """;
        RegimentoLido mesmo = RegimentoFixtures.leitor(Map.of("outro.yaml", reescrito)).ler("outro", true);
        assertThat(mesmo.hash()).isEqualTo(RegimentoFixtures.lido("sp").hash());

        RegimentoLido diferente = RegimentoFixtures.leitor(Map.of("outro.yaml", """
                herda: sp
                prazos:
                  exigencia: { dias: 11 }
                """)).ler("outro", true);
        assertThat(diferente.hash()).isNotEqualTo(RegimentoFixtures.lido("sp").hash());
    }

    @Test
    @DisplayName("PT-03: o conteúdo canônico relido dá o mesmo regimento e o mesmo hash")
    void PT03_conteudo_canonico_relido_reproduz_o_regimento() {
        RegimentoLido sp = RegimentoFixtures.lido("sp");
        RegimentoLido relido = RegimentoFixtures.leitor(Map.of()).lerConteudoCanonico("sp", sp.conteudoCanonico(), true);
        assertThat(relido.hash()).isEqualTo(sp.hash());
        assertThat(relido.regimento()).isEqualTo(sp.regimento());
    }
}
