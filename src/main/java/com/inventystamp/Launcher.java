package com.inventystamp;

/**
 * Ponto de entrada alternativo do aplicativo.
 *
 * Quando a classe executada herda de javafx.application.Application (caso da MainApp),
 * o Java exige o JavaFX no module path. Esta classe, que não herda de Application,
 * permite executar o sistema pela IDE apenas com as bibliotecas do JavaFX no classpath,
 * como já declarado no pom.xml, sem instalar o JavaFX SDK separadamente.
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
