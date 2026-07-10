package com.inventystamp.view;

import com.inventystamp.dao.DemandaDAO;
import com.inventystamp.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class DemandasView {

    private Usuario usuario;
    private DemandaDAO demandaDAO = new DemandaDAO();
    private VBox root;
    private VBox listaDemandas;

    public DemandasView(Usuario usuario) {
        this.usuario = usuario;
        construir();
    }

    private void construir() {
        root = new VBox(15);
        root.setPadding(new Insets(20));

        Label titulo = new Label("📢 Demandas");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Formulário nova demanda
        TitledPane novaDemanda = new TitledPane();
        novaDemanda.setText("+ Nova Demanda");
        novaDemanda.setExpanded(true);

        VBox formBox = new VBox(10);
        formBox.setPadding(new Insets(10));

        Label lblPara = new Label("Demandar para:");
        lblPara.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        ComboBox<String> cbDestinatario = new ComboBox<>();

        // Define destinatários disponíveis por perfil
        Perfil perfil = usuario.getPerfil();
        if (perfil == Perfil.ATENDIMENTO) {
            cbDestinatario.getItems().addAll("Costura", "Recursos");
        } else if (perfil == Perfil.COSTURA) {
            cbDestinatario.getItems().addAll("Recursos");
        } else if (perfil == Perfil.BORDADO) {
            cbDestinatario.getItems().addAll("Costura", "Recursos");
        } else if (perfil == Perfil.RECURSOS) {
            cbDestinatario.getItems().addAll("Costura");
        } else if (perfil == Perfil.PRODUCAO) {
            cbDestinatario.getItems().addAll("Costura", "Recursos");
        }

        cbDestinatario.setPromptText("Selecione...");
        cbDestinatario.setPrefWidth(250);

        TextArea taDescricao = new TextArea();
        taDescricao.setPromptText(
                "Descreva a demanda...\n\n" +
                        "Exemplos:\n" +
                        "• Fabricar 2 camisetas G brancas em algodão\n" +
                        "• Fazer barra na calça jeans\n" +
                        "• Colocar gola na blusa"
        );
        taDescricao.setPrefRowCount(4);

        Label lblFeedback = new Label();

        Button btnEnviar = new Button("📤 Enviar Demanda");
        btnEnviar.setStyle(
                "-fx-background-color: #8e44ad; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-font-size: 12px;"
        );
        btnEnviar.setOnAction(e -> {
            if (cbDestinatario.getValue() == null || taDescricao.getText().isBlank()) {
                lblFeedback.setText("⚠ Preencha todos os campos.");
                lblFeedback.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }

            Perfil destinatario = cbDestinatario.getValue().equals("Costura")
                    ? Perfil.COSTURA : Perfil.RECURSOS;

            Demanda d = new Demanda();
            d.setDescricao(taDescricao.getText().trim());
            d.setSolicitante(usuario.getPerfil());
            d.setDestinatario(destinatario);
            demandaDAO.salvar(d);

            taDescricao.clear();
            cbDestinatario.setValue(null);
            lblFeedback.setText("✅ Demanda enviada!");
            lblFeedback.setStyle("-fx-text-fill: #27ae60;");

            carregarDemandas();
        });

        formBox.getChildren().addAll(lblPara, cbDestinatario, taDescricao, btnEnviar, lblFeedback);
        novaDemanda.setContent(formBox);

        // Lista de demandas recebidas
        Label lblRecebidas = new Label("Demandas recebidas:");
        lblRecebidas.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        listaDemandas = new VBox(10);
        ScrollPane scroll = new ScrollPane(listaDemandas);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        root.getChildren().addAll(titulo, novaDemanda, lblRecebidas, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        carregarDemandas();
    }

    private void carregarDemandas() {
        listaDemandas.getChildren().clear();
        List<Demanda> demandas = demandaDAO.listarPorDestinatario(usuario.getPerfil());

        if (demandas.isEmpty()) {
            Label vazio = new Label("Nenhuma demanda recebida.");
            vazio.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");
            listaDemandas.getChildren().add(vazio);
            return;
        }

        for (Demanda d : demandas) {
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

        Label lblDe = new Label("De: " + d.getSolicitante().name());
        lblDe.setStyle(
                "-fx-background-color: #ecf0f1; -fx-padding: 2 8 2 8; " +
                        "-fx-background-radius: 10; -fx-font-size: 11px; -fx-text-fill: #7f8c8d;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblStatus = new Label(d.isConcluida() ? "● Concluída" : "● Pendente");
        lblStatus.setStyle("-fx-font-size: 12px; -fx-text-fill: " +
                (d.isConcluida() ? "#27ae60" : "#e67e22") + ";");

        cabecalho.getChildren().addAll(lblDe, spacer, lblStatus);

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