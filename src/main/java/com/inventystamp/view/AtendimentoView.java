package com.inventystamp.view;

import com.inventystamp.dao.PedidoDAO;
import com.inventystamp.dao.RascunhoDAO;
import com.inventystamp.dao.TarefaDAO;
import com.inventystamp.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AtendimentoView {

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final TarefaDAO tarefaDAO = new TarefaDAO();
    private final RascunhoDAO rascunhoDAO = new RascunhoDAO();
    private VBox root;
    private File arquivoLayoutSelecionado = null;
    private static final String PASTA_LAYOUTS = System.getProperty("user.home") + "\\Desktop\\layouts-inventy\\";
    private final DateTimeFormatter fmtSql = DateTimeFormatter.ISO_LOCAL_DATE;

    private TextField tfNome, tfTelefone, tfValor, tfResta;
    private TextArea taAnotacao;
    private ComboBox<TipoPersonalizacao> cbTipo;
    private ComboBox<StatusPagamento> cbPagamento;
    private ComboBox<String> cbNF;
    private DatePicker dpConfirmacao, dpEntrega;
    private VBox campoResta;
    private VBox listaRascunhos;

    public AtendimentoView() {
        construir();
    }

    private void construir() {
        root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #e8eaed;");

        Label titulo = new Label("🎧 Novo Pedido");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

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

        Button btnCopiarTelefone = new Button("📋");
        btnCopiarTelefone.setTooltip(new Tooltip("Copiar telefone"));
        btnCopiarTelefone.setPrefSize(38, 38);
        btnCopiarTelefone.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; " +
                        "-fx-background-radius: 8; -fx-font-size: 15px;"
        );
        btnCopiarTelefone.setOnMouseEntered(e -> btnCopiarTelefone.setStyle(
                "-fx-background-color: #2980b9; -fx-text-fill: white; " +
                        "-fx-background-radius: 8; -fx-font-size: 15px;"
        ));
        btnCopiarTelefone.setOnMouseExited(e -> btnCopiarTelefone.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; " +
                        "-fx-background-radius: 8; -fx-font-size: 15px;"
        ));
        Label lblCopiado = new Label("Copiado!");
        lblCopiado.setStyle("-fx-font-size: 10px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
        lblCopiado.setVisible(false);
        lblCopiado.setMinWidth(60);
        lblCopiado.setAlignment(Pos.CENTER);

        btnCopiarTelefone.setOnAction(e -> {
            if (!tfTelefone.getText().isBlank()) {
                javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
                javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
                content.putString(tfTelefone.getText().trim());
                clipboard.setContent(content);

                lblCopiado.setVisible(true);
                lblCopiado.setOpacity(1.0);

                javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(
                        javafx.util.Duration.millis(1200), lblCopiado);
                fade.setFromValue(1.0);
                fade.setToValue(0.0);
                fade.setDelay(javafx.util.Duration.millis(600));
                fade.setOnFinished(ev -> lblCopiado.setVisible(false));
                fade.play();
            }
        });

        VBox botaoComLegenda = new VBox(2, btnCopiarTelefone, lblCopiado);
        botaoComLegenda.setAlignment(Pos.CENTER);

        HBox telefoneComBotao = new HBox(6, tfTelefone, botaoComLegenda);
        telefoneComBotao.setAlignment(Pos.CENTER_LEFT);

        HBox linha1 = new HBox(12,
                criarCampo("👤 Cliente *", tfNome),
                criarCampoFixo("📞 Telefone", telefoneComBotao, 200)
        );

        taAnotacao = new TextArea();
        taAnotacao.setPromptText(
                "Anotações livres...\n\n" +
                        "Sugestões:\n" +
                        "• O cliente trouxe a peça?\n" +
                        "• A peça é da empresa? (tamanho, cor)\n" +
                        "• Quantidade de peças\n" +
                        "• Posição da personalização\n" +
                        "• Tamanho da estampa"
        );
        taAnotacao.setPrefRowCount(5);
        taAnotacao.setStyle(
                "-fx-font-size: 14px; -fx-background-radius: 6; " +
                        "-fx-border-color: #dcdde1; -fx-border-radius: 6;"
        );
        VBox campoAnotacao = criarCampo("📝 Anotação", taAnotacao);

        cbTipo = new ComboBox<>();
        cbTipo.getItems().addAll(TipoPersonalizacao.SEM_PERSONALIZACAO);
        for (TipoPersonalizacao t : TipoPersonalizacao.values()) {
            if (t != TipoPersonalizacao.SEM_PERSONALIZACAO) {
                cbTipo.getItems().add(t);
            }
        }
        cbTipo.setValue(TipoPersonalizacao.SEM_PERSONALIZACAO);
        cbTipo.setMaxWidth(Double.MAX_VALUE);
        cbTipo.setStyle("-fx-font-size: 14px;");

        cbPagamento = new ComboBox<>();
        cbPagamento.getItems().addAll(StatusPagamento.values());
        cbPagamento.setPromptText("Selecione...");
        cbPagamento.setMaxWidth(Double.MAX_VALUE);
        cbPagamento.setStyle("-fx-font-size: 14px;");

        HBox linha2 = new HBox(12,
                criarCampo("🎨 Personalização *", cbTipo),
                criarCampo("💳 Pagamento *", cbPagamento)
        );

        dpConfirmacao = new DatePicker(LocalDate.now());
        dpConfirmacao.setMaxWidth(Double.MAX_VALUE);
        dpConfirmacao.setStyle("-fx-font-size: 14px;");

        dpEntrega = new DatePicker();
        dpEntrega.setMaxWidth(Double.MAX_VALUE);
        dpEntrega.setStyle("-fx-font-size: 14px;");

        HBox linha3 = new HBox(12,
                criarCampo("📅 Confirmação *", dpConfirmacao),
                criarCampo("📅 Entrega *", dpEntrega)
        );

        tfValor = criarTextField("Ex: 65.00", 120);

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
                criarCampoFixo("💰 Valor (R$)", tfValor, 160),
                campoResta
        );

        cbNF = new ComboBox<>();
        cbNF.getItems().addAll("Não precisa de NF", "Precisa de NF");
        cbNF.setValue("Não precisa de NF");
        cbNF.setMaxWidth(Double.MAX_VALUE);
        cbNF.setStyle("-fx-font-size: 14px;");

        HBox linha5 = new HBox(12,
                criarCampo("🧾 Nota Fiscal", cbNF)
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

        VBox uploadCard = new VBox(14);
        uploadCard.setPadding(new Insets(24));
        uploadCard.setPrefWidth(280);
        uploadCard.setMinWidth(280);
        uploadCard.setMaxWidth(280);
        uploadCard.setAlignment(Pos.TOP_CENTER);
        uploadCard.setStyle(
                "-fx-background-color: white; -fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);"
        );

        Label lblUploadTitulo = new Label("🖼 Layout");
        lblUploadTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label lblOpcional = new Label("(opcional)");
        lblOpcional.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        ImageView preview = new ImageView();
        preview.setFitWidth(220);
        preview.setFitHeight(200);
        preview.setPreserveRatio(true);

        Label lblArquivo = new Label("Nenhum arquivo selecionado");
        lblArquivo.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        lblArquivo.setWrapText(true);

        VBox dropArea = new VBox(10);
        dropArea.setAlignment(Pos.CENTER);
        dropArea.setPrefHeight(220);
        dropArea.setPadding(new Insets(10));
        dropArea.setStyle(
                "-fx-background-color: #f8f9fa; -fx-background-radius: 8; " +
                        "-fx-border-color: #dcdde1; -fx-border-radius: 8; -fx-border-style: dashed;"
        );

        Label lblDropIcon = new Label("📁");
        lblDropIcon.setStyle("-fx-font-size: 36px;");

        Label lblDropText = new Label("Clique para selecionar\nPNG, JPG ou PDF");
        lblDropText.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d; -fx-text-alignment: center;");
        lblDropText.setWrapText(true);

        dropArea.getChildren().addAll(lblDropIcon, lblDropText, preview, lblArquivo);

        Button btnSelecionar = new Button("📂 Selecionar arquivo");
        btnSelecionar.setPrefWidth(220);
        btnSelecionar.setPrefHeight(38);
        btnSelecionar.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-font-size: 13px;"
        );

        Button btnLimpar = new Button("✖ Remover");
        btnLimpar.setPrefWidth(220);
        btnLimpar.setPrefHeight(38);
        btnLimpar.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-font-size: 13px;"
        );
        btnLimpar.setVisible(false);
        btnLimpar.setManaged(false);

        btnSelecionar.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Selecionar Layout");
            fc.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Imagens e PDF", "*.png", "*.jpg", "*.jpeg", "*.pdf")
            );
            File arquivo = fc.showOpenDialog(root.getScene().getWindow());
            if (arquivo != null) {
                arquivoLayoutSelecionado = arquivo;
                lblArquivo.setText(arquivo.getName());
                lblArquivo.setStyle("-fx-font-size: 12px; -fx-text-fill: #27ae60;");
                btnLimpar.setVisible(true);
                btnLimpar.setManaged(true);

                String nome = arquivo.getName().toLowerCase();
                if (nome.endsWith(".png") || nome.endsWith(".jpg") || nome.endsWith(".jpeg")) {
                    preview.setImage(new Image(arquivo.toURI().toString()));
                    lblDropIcon.setText("🖼");
                } else {
                    preview.setImage(null);
                    lblDropIcon.setText("📄");
                }
            }
        });

        btnLimpar.setOnAction(e -> {
            arquivoLayoutSelecionado = null;
            lblArquivo.setText("Nenhum arquivo selecionado");
            lblArquivo.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
            preview.setImage(null);
            lblDropIcon.setText("📁");
            btnLimpar.setVisible(false);
            btnLimpar.setManaged(false);
        });

        uploadCard.getChildren().addAll(lblUploadTitulo, lblOpcional, dropArea, btnSelecionar, btnLimpar);

        HBox botaoBox = new HBox(10, btnSalvar, btnSalvarRascunho);
        botaoBox.setAlignment(Pos.CENTER_LEFT);

        btnSalvar.setOnAction(e -> {
            if (tfNome.getText().isBlank() || cbTipo.getValue() == null ||
                    dpEntrega.getValue() == null || cbPagamento.getValue() == null) {
                lblFeedback.setText("⚠ Preencha todos os campos obrigatórios (*).");
                lblFeedback.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px;");
                return;
            }

            Pedido p = new Pedido();
            p.setClienteNome(tfNome.getText().trim());
            p.setClienteTelefone(tfTelefone.getText().trim());
            p.setAnotacao(taAnotacao.getText().trim());
            p.setTipoPersonalizacao(cbTipo.getValue());
            p.setDataConfirmacao(dpConfirmacao.getValue());
            p.setDataEntrega(dpEntrega.getValue());
            p.setStatusPagamento(cbPagamento.getValue());
            p.setSituacao(SituacaoPedido.PREPARANDO);
            p.setPrecisaNf(cbNF.getValue().equals("Precisa de NF"));

            try {
                if (!tfValor.getText().isBlank())
                    p.setValor(new BigDecimal(tfValor.getText().replace(",", ".")));
            } catch (Exception ex) {
                lblFeedback.setText("⚠ Valor inválido. Use o formato: 65.00");
                lblFeedback.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px;");
                return;
            }

            try {
                if (cbPagamento.getValue() == StatusPagamento.PARCIAL && !tfResta.getText().isBlank())
                    p.setResta(new BigDecimal(tfResta.getText().replace(",", ".")));
            } catch (Exception ex) {
                lblFeedback.setText("⚠ Valor de 'Resta' inválido. Use o formato: 32.50");
                lblFeedback.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px;");
                return;
            }

            if (arquivoLayoutSelecionado != null) {
                try {
                    File pasta = new File(PASTA_LAYOUTS);
                    if (!pasta.exists()) pasta.mkdirs();

                    String ext = arquivoLayoutSelecionado.getName()
                            .substring(arquivoLayoutSelecionado.getName().lastIndexOf("."));
                    String nomeArquivo = "layout_" + System.currentTimeMillis() + ext;
                    Path destino = Paths.get(PASTA_LAYOUTS + nomeArquivo);
                    Files.copy(arquivoLayoutSelecionado.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
                    p.setLayoutPath(destino.toString());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            pedidoDAO.salvar(p);

            String[] tarefasAuto = {"Fazer layout", "Fazer arquivo", "Dobrar"};
            for (String desc : tarefasAuto) {
                Tarefa t = new Tarefa();
                t.setDescricao(desc);
                t.setPedido(p);
                t.setOrigem(OrigemTarefa.PEDIDO);
                t.setResponsavelDestino(Perfil.PRODUCAO);
                tarefaDAO.salvar(t);
            }

            limparFormulario();
            preview.setImage(null);
            lblDropIcon.setText("📁");
            lblArquivo.setText("Nenhum arquivo selecionado");
            lblArquivo.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
            btnLimpar.setVisible(false);
            btnLimpar.setManaged(false);

            lblFeedback.setText("✅ Pedido salvo com sucesso!");
            lblFeedback.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 14px;");
        });

        container.getChildren().addAll(formCard, uploadCard);

        formCard.getChildren().addAll(linha1, campoAnotacao, linha2, linha3, linha4, linha5);

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

        root.getChildren().addAll(titulo, scroll);

        carregarRascunhos();
    }

    private void carregarRascunhos() {
        listaRascunhos.getChildren().clear();
        List<RascunhoPedido> rascunhos = rascunhoDAO.listarTodos();

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
        boolean temConteudoAtual = !tfNome.getText().isBlank() || !taAnotacao.getText().isBlank();
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
        tfResta.clear();
        cbPagamento.setValue(null);
        cbNF.setValue("Não precisa de NF");
        campoResta.setVisible(false);
        campoResta.setManaged(false);
        arquivoLayoutSelecionado = null;
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
        return r;
    }

    public void salvarRascunhoAutomatico() {
        boolean temConteudo = !tfNome.getText().isBlank() || !taAnotacao.getText().isBlank();
        if (temConteudo) {
            rascunhoDAO.salvar(montarRascunhoAtual());
        }
    }

    public VBox getView() {

        return root;
    }
}