package com.inventystamp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fornece as conexões com o banco de dados MySQL.
 *
 * Os dados de acesso vêm do arquivo config.properties (ver Configuracao):
 *  - db.url, db.usuario, db.senha: banco principal (na loja, o servidor da rede local);
 *  - db.alternativo.url, db.alternativo.usuario, db.alternativo.senha (opcionais):
 *    banco usado quando o principal não responde dentro de db.timeout.segundos
 *    (por exemplo, uma base de testes local durante o desenvolvimento).
 */
public class ConexaoDB {

    public static Connection getConexao() throws SQLException {
        String url = Configuracao.get("db.url");
        if (url == null) {
            throw new SQLException("Banco de dados não configurado: informe db.url em "
                    + Configuracao.NOME_ARQUIVO + " (veja config.exemplo.properties).");
        }
        String usuario = Configuracao.get("db.usuario", "");
        String senha = Configuracao.get("db.senha", "");

        String urlAlternativa = Configuracao.get("db.alternativo.url");
        if (urlAlternativa == null) {
            return DriverManager.getConnection(url, usuario, senha);
        }

        try {
            DriverManager.setLoginTimeout(lerTimeout());
            Connection conn = DriverManager.getConnection(url, usuario, senha);
            System.out.println("Conectado ao banco principal.");
            return conn;
        } catch (SQLException e) {
            System.out.println("Banco principal não encontrado, conectando ao banco alternativo...");
            return DriverManager.getConnection(urlAlternativa,
                    Configuracao.get("db.alternativo.usuario", ""),
                    Configuracao.get("db.alternativo.senha", ""));
        }
    }

    private static int lerTimeout() {
        try {
            return Integer.parseInt(Configuracao.get("db.timeout.segundos", "2"));
        } catch (NumberFormatException e) {
            return 2;
        }
    }
}
