package com.inventystamp.view;

import com.inventystamp.dao.TarefaDAO;
import com.inventystamp.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class TarefasView {

    private Usuario usuario;
    private TarefaDAO tarefaDAO = new TarefaDAO();
    private VBox root;
    private VBox listaTarefas;

    public TarefasView(Usuario usuario) {
        this.usuario = usuario;
        construir();
    }

    private void construir() {
        root = new VBox(15);
        root.setPadding(new Insets(20));

        Label titulo = new Label("✅ Tarefas");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        listaTarefas = new VBox(10);
        ScrollPane scroll = new ScrollPane(listaTarefas);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        root.getChildren().addAll(titulo, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        carregarTarefas();
    }

    private void carregarTarefas() {
        listaTarefas.getChildren().clear();
        List<Tarefa> tarefas = tarefaDAO.listarPorDestino(usuario.getPerfil());

        if (tarefas.isEmpty()) {
            Label vazio = new Label("Nenhuma tarefa encontrada.");
            vazio.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");
            listaTarefas.getChildren().add(vazio);
            return;
        }

        for (Tarefa t : tarefas) {
            listaTarefas.getChildren().add(criarCardTarefa(t));
        }
    }

    private VBox criarCardTarefa(Tarefa t) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0, 0, 2);"
        );

        HBox cabecalho = new HBox(10);
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        CheckBox cb = new CheckBox(t.getDescricao());
        cb.setSelected(t.isConcluida());
        cb.setStyle("-fx-font-size: 13px;");
        cb.setOnAction(e -> {
            t.setConcluida(cb.isSelected());
            tarefaDAO.atualizar(t);
        });

        Label origem = new Label(t.getOrigem().name());
        origem.setStyle(
                "-fx-background-color: #ecf0f1; -fx-padding: 2 8 2 8; " +
                        "-fx-background-radius: 10; -fx-font-size: 11px; -fx-text-fill: #7f8c8d;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        cabecalho.getChildren().addAll(cb, spacer, origem);

        HBox acoes = new HBox(8);
        acoes.setAlignment(Pos.CENTER_RIGHT);

        Button btnEditar = new Button("✏ Editar");
        btnEditar.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-font-size: 11px;"
        );
        btnEditar.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog(t.getDescricao());
            dialog.setTitle("Editar Tarefa");
            dialog.setHeaderText("Editar descrição:");
            dialog.showAndWait().ifPresent(novaDesc -> {
                t.setDescricao(novaDesc);
                tarefaDAO.atualizar(t);
                carregarTarefas();
            });
        });

        Button btnRemover = new Button("🗑 Remover");
        btnRemover.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-font-size: 11px;"
        );
        btnRemover.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Remover esta tarefa?");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    tarefaDAO.deletar(t.getId());
                    carregarTarefas();
                }
            });
        });

        acoes.getChildren().addAll(btnEditar, btnRemover);
        card.getChildren().addAll(cabecalho, acoes);
        return card;
    }

    public VBox getView() {
        return root;
    }
}