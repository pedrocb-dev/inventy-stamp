package com.inventystamp.view;

import com.inventystamp.dao.DemandaDAO;
import com.inventystamp.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;

public class DemandasView {

    private DemandaDAO demandaDAO = new DemandaDAO();
    private VBox root;
    private VBox listaDemandas;

    // Coloque aqui os números dos responsáveis (só números, sem traço ou parênteses)
    private static final String TELEFONE_RECURSOS = "5511996891934";
    private static final String TELEFONE_COSTURA  = "5511982772189";

    public DemandasView() {
        construir();
    }

    private void construir() {
        root = new VBox(15);
        root.setPadding(new Insets(20));

        Label titulo = new Label("📢 Demandar");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Caixa de Recursos
        Label lblRecursos = new Label("Recursos");
        lblRecursos.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TextArea taRecursos = new TextArea();
        taRecursos.setPromptText("Digite a demanda para Recursos...");
        taRecursos.setPrefRowCount(4);

        Label feedbackRecursos = new Label();

        Button btnRecursos = new Button("📤 Enviar para Recursos");
        btnRecursos.setStyle(
                "-fx-background-color: #8e44ad; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-font-size: 12px;"
        );
        btnRecursos.setOnAction(e -> {
            if (taRecursos.getText().isBlank()) {
                feedbackRecursos.setText("⚠ Escreva a demanda antes de enviar.");
                feedbackRecursos.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }
            enviarWhatsApp(TELEFONE_RECURSOS, taRecursos.getText().trim());

            Demanda d = new Demanda();
            d.setDescricao(taRecursos.getText().trim());
            d.setSolicitante(Perfil.ATENDIMENTO);
            d.setDestinatario(Perfil.RECURSOS);
            demandaDAO.salvar(d);

            taRecursos.clear();
            feedbackRecursos.setText("✅ Mensagem enviada!");
            feedbackRecursos.setStyle("-fx-text-fill: #27ae60;");
        });

        VBox boxRecursos = new VBox(8, lblRecursos, taRecursos, btnRecursos, feedbackRecursos);
        boxRecursos.setPadding(new Insets(12));
        boxRecursos.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0, 0, 2);"
        );

        // Caixa de Costura
        Label lblCostura = new Label("Costura");
        lblCostura.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TextArea taCostura = new TextArea();
        taCostura.setPromptText("Digite a demanda para Costura...");
        taCostura.setPrefRowCount(4);

        Label feedbackCostura = new Label();

        Button btnCostura = new Button("📤 Enviar para Costura");
        btnCostura.setStyle(
                "-fx-background-color: #2980b9; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-font-size: 12px;"
        );
        btnCostura.setOnAction(e -> {
            if (taCostura.getText().isBlank()) {
                feedbackCostura.setText("⚠ Escreva a demanda antes de enviar.");
                feedbackCostura.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }
            enviarWhatsApp(TELEFONE_COSTURA, taCostura.getText().trim());

            Demanda d = new Demanda();
            d.setDescricao(taCostura.getText().trim());
            d.setSolicitante(Perfil.ATENDIMENTO);
            d.setDestinatario(Perfil.COSTURA);
            demandaDAO.salvar(d);

            taCostura.clear();
            feedbackCostura.setText("✅ Mensagem enviada!");
            feedbackCostura.setStyle("-fx-text-fill: #27ae60;");
        });

        VBox boxCostura = new VBox(8, lblCostura, taCostura, btnCostura, feedbackCostura);
        boxCostura.setPadding(new Insets(12));
        boxCostura.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0, 0, 2);"
        );

        // Histórico de demandas
        Label lblHistorico = new Label("Histórico de demandas:");
        lblHistorico.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        listaDemandas = new VBox(10);
        ScrollPane scroll = new ScrollPane(listaDemandas);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        root.getChildren().addAll(titulo, boxRecursos, boxCostura, lblHistorico, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        carregarDemandas();
    }

    private void enviarWhatsApp(String telefone, String mensagem) {
        try {
            String texto = java.net.URLEncoder.encode(mensagem, "UTF-8");
            String url = "https://wa.me/" + telefone + "?text=" + texto;
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void carregarDemandas() {
        listaDemandas.getChildren().clear();
        List<Demanda> todasDemandas = new java.util.ArrayList<>();
        todasDemandas.addAll(demandaDAO.listarPorDestinatario(Perfil.RECURSOS));
        todasDemandas.addAll(demandaDAO.listarPorDestinatario(Perfil.COSTURA));

        if (todasDemandas.isEmpty()) {
            Label vazio = new Label("Nenhuma demanda registrada.");
            vazio.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");
            listaDemandas.getChildren().add(vazio);
            return;
        }

        for (Demanda d : todasDemandas) {
            listaDemandas.getChildren().add(criarCardDemanda(d));
        }
    }

    private VBox criarCardDemanda(Demanda d) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0, 0, 2);"
        );

        HBox cabecalho = new HBox(10);
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        Label lblPara = new Label("Para: " + d.getDestinatario().name());
        lblPara.setStyle(
                "-fx-background-color: #ecf0f1; -fx-padding: 2 8 2 8; " +
                        "-fx-background-radius: 10; -fx-font-size: 11px; -fx-text-fill: #7f8c8d;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblStatus = new Label(d.isConcluida() ? "● Concluída" : "● Pendente");
        lblStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: " +
                (d.isConcluida() ? "#27ae60" : "#e67e22") + ";");

        cabecalho.getChildren().addAll(lblPara, spacer, lblStatus);

        Label descricao = new Label(d.getDescricao());
        descricao.setWrapText(true);
        descricao.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");

        HBox acoes = new HBox(8);
        acoes.setAlignment(Pos.CENTER_RIGHT);

        CheckBox cbConcluida = new CheckBox("Concluída");
        cbConcluida.setSelected(d.isConcluida());
        cbConcluida.setOnAction(e -> {
            d.setConcluida(cbConcluida.isSelected());
            demandaDAO.atualizar(d);
            carregarDemandas();
        });

        Button btnRemover = new Button("🗑 Remover");
        btnRemover.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-font-size: 11px;"
        );
        btnRemover.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Remover esta demanda?");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    demandaDAO.deletar(d.getId());
                    carregarDemandas();
                }
            });
        });

        acoes.getChildren().addAll(cbConcluida, btnRemover);
        card.getChildren().addAll(cabecalho, descricao, acoes);
        return card;
    }

    public VBox getView() {
        return root;
    }
}