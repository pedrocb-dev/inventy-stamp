package com.inventystamp.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoDB {

    private static final String URL_LOJA = "jdbc:mysql://IP_REMOVIDO:3306/inventy_stamp?useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true";
    private static final String USUARIO_LOJA = "inventy";
    private static final String SENHA_LOJA = "***REMOVIDO***";

    private static final String URL_TESTE = "jdbc:mysql://localhost:3306/inventy_stamp_teste?useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true";
    private static final String USUARIO_TESTE = "root";
    private static final String SENHA_TESTE = "***REMOVIDO***";

    public static Connection getConexao() throws SQLException {
        try {
            DriverManager.setLoginTimeout(2);
            Connection conn = DriverManager.getConnection(URL_LOJA, USUARIO_LOJA, SENHA_LOJA);
            System.out.println("Conectado ao banco da LOJA.");
            return conn;
        } catch (SQLException e) {
            System.out.println("Banco da loja não encontrado, conectando ao banco de TESTE local...");
            return DriverManager.getConnection(URL_TESTE, USUARIO_TESTE, SENHA_TESTE);
        }
    }
}