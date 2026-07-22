package com.inventystamp.view;

import com.inventystamp.dao.DemandaDAO;
import com.inventystamp.model.*;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DemandasView {

    private final DemandaDAO demandaDAO = new DemandaDAO();
    private VBox root;
    private VBox listaDemandas;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");

    private static final String TELEFONE_RECURSOS = "5511996891934";
    private static final String TELEFONE_COSTURA  = "5511957323809";

    public DemandasView() {
        construir();
    }

    private void construir() {
        root = new VBox(15);
        root.setPadding(new Insets(20));

        Label titulo = new Label("📢 Demandar");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Caixa de Recursos
        VBox boxRecursos = criarBoxDemanda("Recursos", TELEFONE_RECURSOS, Perfil.RECURSOS, "#8e44ad");

        // Caixa de Costura
        VBox boxCostura = criarBoxDemanda("Costura", TELEFONE_COSTURA, Perfil.COSTURA, "#2980b9");

        // Histórico
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

    private VBox criarBoxDemanda(String nome, String telefone, Perfil perfil, String cor) {
        Label lbl = new Label(nome);
        lbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TextArea taDescricao = new TextArea();
        taDescricao.setPromptText("Digite a demanda para " + nome + "...");
        taDescricao.setPrefRowCount(3);

        // Prazo
        HBox prazoBox = new HBox(10);
        prazoBox.setAlignment(Pos.CENTER_LEFT);

        Label lblPrazo = new Label("Prazo:");
        lblPrazo.setStyle("-fx-font-size: 13px;");

        DatePicker dpPrazo = new DatePicker(LocalDate.now());
        dpPrazo.setPrefWidth(140);

        TextField tfHora = new TextField();
        tfHora.setPromptText("Ex: 17:30");
        tfHora.setPrefWidth(80);

        prazoBox.getChildren().addAll(lblPrazo, dpPrazo, new Label("até as"), tfHora);

        Label feedback = new Label();

        Button btnEnviar = new Button("📤 Enviar para " + nome);
        btnEnviar.setStyle(
                "-fx-background-color: " + cor + "; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-font-size: 12px;"
        );

        btnEnviar.setOnAction(e -> {
            if (taDescricao.getText().isBlank() || dpPrazo.getValue() == null || tfHora.getText().isBlank()) {
                feedback.setText("⚠ Preencha todos os campos.");
                feedback.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }

            int numero = demandaDAO.proximoNumeroDia(perfil);

            Demanda d = new Demanda();
            d.setDescricao(taDescricao.getText().trim());
            d.setSolicitante(Perfil.ATENDIMENTO);
            d.setDestinatario(perfil);
            d.setPrazoData(dpPrazo.getValue());
            d.setPrazoHora(tfHora.getText().trim());
            d.setNumeroDia(numero);
            demandaDAO.salvar(d);

            String mensagem = "*DEMANDA " + numero + " - para " +
                    dpPrazo.getValue().format(fmt) + " até as " + tfHora.getText().trim() + ":* " +
                    taDescricao.getText().trim();

            enviarWhatsApp(telefone, mensagem);

            taDescricao.clear();
            tfHora.clear();
            dpPrazo.setValue(LocalDate.now());
            feedback.setText("✅ Demanda enviada!");
            feedback.setStyle("-fx-text-fill: #27ae60;");

            carregarDemandas();
        });

        VBox box = new VBox(8, lbl, taDescricao, prazoBox, btnEnviar, feedback);
        box.setPadding(new Insets(12));
        box.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2);"
        );

        return box;
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
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2);"
        );

        // Cabeçalho
        HBox cabecalho = new HBox(10);
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        String prazoTexto = "";
        if (d.getPrazoData() != null && d.getPrazoHora() != null) {
            prazoTexto = " - para " + d.getPrazoData().format(fmt) + " até as " + d.getPrazoHora();
        }

        Label lblTitulo = new Label("DEMANDA " + d.getNumeroDia() + prazoTexto);
        lblTitulo.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblPara = new Label("→ " + d.getDestinatario().name());
        lblPara.setStyle(
                "-fx-background-color: #ecf0f1; -fx-padding: 2 8 2 8; " +
                        "-fx-background-radius: 10; -fx-font-size: 11px; -fx-text-fill: #7f8c8d;"
        );

        Label lblStatus = new Label(d.isConcluida() ? "● Concluída" : "● Pendente");
        lblStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: " +
                (d.isConcluida() ? "#27ae60" : "#e67e22") + ";");

        cabecalho.getChildren().addAll(lblTitulo, spacer, lblPara, lblStatus);

        Label descricao = new Label(d.getDescricao());
        descricao.setWrapText(true);
        descricao.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");

        // Botões
        HBox acoes = new HBox(8);
        acoes.setAlignment(Pos.CENTER_RIGHT);

        CheckBox cbConcluida = new CheckBox("Concluída");
        cbConcluida.setSelected(d.isConcluida());
        cbConcluida.setOnAction(e -> {
            if (cbConcluida.isSelected()) {
                card.setStyle(
                        "-fx-background-color: #d5f5e3; -fx-background-radius: 8; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2);"
                );
                FadeTransition fade = new FadeTransition(Duration.millis(1500), card);
                fade.setFromValue(1.0);
                fade.setToValue(0.0);
                fade.setDelay(Duration.millis(800));
                fade.setOnFinished(ev -> {
                    demandaDAO.deletar(d.getId());
                    carregarDemandas();
                });
                fade.play();
            }
        });

        Button btnEditar = new Button("✏ Editar");
        btnEditar.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-font-size: 11px;"
        );
        btnEditar.setOnAction(e -> abrirEdicao(d));

        Button btnRemover = new Button("🗑 Remover");
        btnRemover.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-font-size: 11px;"
        );
        btnRemover.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Remover esta demanda?");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    String telefone = d.getDestinatario() == Perfil.COSTURA ? TELEFONE_COSTURA : TELEFONE_RECURSOS;
                    String mensagem = "*DEMANDA " + d.getNumeroDia() + " cancelada*";
                    enviarWhatsApp(telefone, mensagem);
                    demandaDAO.deletar(d.getId());
                    carregarDemandas();
                }
            });
        });

        acoes.getChildren().addAll(cbConcluida, btnEditar, btnRemover);
        card.getChildren().addAll(cabecalho, descricao, acoes);
        return card;
    }

    private void abrirEdicao(Demanda d) {
        Dialog<Demanda> dialog = new Dialog<>();
        dialog.setTitle("Editar Demanda");

        TextArea taDescricao = new TextArea(d.getDescricao());
        taDescricao.setPrefRowCount(4);

        DatePicker dpPrazo = new DatePicker(d.getPrazoData());
        TextField tfHora = new TextField(d.getPrazoHora() != null ? d.getPrazoHora() : "");
        tfHora.setPromptText("Ex: 17:30");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        grid.add(new Label("Descrição:"), 0, 0); grid.add(taDescricao, 1, 0);
        grid.add(new Label("Data prazo:"), 0, 1); grid.add(dpPrazo, 1, 1);
        grid.add(new Label("Hora prazo:"), 0, 2); grid.add(tfHora, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                d.setDescricao(taDescricao.getText().trim());
                d.setPrazoData(dpPrazo.getValue());
                d.setPrazoHora(tfHora.getText().trim());
                return d;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(demandaAtualizada -> {
            demandaDAO.atualizar(demandaAtualizada);

            String telefone = d.getDestinatario() == Perfil.COSTURA ? TELEFONE_COSTURA : TELEFONE_RECURSOS;
            String prazoTexto = "";
            if (d.getPrazoData() != null && d.getPrazoHora() != null) {
                prazoTexto = " - para " + d.getPrazoData().format(fmt) + " até as " + d.getPrazoHora();
            }
            String mensagem = "*DEMANDA " + d.getNumeroDia() + " - MODIFICADA" + prazoTexto + ":* " + d.getDescricao();
            enviarWhatsApp(telefone, mensagem);

            carregarDemandas();
        });
    }

    private void enviarWhatsApp(String telefone, String mensagem) {
        try {
            String texto = java.net.URLEncoder.encode(mensagem, java.nio.charset.StandardCharsets.UTF_8);
            String url = "https://wa.me/" + telefone + "?text=" + texto;

            // Forçar abertura no Chrome
            String[] comando = {"cmd", "/c", "start", "chrome", url};
            Runtime.getRuntime().exec(comando);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public VBox getView() {
        return root;
    }
}