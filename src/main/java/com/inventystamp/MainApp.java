package com.inventystamp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.inventystamp.model.Perfil;
import com.inventystamp.model.Usuario;
import com.inventystamp.dao.UsuarioDAO;
import com.inventystamp.view.PedidosView;
import com.inventystamp.view.TarefasView;
import com.inventystamp.view.AtendimentoView;
import com.inventystamp.view.DemandasView;

import java.util.List;

public class MainApp extends Application {

    public static Usuario usuarioLogado;

    @Override
    public void start(Stage stage) {
        mostrarTelaLogin(stage);
    }

    public static void mostrarTelaLogin(Stage stage) {
        Label titulo = new Label("Inventy Stamp");
        titulo.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subtitulo = new Label("Quem está entrando?");
        subtitulo.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        VBox botoes = new VBox(10);
        botoes.setAlignment(Pos.CENTER);

        UsuarioDAO dao = new UsuarioDAO();
        List<Usuario> usuarios = dao.listarTodos();

        for (Usuario u : usuarios) {
            Button btn = new Button(u.getNome());
            btn.setPrefWidth(220);
            btn.setPrefHeight(40);
            btn.setStyle(
                    "-fx-font-size: 13px; -fx-background-color: #2980b9; " +
                            "-fx-text-fill: white; -fx-background-radius: 6;"
            );
            btn.setOnMouseEntered(e -> btn.setStyle(
                    "-fx-font-size: 13px; -fx-background-color: #1a5276; " +
                            "-fx-text-fill: white; -fx-background-radius: 6;"
            ));
            btn.setOnMouseExited(e -> btn.setStyle(
                    "-fx-font-size: 13px; -fx-background-color: #2980b9; " +
                            "-fx-text-fill: white; -fx-background-radius: 6;"
            ));
            btn.setOnAction(e -> {
                usuarioLogado = u;
                mostrarTelaPrincipal(stage);
            });
            botoes.getChildren().add(btn);
        }

        VBox root = new VBox(20, titulo, subtitulo, botoes);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #f4f6f7;");

        Scene scene = new Scene(root, 420, 480);
        stage.setTitle("Inventy Stamp");
        stage.setScene(scene);
        stage.show();
    }

    public static void mostrarTelaPrincipal(Stage stage) {
        Perfil perfil = usuarioLogado.getPerfil();

        VBox menu = new VBox(8);
        menu.setPadding(new Insets(20, 10, 20, 10));
        menu.setPrefWidth(180);
        menu.setStyle("-fx-background-color: #2c3e50;");

        Label nomeUsuario = new Label(usuarioLogado.getNome());
        nomeUsuario.setStyle("-fx-font-size: 13px; -fx-text-fill: #bdc3c7; -fx-font-weight: bold;");
        nomeUsuario.setPadding(new Insets(0, 0, 15, 5));
        menu.getChildren().add(nomeUsuario);

        StackPane conteudo = new StackPane();
        conteudo.setStyle("-fx-background-color: #f4f6f7;");

        if (perfil == Perfil.ATENDIMENTO) {
            menu.getChildren().addAll(
                    criarBotaoMenu("📋 Pedidos", () -> conteudo.getChildren().setAll(new PedidosView(usuarioLogado).getView())),
                    criarBotaoMenu("✅ Tarefas", () -> conteudo.getChildren().setAll(new TarefasView(usuarioLogado).getView())),
                    criarBotaoMenu("🎧 Atendimento", () -> conteudo.getChildren().setAll(new AtendimentoView(usuarioLogado).getView())),
                    criarBotaoMenu("📢 Demandas", () -> conteudo.getChildren().setAll(new DemandasView(usuarioLogado).getView()))
            );
            conteudo.getChildren().setAll(new PedidosView(usuarioLogado).getView());
        } else if (perfil == Perfil.COSTURA) {
            menu.getChildren().addAll(
                    criarBotaoMenu("📢 Demandas", () -> conteudo.getChildren().setAll(new DemandasView(usuarioLogado).getView()))
            );
            conteudo.getChildren().setAll(new DemandasView(usuarioLogado).getView());
        } else if (perfil == Perfil.BORDADO) {
            menu.getChildren().addAll(
                    criarBotaoMenu("📋 Pedidos", () -> conteudo.getChildren().setAll(new PedidosView(usuarioLogado).getView())),
                    criarBotaoMenu("📢 Demandas", () -> conteudo.getChildren().setAll(new DemandasView(usuarioLogado).getView()))
            );
            conteudo.getChildren().setAll(new PedidosView(usuarioLogado).getView());
        } else if (perfil == Perfil.RECURSOS) {
            menu.getChildren().addAll(
                    criarBotaoMenu("📢 Demandas", () -> conteudo.getChildren().setAll(new DemandasView(usuarioLogado).getView()))
            );
            conteudo.getChildren().setAll(new DemandasView(usuarioLogado).getView());
        } else if (perfil == Perfil.PRODUCAO) {
            menu.getChildren().addAll(
                    criarBotaoMenu("📋 Pedidos", () -> conteudo.getChildren().setAll(new PedidosView(usuarioLogado).getView())),
                    criarBotaoMenu("✅ Tarefas", () -> conteudo.getChildren().setAll(new TarefasView(usuarioLogado).getView())),
                    criarBotaoMenu("📢 Demandas", () -> conteudo.getChildren().setAll(new DemandasView(usuarioLogado).getView()))
            );
            conteudo.getChildren().setAll(new PedidosView(usuarioLogado).getView());
        }

        Button btnSair = new Button("↩ Sair");
        btnSair.setPrefWidth(160);
        btnSair.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-font-size: 12px;"
        );
        btnSair.setOnAction(e -> mostrarTelaLogin(stage));
        VBox.setMargin(btnSair, new Insets(20, 0, 0, 0));
        menu.getChildren().add(btnSair);

        BorderPane root = new BorderPane();
        root.setLeft(menu);
        root.setCenter(conteudo);

        Scene scene = new Scene(root, 950, 650);
        stage.setScene(scene);
        stage.setMaximized(true);
    }

    private static Button criarBotaoMenu(String texto, Runnable acao) {
        Button btn = new Button(texto);
        btn.setPrefWidth(160);
        btn.setPrefHeight(38);
        btn.setStyle(
                "-fx-background-color: #34495e; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-font-size: 12px; -fx-alignment: CENTER-LEFT;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #2980b9; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-font-size: 12px; -fx-alignment: CENTER-LEFT;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #34495e; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-font-size: 12px; -fx-alignment: CENTER-LEFT;"
        ));
        btn.setOnAction(e -> acao.run());
        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}