package com.inventystamp.view;

import com.inventystamp.dao.PedidoDAO;
import com.inventystamp.dao.TarefaDAO;
import com.inventystamp.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

public class AtendimentoView {

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final TarefaDAO tarefaDAO = new TarefaDAO();
    private VBox root;
    private File arquivoLayoutSelecionado = null;
    private static final String PASTA_LAYOUTS = System.getProperty("user.home") + "\\Desktop\\layouts-inventy\\";

    public AtendimentoView() {
        construir();
    }

    private void construir() {
        root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #e8eaed;");

        Label titulo = new Label("🎧 Novo Pedido");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Container central com formulário + upload lado a lado
        HBox container = new HBox(20);
        container.setAlignment(Pos.TOP_CENTER);

        // Card do formulário
        VBox formCard = new VBox(14);
        formCard.setPadding(new Insets(24));
        formCard.setPrefWidth(620);
        formCard.setMaxWidth(620);
        formCard.setStyle(
                "-fx-background-color: white; -fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);"
        );

        // Linha 1: Cliente e Telefone
        TextField tfNome = criarTextField("Nome do cliente", 200);
        TextField tfTelefone = criarTextField("(11) 99999-9999", 140);

        HBox linha1 = new HBox(12,
                criarCampo("👤 Cliente *", tfNome),
                criarCampoFixo("📞 Telefone", tfTelefone, 160)
        );

        // Anotação
        TextArea taAnotacao = new TextArea();
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

        // Linha 2: Tipo e Pagamento
        ComboBox<TipoPersonalizacao> cbTipo = new ComboBox<>();
        cbTipo.getItems().addAll(TipoPersonalizacao.values());
        cbTipo.setPromptText("Selecione...");
        cbTipo.setMaxWidth(Double.MAX_VALUE);
        cbTipo.setStyle("-fx-font-size: 14px;");

        ComboBox<StatusPagamento> cbPagamento = new ComboBox<>();
        cbPagamento.getItems().addAll(StatusPagamento.values());
        cbPagamento.setPromptText("Selecione...");
        cbPagamento.setMaxWidth(Double.MAX_VALUE);
        cbPagamento.setStyle("-fx-font-size: 14px;");

        HBox linha2 = new HBox(12,
                criarCampo("🎨 Personalização *", cbTipo),
                criarCampo("💳 Pagamento *", cbPagamento)
        );

        // Linha 3: Datas
        DatePicker dpConfirmacao = new DatePicker(LocalDate.now());
        dpConfirmacao.setMaxWidth(Double.MAX_VALUE);
        dpConfirmacao.setStyle("-fx-font-size: 14px;");

        DatePicker dpEntrega = new DatePicker();
        dpEntrega.setMaxWidth(Double.MAX_VALUE);
        dpEntrega.setStyle("-fx-font-size: 14px;");

        HBox linha3 = new HBox(12,
                criarCampo("📅 Confirmação *", dpConfirmacao),
                criarCampo("📅 Entrega *", dpEntrega)
        );

        // Linha 4: Valor e Resta
        TextField tfValor = criarTextField("Ex: 65.00", 120);

        TextField tfResta = criarTextField("Valor restante", 120);
        VBox campoResta = criarCampoFixo("⚠ Resta (R$)", tfResta, 160);
        campoResta.setVisible(false);
        campoResta.setManaged(false);

        cbPagamento.setOnAction(e -> {
            boolean isParcial = cbPagamento.getValue() == StatusPagamento.PARCIAL;
            campoResta.setVisible(isParcial);
            campoResta.setManaged(isParcial);
        });

        // NF
        ComboBox<String> cbNF = new ComboBox<>();
        cbNF.getItems().addAll("Não precisa de NF", "Precisa de NF");
        cbNF.setValue("Não precisa de NF");
        cbNF.setMaxWidth(Double.MAX_VALUE);
        cbNF.setStyle("-fx-font-size: 14px;");

        HBox linha4 = new HBox(12,
                criarCampoFixo("💰 Valor (R$)", tfValor, 160),
                campoResta
        );

        HBox linha5 = new HBox(12,
                criarCampo("🧾 Nota Fiscal", cbNF)
        );

        // Feedback e botão
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

        formCard.getChildren().addAll(linha1, campoAnotacao, linha2, linha3, linha4, linha5);

        // Card de upload do layout
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

        // Preview do arquivo
        ImageView preview = new ImageView();
        preview.setFitWidth(220);
        preview.setFitHeight(200);
        preview.setPreserveRatio(true);

        Label lblArquivo = new Label("Nenhum arquivo selecionado");
        lblArquivo.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        lblArquivo.setWrapText(true);

        // Área de drop visual
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

                // Mostra preview se for imagem
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

        // Botão salvar e feedback abaixo do container
        HBox botaoBox = new HBox(btnSalvar);
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

            // Salva o layout se houver
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

            // Limpa formulário
            tfNome.clear();
            tfTelefone.clear();
            taAnotacao.clear();
            cbTipo.setValue(null);
            dpConfirmacao.setValue(LocalDate.now());
            dpEntrega.setValue(null);
            tfValor.clear();
            tfResta.clear();
            cbPagamento.setValue(null);
            campoResta.setVisible(false);
            campoResta.setManaged(false);
            arquivoLayoutSelecionado = null;
            lblArquivo.setText("Nenhum arquivo selecionado");
            lblArquivo.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
            preview.setImage(null);
            lblDropIcon.setText("📁");
            btnLimpar.setVisible(false);
            btnLimpar.setManaged(false);

            lblFeedback.setText("✅ Pedido salvo com sucesso!");
            lblFeedback.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 14px;");
        });

        container.getChildren().addAll(formCard, uploadCard);

        VBox bottomBox = new VBox(10, botaoBox, lblFeedback);
        bottomBox.setMaxWidth(620 + 280 + 20);

        VBox tudo = new VBox(20, container, bottomBox);
        tudo.setAlignment(Pos.TOP_CENTER);

        ScrollPane scroll = new ScrollPane(tudo);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(titulo, scroll);
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

    public VBox getView() {
        return root;
    }
}