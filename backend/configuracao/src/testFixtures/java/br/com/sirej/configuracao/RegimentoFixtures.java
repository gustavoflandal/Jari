package br.com.sirej.configuracao;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import br.com.sirej.compartilhado.Hash;
import br.com.sirej.configuracao.RegimentoInvalidoException.Violacao;
import br.com.sirej.configuracao.dominio.JsonCanonico;
import br.com.sirej.configuracao.dominio.RegrasDeConsistencia;
import br.com.sirej.configuracao.infraestrutura.ConversorDeRegistros;
import br.com.sirej.configuracao.infraestrutura.LeitorDeRegimento;
import br.com.sirej.configuracao.infraestrutura.LeitorDeRegimento.RegimentoLido;

/**
 * Regimentos para testes de todos os módulos (docs/dev/06, "Acesso no código"; docs/dev/12, "Testes obrigatórios
 * por regra"). Publicado no artefato {@code sirej-configuracao} com classificador {@code testfixtures}.
 *
 * <pre>{@code
 * Regimento sp = RegimentoFixtures.sp();
 * Regimento variacao = RegimentoFixtures.sp().comVotacao(novaVotacao);
 * RegimentoVigente vigente = RegimentoFixtures.vigente(variacao);
 * }</pre>
 *
 * <p>Os regimentos vêm dos YAML de referência empacotados ({@code config/regimentos}), lidos e validados pelo
 * mesmo caminho da produção (herança, esquema e regras de consistência), com cofre de chaves simulado presente.
 * Uma variação montada com {@code com*} não passa pelo esquema: valide-a com {@link #violacoes(Regimento)} quando o
 * teste depender de ela ser consistente.
 */
public final class RegimentoFixtures {

    private static final String PACOTE = "regimentos/";
    private static final Map<String, RegimentoLido> CACHE = new HashMap<>();

    private RegimentoFixtures() {
    }

    /** Configuração de referência ({@code sp.yaml}). */
    public static Regimento sp() {
        return lido("sp").regimento();
    }

    /** {@code curitiba.yaml}, que herda de {@code sp} (exemplo de herança; Curitiba fora da A0, D-36). */
    public static Regimento curitiba() {
        return lido("curitiba").regimento();
    }

    /** Lê e valida um YAML qualquer, com {@code herda} resolvido contra os regimentos empacotados. */
    public static Regimento deYaml(String nome, String yaml) {
        return leitor(Map.of(nome + LeitorDeRegimento.EXTENSAO, yaml)).ler(nome, true).regimento();
    }

    /** Regimento lido, com conteúdo canônico e hash, pelo nome do YAML empacotado. */
    public static synchronized RegimentoLido lido(String nome) {
        return CACHE.computeIfAbsent(nome, n -> leitor(Map.of()).ler(n, true));
    }

    /**
     * Leitor sobre os YAML empacotados mais arquivos em memória (nome do arquivo → conteúdo), que têm precedência.
     */
    public static LeitorDeRegimento leitor(Map<String, String> arquivosEmMemoria) {
        Map<String, String> copia = Map.copyOf(arquivosEmMemoria);
        return new LeitorDeRegimento(arquivo -> Optional.ofNullable(copia.get(arquivo)).or(() -> doPacote(arquivo)));
    }

    /** Violações das regras de consistência do doc 06 (exceto a 8, que olha o YAML cru), com cofre presente. */
    public static List<Violacao> violacoes(Regimento regimento) {
        return RegrasDeConsistencia.verificar(regimento, true);
    }

    /** {@link RegimentoVigente} fixo, com uma única versão vigente desde sempre. */
    public static RegimentoVigente vigente(Regimento regimento) {
        Hash hash = JsonCanonico.hash(ConversorDeRegistros.paraArvore(regimento));
        RegimentoVersao versao = new RegimentoVersao(UUID.nameUUIDFromBytes(hash.bytes()), hash, Instant.EPOCH,
                "TESTE", regimento);
        return new RegimentoVigente() {
            @Override
            public RegimentoVersao versao() {
                return versao;
            }

            @Override
            public Optional<RegimentoVersao> versao(UUID id) {
                return versao.id().equals(id) ? Optional.of(versao) : Optional.empty();
            }
        };
    }

    private static Optional<String> doPacote(String arquivo) {
        try (InputStream entrada = RegimentoFixtures.class.getClassLoader().getResourceAsStream(PACOTE + arquivo)) {
            return entrada == null ? Optional.empty()
                    : Optional.of(new String(entrada.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
