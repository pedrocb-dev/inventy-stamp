package com.inventystamp.view;

import com.inventystamp.dao.RascunhoDAO;
import com.inventystamp.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.inventystamp.controller.PedidoController;

public class AtendimentoView {

    private final PedidoController pedidoController = new PedidoController();
    private final RascunhoDAO rascunhoDAO = new RascunhoDAO();
    private VBox root;
    private final DateTimeFormatter fmtSql = DateTimeFormatter.ISO_LOCAL_DATE;

    private TextField tfNome, tfTelefone, tfValor, tfResta;
    private ToggleButton tbACombinar;
    private TextArea taAnotacao;
    private ComboBox<TipoPersonalizacao> cbTipo;
    private ComboBox<StatusPagamento> cbPagamento;
    private ComboBox<String> cbNF;
    private ComboBox<String> cbCupomFiscal;
    private TextArea taDemanda;
    private ToggleButton tbMarco, tbSimone;
    private ComboBox<String> cbAtendente;
    private DatePicker dpConfirmacao, dpEntrega;
    private VBox campoResta;
    private VBox listaRascunhos;
    private Label lblAvisoRascunho;

    public AtendimentoView() {
        construir();
    }

    private void construir() {
        root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #e8eaed;");

        Label titulo = new Label("🎧 Novo Pedido");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

// Ícone de aviso — triângulo laranja com "!" vazado
        StackPane iconeAviso = new StackPane();
        iconeAviso.setPrefSize(28, 26);

        javafx.scene.shape.Polygon triangulo = new javafx.scene.shape.Polygon();
        triangulo.getPoints().addAll(
                14.0, 0.0,
                28.0, 26.0,
                0.0, 26.0
        );
        triangulo.setFill(javafx.scene.paint.Color.web("#e67e22"));

        Label exclamacao = new Label("!");
        exclamacao.setStyle(
                "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; " +
                        "-fx-font-family: 'Arial'; -fx-effect: none;"
        );
        exclamacao.setTranslateY(3);

        iconeAviso.getChildren().addAll(triangulo, exclamacao);

        lblAvisoRascunho = new Label("rascunho(s) pendente(s)");
        lblAvisoRascunho.setStyle("-fx-font-size: 13px; -fx-text-fill: #e67e22; -fx-font-weight: bold;");

        HBox avisoBox = new HBox(6, iconeAviso, lblAvisoRascunho);
        avisoBox.setAlignment(Pos.CENTER_LEFT);
        avisoBox.setVisible(false);
        avisoBox.setManaged(false);
        avisoBox.setPadding(new Insets(4, 10, 4, 10));
        avisoBox.setStyle(
                "-fx-background-color: #fef5e7; -fx-background-radius: 6; " +
                        "-fx-border-color: #e67e22; -fx-border-radius: 6; -fx-border-width: 1;"
        );

        HBox cabecalhoTitulo = new HBox(16, titulo, avisoBox);
        cabecalhoTitulo.setAlignment(Pos.CENTER_LEFT);

        HBox container = new HBox(20);
        container.setAlignment(Pos.TOP_CENTER);

        VBox formCard = new VBox(14);
        formCard.setPadding(new Insets(24));
        formCard.setPrefWidth(620);
        formCard.setMaxWidth(620);
        formCard.setStyle(
                "-fx-background-color: white; -fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);"
        );

        tfNome = criarTextField("Nome do cliente", 200);
        tfTelefone = criarTextField("(11) 99999-9999", 140);

        tfTelefone.textProperty().addListener((obs, oldVal, newVal) -> {
            String digitos = newVal.replaceAll("[^0-9]", "");
            if (digitos.length() > 11) digitos = digitos.substring(0, 11);

            StringBuilder formatado = new StringBuilder();
            if (digitos.length() > 0) {
                formatado.append("(").append(digitos, 0, Math.min(2, digitos.length()));
            }
            if (digitos.length() >= 3) {
                formatado.append(") ").append(digitos, 2, Math.min(7, digitos.length()));
            }
            if (digitos.length() >= 8) {
                formatado.append("-").append(digitos, 7, digitos.length());
            }

            if (!formatado.toString().equals(newVal)) {
                tfTelefone.setText(formatado.toString());
                tfTelefone.positionCaret(formatado.length());
            }
        });

        Button btnCopiarTelefone = new Button("💬");
        btnCopiarTelefone.setTooltip(new Tooltip("Abrir WhatsApp"));
        btnCopiarTelefone.setPrefSize(38, 38);
        btnCopiarTelefone.setStyle(
                "-fx-background-color: #25D366; -fx-text-fill: white; " +
                        "-fx-background-radius: 8; -fx-font-size: 15px;"
        );
        btnCopiarTelefone.setOnMouseEntered(e -> btnCopiarTelefone.setStyle(
                "-fx-background-color: #1ebe57; -fx-text-fill: white; " +
                        "-fx-background-radius: 8; -fx-font-size: 15px;"
        ));
        btnCopiarTelefone.setOnMouseExited(e -> btnCopiarTelefone.setStyle(
                "-fx-background-color: #25D366; -fx-text-fill: white; " +
                        "-fx-background-radius: 8; -fx-font-size: 15px;"
        ));
        Label lblCopiado = new Label("Abrindo...");
        lblCopiado.setStyle("-fx-font-size: 10px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
        lblCopiado.setVisible(false);
        lblCopiado.setMinWidth(60);
        lblCopiado.setAlignment(Pos.CENTER);

        btnCopiarTelefone.setOnAction(e -> {
            if (!tfTelefone.getText().isBlank()) {
                try {
                    String numeros = tfTelefone.getText().replaceAll("[^0-9]", "");
                    String numeroComDDI = numeros.startsWith("55") ? numeros : "55" + numeros;
                    String url = "whatsapp://send?phone=" + numeroComDDI;
                    Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", url});

                    lblCopiado.setVisible(true);
                    lblCopiado.setOpacity(1.0);

                    javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(
                            javafx.util.Duration.millis(1200), lblCopiado);
                    fade.setFromValue(1.0);
                    fade.setToValue(0.0);
                    fade.setDelay(javafx.util.Duration.millis(600));
                    fade.setOnFinished(ev -> lblCopiado.setVisible(false));
                    fade.play();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        VBox botaoComLegenda = new VBox(2, btnCopiarTelefone, lblCopiado);
        botaoComLegenda.setAlignment(Pos.CENTER);

        HBox telefoneComBotao = new HBox(6, tfTelefone, botaoComLegenda);
        telefoneComBotao.setAlignment(Pos.TOP_LEFT);

        HBox linha1 = new HBox(12,
                criarCampo("👤 Cliente", tfNome),
                criarCampoFixo("📞 Telefone", telefoneComBotao, 200)
        );

        taAnotacao = new TextArea();
        taAnotacao.setPrefRowCount(5);
        taAnotacao.setStyle(
                "-fx-font-size: 14px; -fx-background-radius: 6; " +
                        "-fx-border-color: #dcdde1; -fx-border-radius: 6;"
        );
        VBox campoAnotacao = criarCampo("📝 Anotação", taAnotacao);

        cbTipo = new ComboBox<>();
        cbTipo.getItems().addAll(TipoPersonalizacao.values());
        cbTipo.setValue(TipoPersonalizacao.DTF);
        cbTipo.setMaxWidth(Double.MAX_VALUE);
        cbTipo.setStyle("-fx-font-size: 14px;");

        cbPagamento = new ComboBox<>();
        cbPagamento.getItems().addAll(StatusPagamento.values());
        cbPagamento.setPromptText("Selecione...");
        cbPagamento.setMaxWidth(Double.MAX_VALUE);
        cbPagamento.setStyle("-fx-font-size: 14px;");

        HBox linha2 = new HBox(12,
                criarCampo("🎨 Personalização", cbTipo),
                criarCampo("💳 Pagamento", cbPagamento)
        );

        dpConfirmacao = new DatePicker(LocalDate.now());
        dpConfirmacao.setMaxWidth(Double.MAX_VALUE);
        dpConfirmacao.setStyle("-fx-font-size: 14px;");

        dpEntrega = new DatePicker();
        dpEntrega.setMaxWidth(Double.MAX_VALUE);
        dpEntrega.setStyle("-fx-font-size: 14px;");

        HBox linha3 = new HBox(12,
                criarCampo("📅 Confirmação", dpConfirmacao),
                criarCampo("📅 Entrega", dpEntrega)
        );

        tfValor = criarTextField("Ex: 65.00", 120);

        tbACombinar = new ToggleButton("A combinar");
        tbACombinar.setStyle(
                "-fx-font-size: 12px; -fx-background-radius: 6; " +
                        "-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50;"
        );
        tbACombinar.selectedProperty().addListener((obs, oldVal, novoValor) -> {
            tfValor.setDisable(novoValor);
            if (novoValor) tfValor.clear();
            tbACombinar.setStyle(novoValor
                    ? "-fx-font-size: 12px; -fx-background-radius: 6; -fx-background-color: #8e44ad; -fx-text-fill: white;"
                    : "-fx-font-size: 12px; -fx-background-radius: 6; -fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50;"
            );

            if (novoValor) {
                cbPagamento.setValue(StatusPagamento.PAGAR_NA_RETIRADA);
            }
            cbPagamento.setDisable(novoValor);
        });
        VBox campoValor = new VBox(6);
        Label lblValor = new Label("💰 Valor (R$)");
        lblValor.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        HBox linhaValorBotao = new HBox(6, tfValor, tbACombinar);
        linhaValorBotao.setAlignment(Pos.CENTER_LEFT);
        campoValor.getChildren().addAll(lblValor, linhaValorBotao);
        campoValor.setPrefWidth(220);
        campoValor.setMinWidth(220);
        campoValor.setMaxWidth(220);

        tfResta = criarTextField("Valor restante", 120);
        campoResta = criarCampoFixo("⚠ Resta (R$)", tfResta, 160);
        campoResta.setVisible(false);
        campoResta.setManaged(false);

        cbPagamento.setOnAction(e -> {
            boolean isParcial = cbPagamento.getValue() == StatusPagamento.PARCIAL;
            campoResta.setVisible(isParcial);
            campoResta.setManaged(isParcial);
        });

        HBox linha4 = new HBox(12,
                campoValor,
                campoResta
        );

        cbNF = new ComboBox<>();
        cbNF.getItems().addAll("Não precisa de NF", "Precisa de NF");
        cbNF.setValue("Não precisa de NF");
        cbNF.setMaxWidth(Double.MAX_VALUE);
        cbNF.setStyle("-fx-font-size: 14px;");

        cbCupomFiscal = new ComboBox<>();
        cbCupomFiscal.getItems().addAll("Não precisa de C. Fiscal", "Precisa de C. Fiscal");
        cbCupomFiscal.setValue("Não precisa de C. Fiscal");
        cbCupomFiscal.setMaxWidth(Double.MAX_VALUE);
        cbCupomFiscal.setStyle("-fx-font-size: 14px;");

        HBox linha5 = new HBox(12,
                criarCampo("🧾 Nota Fiscal", cbNF),
                criarCampo("🧾 Cupom Fiscal", cbCupomFiscal)
        );

        cbAtendente = new ComboBox<>();
        cbAtendente.getItems().addAll("Kennia", "Pedro", "Bia", "Marco");
        cbAtendente.setPromptText("Selecione...");
        cbAtendente.setMaxWidth(Double.MAX_VALUE);
        cbAtendente.setStyle("-fx-font-size: 14px;");

        HBox linha6 = new HBox(12,
                criarCampo("👩‍💼 Atendente", cbAtendente)
        );

        Label lblFeedback = new Label();
        lblFeedback.setStyle("-fx-font-size: 14px;");

        Button btnSalvar = new Button("💾 Salvar Pedido");
        btnSalvar.setPrefWidth(200);
        btnSalvar.setPrefHeight(44);
        btnSalvar.setStyle(
                "-fx-background-color: #27ae60; -fx-text-fill: white; " +
                        "-fx-background-radius: 8; -fx-font-size: 15px; -fx-font-weight: bold;"
        );
        btnSalvar.setOnMouseEntered(e -> btnSalvar.setStyle(
                "-fx-background-color: #1e8449; -fx-text-fill: white; " +
                        "-fx-background-radius: 8; -fx-font-size: 15px; -fx-font-weight: bold;"
        ));
        btnSalvar.setOnMouseExited(e -> btnSalvar.setStyle(
                "-fx-background-color: #27ae60; -fx-text-fill: white; " +
                        "-fx-background-radius: 8; -fx-font-size: 15px; -fx-font-weight: bold;"
        ));

        Button btnSalvarRascunho = new Button("📝 Salvar Rascunho");
        btnSalvarRascunho.setPrefWidth(200);
        btnSalvarRascunho.setPrefHeight(44);
        btnSalvarRascunho.setStyle(
                "-fx-background-color: #7f8c8d; -fx-text-fill: white; " +
                        "-fx-background-radius: 8; -fx-font-size: 14px;"
        );

        btnSalvarRascunho.setOnAction(e -> {
            rascunhoDAO.salvar(montarRascunhoAtual());
            carregarRascunhos();

            lblFeedback.setText("📝 Rascunho salvo!");
            lblFeedback.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");
        });

        VBox sugestoesCard = new VBox(12);
        sugestoesCard.setPadding(new Insets(24));
        sugestoesCard.setPrefWidth(280);
        sugestoesCard.setMinWidth(280);
        sugestoesCard.setMaxWidth(280);
        sugestoesCard.setAlignment(Pos.TOP_LEFT);
        sugestoesCard.setStyle(
                "-fx-background-color: white; -fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);"
        );

        Label lblSugestoesTitulo = new Label("💡 Sugestões de anotação");
        lblSugestoesTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label lblSugestao1 = new Label("Tamanho, cor e quantidade de peças.");
        lblSugestao1.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        lblSugestao1.setWrapText(true);

        Label lblSugestao2 = new Label("Tamanho de estampa A4 ou A3.");
        lblSugestao2.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        lblSugestao2.setWrapText(true);

        Label lblSugestao3 = new Label("Pedir peças pra Simone ou pro Marco.");
        lblSugestao3.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        lblSugestao3.setWrapText(true);

        Label lblSugestao4 = new Label("Tamanhos: Pequeno até 10, Médio até 30, Grande até 50, Bem grande mais de 50.");
        lblSugestao4.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        lblSugestao4.setWrapText(true);

        Label lblSugestao5 = new Label("Mostrar ao cliente a diferença entre estampa e bordado.");
        lblSugestao5.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        lblSugestao5.setWrapText(true);

        sugestoesCard.getChildren().addAll(lblSugestoesTitulo, lblSugestao1, lblSugestao2, lblSugestao3, lblSugestao4, lblSugestao5);

// Container de demanda
        VBox demandaCard = new VBox(12);
        demandaCard.setPadding(new Insets(24));
        demandaCard.setPrefWidth(280);
        demandaCard.setMinWidth(280);
        demandaCard.setMaxWidth(280);
        demandaCard.setAlignment(Pos.TOP_LEFT);
        demandaCard.setStyle(
                "-fx-background-color: white; -fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);"
        );

        Label lblDemandaTitulo = new Label("📢 Demandar (opcional)");
        lblDemandaTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        taDemanda = new TextArea();
        taDemanda.setPromptText("Escreva a demanda relacionada a este pedido...");
        taDemanda.setPrefRowCount(4);
        taDemanda.setStyle(
                "-fx-font-size: 13px; -fx-background-radius: 6; " +
                        "-fx-border-color: #dcdde1; -fx-border-radius: 6;"
        );

        tbMarco = new ToggleButton("Marco");
        tbSimone = new ToggleButton("Simone");
        ToggleGroup grupoDestinatario = new ToggleGroup();
        tbMarco.setToggleGroup(grupoDestinatario);
        tbSimone.setToggleGroup(grupoDestinatario);
        tbMarco.setStyle("-fx-font-size: 13px; -fx-pref-width: 110;");
        tbSimone.setStyle("-fx-font-size: 13px; -fx-pref-width: 110;");

        HBox linhaDestinatarios = new HBox(8, tbMarco, tbSimone);

        demandaCard.getChildren().addAll(lblDemandaTitulo, taDemanda, linhaDestinatarios);

        HBox botaoBox = new HBox(10, btnSalvar, btnSalvarRascunho);
        botaoBox.setAlignment(Pos.CENTER_LEFT);

        btnSalvar.setOnAction(e -> {
            boolean valido = true;

            if (tfNome.getText() == null || tfNome.getText().isBlank()) {
                tfNome.setStyle("-fx-font-size: 14px; -fx-background-radius: 6; -fx-border-color: #e74c3c; -fx-border-radius: 6; -fx-border-width: 2;");
                valido = false;
            } else {
                tfNome.setStyle("-fx-font-size: 14px; -fx-background-radius: 6; -fx-border-color: #dcdde1; -fx-border-radius: 6;");
            }

            if (cbTipo.getValue() == null) {
                cbTipo.setStyle("-fx-font-size: 14px; -fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 4;");
                valido = false;
            } else {
                cbTipo.setStyle("-fx-font-size: 14px;");
            }

            if (cbPagamento.getValue() == null) {
                cbPagamento.setStyle("-fx-font-size: 14px; -fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 4;");
                valido = false;
            } else {
                cbPagamento.setStyle("-fx-font-size: 14px;");
            }

            if (dpConfirmacao.getValue() == null) {
                dpConfirmacao.setStyle("-fx-font-size: 14px; -fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 4;");
                valido = false;
            } else {
                dpConfirmacao.setStyle("-fx-font-size: 14px;");
            }

            if (dpEntrega.getValue() == null) {
                dpEntrega.setStyle("-fx-font-size: 14px; -fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 4;");
                valido = false;
            } else {
                dpEntrega.setStyle("-fx-font-size: 14px;");
            }

            if (taAnotacao.getText() == null || taAnotacao.getText().isBlank()) {
                taAnotacao.setStyle("-fx-font-size: 14px; -fx-background-radius: 6; -fx-border-color: #e74c3c; -fx-border-radius: 6; -fx-border-width: 2;");
                valido = false;
            } else {
                taAnotacao.setStyle("-fx-font-size: 14px; -fx-background-radius: 6; -fx-border-color: #dcdde1; -fx-border-radius: 6;");
            }

            if (!tbACombinar.isSelected() && (tfValor.getText() == null || tfValor.getText().isBlank())) {
                tfValor.setStyle("-fx-font-size: 14px; -fx-background-radius: 6; -fx-border-color: #e74c3c; -fx-border-radius: 6; -fx-border-width: 2;");
                valido = false;
            } else {
                tfValor.setStyle("-fx-font-size: 14px; -fx-background-radius: 6; -fx-border-color: #dcdde1; -fx-border-radius: 6;");
            }

            if (!tbACombinar.isSelected() && (tfValor.getText() == null || tfValor.getText().isBlank())) {
                tfValor.setStyle("-fx-font-size: 14px; -fx-background-radius: 6; -fx-border-color: #e74c3c; -fx-border-radius: 6; -fx-border-width: 2;");
                valido = false;
            } else {
                tfValor.setStyle("-fx-font-size: 14px; -fx-background-radius: 6; -fx-border-color: #dcdde1; -fx-border-radius: 6;");
            }

            if (!valido) {
                lblFeedback.setText("⚠ Preencha os campos obrigatórios destacados em vermelho.");
                lblFeedback.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px;");
                return;
            }
            String destinatarioDemanda = tbMarco.isSelected() ? "MARCO" : tbSimone.isSelected() ? "SIMONE" : null;

            PedidoController.ResultadoSalvar resultado = pedidoController.salvarPedido(
                    tfNome.getText(),
                    tfTelefone.getText(),
                    taAnotacao.getText(),
                    cbTipo.getValue(),
                    dpConfirmacao.getValue(),
                    dpEntrega.getValue(),
                    cbPagamento.getValue(),
                    tfValor.getText(),
                    tbACombinar.isSelected(),
                    tfResta.getText(),
                    cbNF.getValue().equals("Precisa de NF"),
                    cbCupomFiscal.getValue().equals("Precisa de C. Fiscal"),
                    cbAtendente.getValue(),
                    taDemanda.getText(),
                    destinatarioDemanda
            );

            if (!resultado.sucesso) {
                lblFeedback.setText(resultado.mensagem);
                lblFeedback.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px;");
                return;
            }

            limparFormulario();
            lblFeedback.setText(resultado.mensagem);
            lblFeedback.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 14px;");
        });

        VBox colunaLateral = new VBox(20, sugestoesCard, demandaCard);
        container.getChildren().addAll(formCard, colunaLateral);

        formCard.getChildren().addAll(linha1, campoAnotacao, linha2, linha3, linha4, linha5, linha6);

        VBox bottomBox = new VBox(10, botaoBox, lblFeedback);
        bottomBox.setMaxWidth(620 + 280 + 20);

        VBox rascunhosCard = new VBox(12);
        rascunhosCard.setPadding(new Insets(20));
        rascunhosCard.setMaxWidth(620 + 280 + 20);
        rascunhosCard.setStyle(
                "-fx-background-color: white; -fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);"
        );

        Label lblRascunhosTitulo = new Label("📝 Rascunhos salvos");
        lblRascunhosTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        listaRascunhos = new VBox(8);

        rascunhosCard.getChildren().addAll(lblRascunhosTitulo, listaRascunhos);

        VBox tudo = new VBox(20, container, bottomBox, rascunhosCard);
        tudo.setAlignment(Pos.TOP_CENTER);

        ScrollPane scroll = new ScrollPane(tudo);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(cabecalhoTitulo, scroll);

        carregarRascunhos();
    }

    private void carregarRascunhos() {
        listaRascunhos.getChildren().clear();
        List<RascunhoPedido> rascunhos = rascunhoDAO.listarTodos();

        HBox avisoBox = (HBox) lblAvisoRascunho.getParent();
        if (rascunhos.isEmpty()) {
            avisoBox.setVisible(false);
            avisoBox.setManaged(false);
        } else {
            avisoBox.setVisible(true);
            avisoBox.setManaged(true);
            lblAvisoRascunho.setText(rascunhos.size() + " rascunho(s) pendente(s)");
        }

        if (rascunhos.isEmpty()) {
            Label vazio = new Label("Nenhum rascunho salvo.");
            vazio.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");
            listaRascunhos.getChildren().add(vazio);
            return;
        }

        for (RascunhoPedido r : rascunhos) {
            listaRascunhos.getChildren().add(criarCardRascunho(r));
        }
    }

    private HBox criarCardRascunho(RascunhoPedido r) {
        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10));
        card.setStyle(
                "-fx-background-color: #f8f9fa; -fx-background-radius: 6; " +
                        "-fx-border-color: #dcdde1; -fx-border-radius: 6;"
        );

        String nome = (r.getClienteNome() == null || r.getClienteNome().isBlank())
                ? "(sem nome)" : r.getClienteNome();

        Label lblInfo = new Label("👤 " + nome +
                (r.getDataEntrega() != null ? "  •  entrega: " + r.getDataEntrega() : ""));
        lblInfo.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnCarregar = new Button("📂 Carregar");
        btnCarregar.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-font-size: 12px;"
        );
        btnCarregar.setOnAction(e -> carregarRascunhoNoFormulario(r));

        Button btnExcluir = new Button("🗑 Excluir");
        btnExcluir.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-font-size: 12px;"
        );
        btnExcluir.setOnAction(e -> {
            rascunhoDAO.deletar(r.getId());
            carregarRascunhos();
        });

        card.getChildren().addAll(lblInfo, spacer, btnCarregar, btnExcluir);
        return card;
    }

    private void carregarRascunhoNoFormulario(RascunhoPedido r) {
        // Se já tem algo sendo digitado, salva como rascunho antes de sobrescrever
        boolean temConteudoAtual = !tfNome.getText().isBlank() || !taAnotacao.getText().isBlank() || !taDemanda.getText().isBlank();
        if (temConteudoAtual) {
            rascunhoDAO.salvar(montarRascunhoAtual());
        }

        tfNome.setText(r.getClienteNome() != null ? r.getClienteNome() : "");
        tfTelefone.setText(r.getClienteTelefone() != null ? r.getClienteTelefone() : "");
        taAnotacao.setText(r.getAnotacao() != null ? r.getAnotacao() : "");

        if (r.getTipoPersonalizacao() != null) {
            try { cbTipo.setValue(TipoPersonalizacao.valueOf(r.getTipoPersonalizacao())); }
            catch (Exception ex) { cbTipo.setValue(null); }
        }

        if (r.getStatusPagamento() != null) {
            try {
                cbPagamento.setValue(StatusPagamento.valueOf(r.getStatusPagamento()));
                boolean isParcial = cbPagamento.getValue() == StatusPagamento.PARCIAL;
                campoResta.setVisible(isParcial);
                campoResta.setManaged(isParcial);
            } catch (Exception ex) { cbPagamento.setValue(null); }
        }

        if (r.getDataConfirmacao() != null && !r.getDataConfirmacao().isBlank()) {
            try { dpConfirmacao.setValue(LocalDate.parse(r.getDataConfirmacao())); }
            catch (Exception ignored) {}
        }
        if (r.getDataEntrega() != null && !r.getDataEntrega().isBlank()) {
            try { dpEntrega.setValue(LocalDate.parse(r.getDataEntrega())); }
            catch (Exception ignored) {}
        }

        tfValor.setText(r.getValor() != null ? r.getValor() : "");
        tfResta.setText(r.getResta() != null ? r.getResta() : "");
        cbNF.setValue(r.isPrecisaNf() ? "Precisa de NF" : "Não precisa de NF");
        cbCupomFiscal.setValue(r.isPrecisaCupomFiscal() ? "Precisa de C. Fiscal" : "Não precisa de C. Fiscal");
        cbAtendente.setValue(r.getAtendente());
        taDemanda.setText(r.getDemandaTexto() != null ? r.getDemandaTexto() : "");
        tbMarco.setSelected("MARCO".equals(r.getDemandaDestinatario()));
        tbSimone.setSelected("SIMONE".equals(r.getDemandaDestinatario()));

        rascunhoDAO.deletar(r.getId());
        carregarRascunhos();
    }

    private void limparFormulario() {
        tfNome.clear();
        tfTelefone.clear();
        taAnotacao.clear();
        cbTipo.setValue(null);
        dpConfirmacao.setValue(LocalDate.now());
        dpEntrega.setValue(null);
        tfValor.clear();
        tbACombinar.setSelected(false);
        tfValor.setDisable(false);
        tfResta.clear();
        cbPagamento.setValue(null);
        cbNF.setValue("Não precisa de NF");
        cbAtendente.setValue(null);
        campoResta.setVisible(false);
        campoResta.setManaged(false);
        taDemanda.clear();
        tbMarco.setSelected(false);
        tbSimone.setSelected(false);
        tfNome.setStyle("-fx-font-size: 14px; -fx-background-radius: 6; -fx-border-color: #dcdde1; -fx-border-radius: 6;");
        cbTipo.setStyle("-fx-font-size: 14px;");
        cbPagamento.setStyle("-fx-font-size: 14px;");
        dpConfirmacao.setStyle("-fx-font-size: 14px;");
        dpEntrega.setStyle("-fx-font-size: 14px;");
        taAnotacao.setStyle("-fx-font-size: 14px; -fx-background-radius: 6; -fx-border-color: #dcdde1; -fx-border-radius: 6;");
        tfValor.setStyle("-fx-font-size: 14px; -fx-background-radius: 6; -fx-border-color: #dcdde1; -fx-border-radius: 6;");

    }

    private VBox criarCampo(String labelTexto, javafx.scene.Node campo) {
        Label lbl = new Label(labelTexto);
        lbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        VBox box = new VBox(6, lbl, campo);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private VBox criarCampoFixo(String labelTexto, javafx.scene.Node campo, double largura) {
        Label lbl = new Label(labelTexto);
        lbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        VBox box = new VBox(6, lbl, campo);
        box.setPrefWidth(largura);
        box.setMinWidth(largura);
        box.setMaxWidth(largura);
        return box;
    }

    private TextField criarTextField(String prompt, double largura) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(largura);
        tf.setPrefHeight(38);
        tf.setStyle(
                "-fx-font-size: 14px; -fx-background-radius: 6; " +
                        "-fx-border-color: #dcdde1; -fx-border-radius: 6;"
        );
        return tf;
    }

    private RascunhoPedido montarRascunhoAtual() {
        RascunhoPedido r = new RascunhoPedido();
        r.setClienteNome(tfNome.getText().trim());
        r.setClienteTelefone(tfTelefone.getText().trim());
        r.setAnotacao(taAnotacao.getText().trim());
        r.setTipoPersonalizacao(cbTipo.getValue() != null ? cbTipo.getValue().name() : null);
        r.setDataConfirmacao(dpConfirmacao.getValue() != null ? dpConfirmacao.getValue().format(fmtSql) : null);
        r.setDataEntrega(dpEntrega.getValue() != null ? dpEntrega.getValue().format(fmtSql) : null);
        r.setValor(tfValor.getText().trim());
        r.setStatusPagamento(cbPagamento.getValue() != null ? cbPagamento.getValue().name() : null);
        r.setResta(tfResta.getText().trim());
        r.setPrecisaNf(cbNF.getValue().equals("Precisa de NF"));
        r.setPrecisaCupomFiscal(cbCupomFiscal.getValue().equals("Precisa de C. Fiscal"));
        r.setAtendente(cbAtendente.getValue());
        r.setDemandaTexto(taDemanda.getText().trim());
        r.setDemandaDestinatario(tbMarco.isSelected() ? "MARCO" : tbSimone.isSelected() ? "SIMONE" : null);
        return r;
    }

    public void salvarRascunhoAutomatico() {
        boolean temConteudo = !tfNome.getText().isBlank() || !taAnotacao.getText().isBlank() || !taDemanda.getText().isBlank();
        if (temConteudo) {
            rascunhoDAO.salvar(montarRascunhoAtual());
        }
    }

    public VBox getView() {

        return root;
    }
}