package br.com.sirej.configuracao;

import java.nio.file.Files;
import java.nio.file.Path;

/** Localiza a raiz do repositório a partir do diretório do módulo, para testes que leem arquivos versionados. */
final class RaizDoRepositorio {

    private static final Path RAIZ = localizar();

    private RaizDoRepositorio() {
    }

    static Path caminho(String relativo) {
        return RAIZ.resolve(relativo);
    }

    private static Path localizar() {
        Path atual = Path.of("").toAbsolutePath();
        while (atual != null) {
            if (Files.isRegularFile(atual.resolve("docs/dev/06-parametrizacao.md"))) {
                return atual;
            }
            atual = atual.getParent();
        }
        throw new IllegalStateException("Raiz do repositório não encontrada a partir de " + Path.of("").toAbsolutePath());
    }
}
