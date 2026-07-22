package com.inventystamp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.inventystamp.view.PedidosView;
import com.inventystamp.view.AtendimentoView;
import com.inventystamp.view.DemandasView;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        mostrarTelaInicial(stage);
    }

    public static void mostrarTelaInicial(Stage stage) {
        Label titulo = new Label("Inventy Stamp");
        titulo.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subtitulo = new Label("O que deseja fazer?");
        subtitulo.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        Button btnAtender = criarBotao("🎧 Atender");
        Button btnPedidos = criarBotao("📋 Ver Pedidos");
        Button btnDemandar = criarBotao("📢 Demandar");

        btnAtender.setOnAction(e -> mostrarTela(stage, "atendimento"));
        btnPedidos.setOnAction(e -> mostrarTela(stage, "pedidos"));
        btnDemandar.setOnAction(e -> mostrarTela(stage, "demandas"));

        VBox botoes = new VBox(12, btnAtender, btnPedidos, btnDemandar);
        botoes.setAlignment(Pos.CENTER);

        VBox root = new VBox(20, titulo, subtitulo, botoes);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #f4f6f7;");

        Scene scene = new Scene(root, 420, 380);
        stage.setTitle("Inventy Stamp");
        stage.setScene(scene);
        stage.show();
    }

    public static void mostrarTela(Stage stage, String tela) {
        VBox menu = new VBox(8);
        menu.setPadding(new Insets(20, 10, 20, 10));
        menu.setPrefWidth(180);
        menu.setStyle("-fx-background-color: #2c3e50;");

        StackPane conteudo = new StackPane();
        conteudo.setStyle("-fx-background-color: #f4f6f7;");

        menu.getChildren().addAll(
                criarBotaoMenu("🎧 Atender", () -> conteudo.getChildren().setAll(new AtendimentoView().getView())),
                criarBotaoMenu("📋 Ver Pedidos", () -> conteudo.getChildren().setAll(new PedidosView().getView())),
                criarBotaoMenu("📢 Demandar", () -> conteudo.getChildren().setAll(new DemandasView().getView()))
        );

        switch (tela) {
            case "atendimento" -> conteudo.getChildren().setAll(new AtendimentoView().getView());
            case "pedidos" -> conteudo.getChildren().setAll(new PedidosView().getView());
            case "demandas" -> conteudo.getChildren().setAll(new DemandasView().getView());
        }

        Button btnVoltar = new Button("↩ Voltar");
        btnVoltar.setPrefWidth(160);
        btnVoltar.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-font-size: 12px;"
        );
        btnVoltar.setOnAction(e -> mostrarTelaInicial(stage));
        VBox.setMargin(btnVoltar, new Insets(20, 0, 0, 0));
        menu.getChildren().add(btnVoltar);

        BorderPane root = new BorderPane();
        root.setLeft(menu);
        root.setCenter(conteudo);

        Scene scene = new Scene(root, 950, 650);
        stage.setScene(scene);
        stage.setMaximized(true);
    }

    private static Button criarBotao(String texto) {
        Button btn = new Button(texto);
        btn.setPrefWidth(220);
        btn.setPrefHeight(45);
        btn.setStyle(
                "-fx-font-size: 14px; -fx-background-color: #2980b9; " +
                        "-fx-text-fill: white; -fx-background-radius: 6;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-font-size: 14px; -fx-background-color: #1a5276; " +
                        "-fx-text-fill: white; -fx-background-radius: 6;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-font-size: 14px; -fx-background-color: #2980b9; " +
                        "-fx-text-fill: white; -fx-background-radius: 6;"
        ));
        return btn;
    }

    private static Button criarBotaoMenu(String texto, Runnable acao) {
        Button btn = new Button(texto);
        btn.setPrefWidth(160);
        btn.setPrefHeight(38);
        btn.setStyle(
                "-fx-background-color: #34495e; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-font-size: 12px;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #2980b9; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-font-size: 12px;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #34495e; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-font-size: 12px;"
        ));
        btn.setOnAction(e -> acao.run());
        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}