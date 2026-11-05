package com.inventystamp.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Lê as configurações do sistema (conexão com o banco e senha das estatísticas)
 * a partir do arquivo config.properties, que fica fora do controle de versão.
 *
 * O arquivo é procurado, nesta ordem:
 *  1. no caminho informado pela propriedade de sistema "inventy.config"
 *     (ex.: java -Dinventy.config=/caminho/config.properties -jar ...);
 *  2. na pasta em que o aplicativo foi iniciado;
 *  3. na pasta em que está o arquivo .jar.
 *
 * Um modelo comentado está em config.exemplo.properties, na raiz do projeto.
 */
public final class Configuracao {

    public static final String NOME_ARQUIVO = "config.properties";

    private static final Properties propriedades = new Properties();
    private static Path arquivoCarregado;

    static {
        carregar();
    }

    private Configuracao() {
    }

    private static void carregar() {
        for (Path candidato : locaisPossiveis()) {
            if (candidato != null && Files.isRegularFile(candidato)) {
                try (InputStream in = Files.newInputStream(candidato);
                     Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                    propriedades.load(reader);
                    arquivoCarregado = candidato.toAbsolutePath();
                    System.out.println("Configuração carregada de: " + arquivoCarregado);
                    return;
                } catch (IOException e) {
                    System.out.println("ERRO ao ler " + candidato + ": " + e.getMessage());
                }
            }
        }
        System.out.println("Arquivo " + NOME_ARQUIVO + " não encontrado. "
                + "Copie config.exemplo.properties para " + NOME_ARQUIVO + " e preencha os dados.");
    }

    private static List<Path> locaisPossiveis() {
        List<Path> locais = new ArrayList<>();
        String informado = System.getProperty("inventy.config");
        if (informado != null && !informado.isBlank()) {
            locais.add(Paths.get(informado));
        }
        locais.add(Paths.get(NOME_ARQUIVO));
        try {
            Path codigo = Paths.get(Configuracao.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path pasta = Files.isDirectory(codigo) ? codigo : codigo.getParent();
            if (pasta != null) {
                locais.add(pasta.resolve(NOME_ARQUIVO));
            }
        } catch (Exception ignorada) {
            // Sem acesso ao local do .jar: usa apenas os locais anteriores.
        }
        return locais;
    }

    /** Valor da chave, sem espaços nas pontas, ou null se ausente ou vazio. */
    public static String get(String chave) {
        String valor = propriedades.getProperty(chave);
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    public static String get(String chave, String valorPadrao) {
        String valor = get(chave);
        return valor != null ? valor : valorPadrao;
    }

    public static boolean arquivoEncontrado() {
        return arquivoCarregado != null;
    }
}
