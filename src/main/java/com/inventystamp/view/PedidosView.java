package com.inventystamp.view;

import com.inventystamp.dao.PedidoDAO;
import com.inventystamp.dao.DemandaPedidoDAO;
import com.inventystamp.dao.EstatisticaDAO;
import com.inventystamp.model.*;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.inventystamp.dao.PecasPedidoDAO;

public class PedidosView {

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final DemandaPedidoDAO demandaPedidoDAO = new DemandaPedidoDAO();

    private VBox root;
    private StackPane conteudoPaginas;
    private GridPane gridPedidos;
    private GridPane gridDemandas;
    private Button btnAbaPedidos, btnAbaDemandas;
    private Label lblContadorDtf;
    private Label lblContadorDemandas;

    private String textoPesquisa = "";
    private String textoPesquisaDemanda = "";
    private final java.util.Set<TipoPersonalizacao> filtrosSelecionados = new java.util.HashSet<>();
    private final java.util.Set<String> filtrosDemandaSelecionados = new java.util.HashSet<>();

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PedidosView() {
        construir();
    }

    private void construir() {
        root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #e8eaed;");

        btnAbaPedidos = new Button("📋 Pedidos");
        btnAbaDemandas = new Button("📢 Demandas");

        lblContadorDemandas = new Label();
        lblContadorDemandas.setStyle(
                "-fx-font-size: 11px; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-color: #e67e22; -fx-background-radius: 20; -fx-padding: 2 8 2 8;"
        );
        lblContadorDemandas.setVisible(false);
        lblContadorDemandas.setManaged(false);

        HBox abaDemandasBox = new HBox(6, btnAbaDemandas, lblContadorDemandas);
        abaDemandasBox.setAlignment(Pos.CENTER_LEFT);

        HBox abas = new HBox(10, btnAbaPedidos, abaDemandasBox);
        abas.setAlignment(Pos.CENTER_LEFT);

        conteudoPaginas = new StackPane();

        VBox paginaPedidos = construirPaginaPedidos();
        VBox paginaDemandas = construirPaginaDemandas();

        conteudoPaginas.getChildren().addAll(paginaPedidos, paginaDemandas);
        paginaDemandas.setVisible(false);
        paginaDemandas.setManaged(false);

        btnAbaPedidos.setOnAction(e -> {
            paginaPedidos.setVisible(true);
            paginaPedidos.setManaged(true);
            paginaDemandas.setVisible(false);
            paginaDemandas.setManaged(false);
            atualizarEstiloAbas(true);
        });

        btnAbaDemandas.setOnAction(e -> {
            paginaPedidos.setVisible(false);
            paginaPedidos.setManaged(false);
            paginaDemandas.setVisible(true);
            paginaDemandas.setManaged(true);
            atualizarEstiloAbas(false);
            carregarDemandas();
        });

        atualizarEstiloAbas(true);

        VBox.setVgrow(conteudoPaginas, Priority.ALWAYS);
        root.getChildren().addAll(abas, conteudoPaginas);

        carregarPedidos();
        carregarDemandas();

        javafx.animation.Timeline autoRefresh = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.minutes(1), e -> {
                    carregarPedidos();
                    carregarDemandas();
                })
        );
        autoRefresh.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        autoRefresh.play();
    }

    private void atualizarEstiloAbas(boolean pedidosAtiva) {
        String ativo = "-fx-background-color: #2980b9; -fx-text-fill: white; -fx-background-radius: 8 8 0 0; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 8 20 8 20;";
        String inativo = "-fx-background-color: #bdc3c7; -fx-text-fill: #2c3e50; -fx-background-radius: 8 8 0 0; " +
                "-fx-font-size: 14px; -fx-padding: 8 20 8 20;";
        btnAbaPedidos.setStyle(pedidosAtiva ? ativo : inativo);
        btnAbaDemandas.setStyle(pedidosAtiva ? inativo : ativo);
    }

    // ============================================================
    // PÁGINA 1 — PEDIDOS
    // ============================================================

    private VBox construirPaginaPedidos() {
        VBox pagina = new VBox(15);

        Label titulo = new Label("📋 Pedidos");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        lblContadorDtf = new Label();
        lblContadorDtf.setStyle(
                "-fx-font-size: 13px; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-background-color: #e67e22; -fx-background-radius: 20; -fx-padding: 6 14 6 14;"
        );
        lblContadorDtf.setVisible(false);
        lblContadorDtf.setManaged(false);

        HBox cabecalhoPedidos = new HBox(14, titulo, lblContadorDtf);
        cabecalhoPedidos.setAlignment(Pos.CENTER_LEFT);

        Label lblFiltrarTitulo = new Label("Filtrar:");
        lblFiltrarTitulo.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white;");

        Button btnFiltroTodos = new Button("Todos");
        btnFiltroTodos.setStyle(estiloChip(false));

        java.util.List<ToggleButton> chipsTipo = new java.util.ArrayList<>();
        for (TipoPersonalizacao tipo : TipoPersonalizacao.values()) {
            ToggleButton chip = criarChipFiltro(tipo.name());
            chipsTipo.add(chip);
        }

        for (ToggleButton chip : chipsTipo) {
            chip.setOnAction(e -> {
                TipoPersonalizacao tipoDoChip = TipoPersonalizacao.valueOf(chip.getText());
                if (chip.isSelected()) filtrosSelecionados.add(tipoDoChip);
                else filtrosSelecionados.remove(tipoDoChip);

                boolean nenhumEspecificoSelecionado = filtrosSelecionados.isEmpty();
                btnFiltroTodos.setStyle(estiloChip(nenhumEspecificoSelecionado));

                carregarPedidos();
            });
        }

        btnFiltroTodos.setOnAction(e -> {
            filtrosSelecionados.clear();
            for (ToggleButton chip : chipsTipo) {
                chip.setSelected(false);
            }
            btnFiltroTodos.setStyle(estiloChip(true));
            carregarPedidos();
        });

        FlowPane filtros = new FlowPane(8, 8);
        filtros.setAlignment(Pos.CENTER_LEFT);
        filtros.getChildren().add(lblFiltrarTitulo);
        filtros.getChildren().add(btnFiltroTodos);
        filtros.getChildren().addAll(chipsTipo);

        VBox compartimentoFiltros = new VBox(filtros);
        compartimentoFiltros.setPadding(new Insets(14));
        compartimentoFiltros.setStyle("-fx-background-color: #2980b9; -fx-background-radius: 10;");

        TextField tfPesquisa = new TextField();
        tfPesquisa.setPromptText("🔍 Pesquisar por nome, código, data ou anotação...");
        tfPesquisa.setPrefWidth(320);
        tfPesquisa.setStyle(
                "-fx-font-size: 13px; -fx-background-radius: 6; " +
                        "-fx-border-color: #dcdde1; -fx-border-radius: 6;"
        );
        tfPesquisa.textProperty().addListener((obs, oldVal, newVal) -> {
            textoPesquisa = newVal.toLowerCase().trim();
            carregarPedidos();
        });

        VBox linhaFiltrosEPesquisa = new VBox(10, compartimentoFiltros, tfPesquisa);

        gridPedidos = new GridPane();
        gridPedidos.setHgap(12);
        gridPedidos.setVgap(12);
        gridPedidos.setPadding(new Insets(4));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        col1.setFillWidth(true);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        col2.setFillWidth(true);
        gridPedidos.getColumnConstraints().addAll(col1, col2);

        ScrollPane scroll = new ScrollPane(gridPedidos);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        pagina.getChildren().addAll(cabecalhoPedidos, linhaFiltrosEPesquisa, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        return pagina;
    }

    private void carregarPedidos() {
        gridPedidos.getChildren().clear();

        List<Pedido> todosPedidosAtivos = pedidoDAO.listarTodos().stream()
                .filter(p -> p.getSituacao() != SituacaoPedido.PRONTO)
                .toList();

        long dtfsPendentes = todosPedidosAtivos.stream()
                .filter(p -> p.getTipoPersonalizacao() == TipoPersonalizacao.DTF && !p.isDtfMontado())
                .count();

        if (dtfsPendentes > 0) {
            lblContadorDtf.setText("🔶 " + dtfsPendentes + " DTF(s) para montar");
            lblContadorDtf.setVisible(true);
            lblContadorDtf.setManaged(true);
        } else {
            lblContadorDtf.setVisible(false);
            lblContadorDtf.setManaged(false);
        }

        List<Pedido> pedidos = todosPedidosAtivos.stream()
                .filter(p -> filtrosSelecionados.isEmpty() || filtrosSelecionados.contains(p.getTipoPersonalizacao()))
                .filter(p -> {
                    if (textoPesquisa.isBlank()) return true;
                    String nome = p.getClienteNome() != null ? p.getClienteNome().toLowerCase() : "";
                    String codigo = extrairCodigo(p.getClienteTelefone()).toLowerCase();
                    String telefone = p.getClienteTelefone() != null ? p.getClienteTelefone().toLowerCase() : "";
                    String anotacao = p.getAnotacao() != null ? p.getAnotacao().toLowerCase() : "";
                    String dataEntrega = p.getDataEntrega() != null ? p.getDataEntrega().format(fmt).toLowerCase() : "";
                    String dataConfirmacao = p.getDataConfirmacao() != null ? p.getDataConfirmacao().format(fmt).toLowerCase() : "";
                    String atendente = p.getAtendente() != null ? p.getAtendente().toLowerCase() : "";
                    String nf = p.isPrecisaNf() ? "nf nota fiscal" : "";
                    String cupomFiscal = p.isPrecisaCupomFiscal() ? "cupom fiscal" : "";
                    return nome.contains(textoPesquisa) || codigo.contains(textoPesquisa)
                            || telefone.contains(textoPesquisa) || anotacao.contains(textoPesquisa)
                            || dataEntrega.contains(textoPesquisa) || dataConfirmacao.contains(textoPesquisa)
                            || atendente.contains(textoPesquisa) || nf.contains(textoPesquisa) || cupomFiscal.contains(textoPesquisa);
                })
                .sorted((a, b) -> a.getDataEntrega().compareTo(b.getDataEntrega()))
                .toList();

        if (pedidos.isEmpty()) {
            Label vazio = new Label("Nenhum pedido encontrado.");
            vazio.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");
            gridPedidos.add(vazio, 0, 0, 2, 1);
            return;
        }

        gridPedidos.getRowConstraints().clear();
        for (int i = 0; i < pedidos.size(); i++) {
            VBox card = criarCardPedido(pedidos.get(i));
            card.setMinHeight(220);
            GridPane.setValignment(card, javafx.geometry.VPos.TOP);
            gridPedidos.add(card, i % 2, i / 2);
        }
        for (int row = 0; row <= pedidos.size() / 2; row++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.NEVER);
            rc.setFillHeight(false);
            gridPedidos.getRowConstraints().add(rc);
        }
    }

    private VBox criarCardPedido(Pedido p) {
        VBox card = new VBox();
        card.setPadding(new Insets(14));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMaxHeight(Double.MAX_VALUE);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2);"
        );

        HBox barra = new HBox();
        barra.setPrefHeight(6);
        barra.setMaxWidth(Double.MAX_VALUE);
        barra.setStyle("-fx-background-color: " + getCorPrazo(p.getDataEntrega()) + "; -fx-background-radius: 4;");

        String codigo = extrairCodigo(p.getClienteTelefone());
        Label lblNome = new Label(p.getClienteNome() + (codigo.isEmpty() ? "" : " " + codigo));
        lblNome.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        lblNome.setWrapText(true);

        StackPane iconeSinalDtf = new StackPane();
        iconeSinalDtf.setPrefSize(22, 20);
        iconeSinalDtf.setStyle("-fx-cursor: hand;");

        javafx.scene.shape.Polygon trianguloDtf = new javafx.scene.shape.Polygon();
        trianguloDtf.getPoints().addAll(11.0, 0.0, 22.0, 20.0, 0.0, 20.0);
        trianguloDtf.setFill(javafx.scene.paint.Color.web("#e67e22"));

        Label exclamacaoDtf = new Label("!");
        exclamacaoDtf.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-font-family: 'Arial';");
        exclamacaoDtf.setTranslateY(2);

        iconeSinalDtf.getChildren().addAll(trianguloDtf, exclamacaoDtf);

        boolean mostrarSinalDtf = p.getTipoPersonalizacao() == TipoPersonalizacao.DTF && !p.isDtfMontado();
        iconeSinalDtf.setVisible(mostrarSinalDtf);
        iconeSinalDtf.setManaged(mostrarSinalDtf);

        iconeSinalDtf.setOnMouseClicked(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "O DTF foi montado?", ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText(null);
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES) {
                    p.setDtfMontado(true);
                    pedidoDAO.atualizar(p);
                    iconeSinalDtf.setVisible(false);
                    iconeSinalDtf.setManaged(false);
                    carregarPedidos();
                }
            });
        });

        Label lblTipo = new Label(p.getTipoPersonalizacao().name());
        lblTipo.setStyle(
                "-fx-background-color: #ecf0f1; -fx-padding: 2 8 2 8; " +
                        "-fx-background-radius: 10; -fx-font-size: 13px; -fx-text-fill: #7f8c8d;"
        );

        Label lblEntrega = new Label("📅 Entrega: " + p.getDataEntrega().format(fmt));
        lblEntrega.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        lblEntrega.setWrapText(true);

        VBox infoValor = new VBox(2);
        if (p.isValorACombinar()) {
            Label lblValor = new Label("💰 Valor a combinar");
            lblValor.setStyle("-fx-font-size: 14px; -fx-text-fill: #e67e22; -fx-font-weight: bold;");
            infoValor.getChildren().add(lblValor);
        } else if (p.getValor() != null) {
            Label lblValor = new Label("💰 Valor: R$ " + p.getValor().setScale(2, java.math.RoundingMode.HALF_UP));
            lblValor.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
            infoValor.getChildren().add(lblValor);
        }
        if (p.getResta() != null) {
            Label lblResta = new Label("⚠ Resta: R$ " + p.getResta().setScale(2, java.math.RoundingMode.HALF_UP));
            lblResta.setStyle("-fx-font-size: 14px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            infoValor.getChildren().add(lblResta);
        }

        Label lblPagamento = new Label("💳 " + p.getStatusPagamento().name());
        lblPagamento.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");

        Label lblNF = new Label(p.isPrecisaNf() ? "🧾 Precisa de NF" : "");
        lblNF.setStyle("-fx-font-size: 13px; -fx-text-fill: #e67e22; -fx-font-weight: bold;");

        Label lblCupomFiscal = new Label(p.isPrecisaCupomFiscal() ? "🧾 Precisa de cupom fiscal" : "");
        lblCupomFiscal.setStyle("-fx-font-size: 13px; -fx-text-fill: #e67e22; -fx-font-weight: bold;");

        Label lblTelefone = new Label(p.getClienteTelefone() != null && !p.getClienteTelefone().isBlank() ? "📞 " + p.getClienteTelefone() : "");
        lblTelefone.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");

        Label lblAtendente = new Label(p.getAtendente() != null ? "👩‍💼 Atendente: " + p.getAtendente() : "");
        lblAtendente.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");

        Label lblAnotacao = new Label(p.getAnotacao() != null ? p.getAnotacao() : "");
        lblAnotacao.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
        lblAnotacao.setWrapText(true);

        HBox acoes = new HBox(8);
        acoes.setAlignment(Pos.CENTER_RIGHT);

        Button btnConcluir = new Button("✅ Concluir");
        btnConcluir.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 13px;");

        Button btnEditar = new Button("✏ Editar");
        btnEditar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 13px;");

        Button btnRemover = new Button("🗑 Cancelar");
        btnRemover.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 13px;");

        Button btnImprimir = new Button("🖨 Imprimir");
        btnImprimir.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 13px;");
        btnImprimir.setOnAction(e -> imprimirEtiqueta(p));

        Button btnCupomFiscal = new Button("🧾 Cupom Fiscal");
        btnCupomFiscal.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 13px;");
        btnCupomFiscal.setOnAction(e -> gerarECopiarCupomFiscal(p));

        Button btnWhatsApp = new Button("💬");
        btnWhatsApp.setTooltip(new Tooltip("Abrir WhatsApp do cliente"));
        btnWhatsApp.setStyle("-fx-background-color: #25D366; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 13px;");
        btnWhatsApp.setOnAction(e -> abrirWhatsAppCliente(p));

        acoes.getChildren().addAll(btnWhatsApp, btnCupomFiscal, btnImprimir, btnConcluir, btnEditar, btnRemover);

        btnConcluir.setOnAction(e -> {
            if (p.isValorACombinar() || p.getValor() == null || p.getValor().compareTo(BigDecimal.ZERO) == 0) {
                mostrarAlerta("⚠ Informe o valor do pedido antes de concluir.");
                return;
            }

            TextInputDialog dialogPecas = new TextInputDialog();
            dialogPecas.setTitle("Quantidade de Peças");
            dialogPecas.setHeaderText("Quantas peças (exato ou aproximado) tinha este pedido?");
            dialogPecas.setContentText("Quantidade:");

            java.util.Optional<String> resultadoPecas = dialogPecas.showAndWait();
            if (resultadoPecas.isEmpty() || resultadoPecas.get().isBlank()) {
                mostrarAlerta("⚠ Informe a quantidade de peças para concluir o pedido.");
                return;
            }

            int quantidadePecas;
            try {
                quantidadePecas = Integer.parseInt(resultadoPecas.get().trim());
            } catch (NumberFormatException ex) {
                mostrarAlerta("⚠ Digite um número válido de peças.");
                return;
            }

            if (quantidadePecas < 1) {
                mostrarAlerta("⚠ A quantidade de peças deve ser pelo menos 1.");
                return;
            }

            Alert dialogOrigem = new Alert(Alert.AlertType.CONFIRMATION);
            dialogOrigem.setTitle("Origem das Peças");
            dialogOrigem.setHeaderText("As peças eram nossas, do cliente, ou ambos?");
            ButtonType btnNossas = new ButtonType("Nossas");
            ButtonType btnCliente = new ButtonType("Do Cliente");
            ButtonType btnAmbos = new ButtonType("Ambos");
            dialogOrigem.getButtonTypes().setAll(btnNossas, btnCliente, btnAmbos);

            java.util.Optional<ButtonType> resultadoOrigem = dialogOrigem.showAndWait();
            if (resultadoOrigem.isEmpty()) {
                mostrarAlerta("⚠ Informe a origem das peças para concluir o pedido.");
                return;
            }
            String origemPecas = resultadoOrigem.get().getText();

            p.setSituacao(SituacaoPedido.PRONTO);
            pedidoDAO.atualizar(p);

            PecasPedidoDAO pecasDAO = new PecasPedidoDAO();
            pecasDAO.salvar(java.time.LocalDate.now().getYear(), java.time.LocalDate.now().getMonthValue(), quantidadePecas);

            if (p.getValor() != null) {
                EstatisticaDAO estatisticaDAO = new EstatisticaDAO();
                EstatisticasMes est = new EstatisticasMes();
                est.setAno(java.time.LocalDate.now().getYear());
                est.setMes(java.time.LocalDate.now().getMonthValue());
                est.setLucroBruto(p.getValor());
                est.setLucroBordado(BigDecimal.ZERO);
                est.setLucroDtf(BigDecimal.ZERO);
                est.setLucroSublimacao(BigDecimal.ZERO);
                est.setLucroSilkscreen(BigDecimal.ZERO);
                est.setLucroBrindes(BigDecimal.ZERO);

                switch (p.getTipoPersonalizacao()) {
                    case BORDADO -> est.setLucroBordado(p.getValor());
                    case DTF -> est.setLucroDtf(p.getValor());
                    case SUBLIMACAO -> est.setLucroSublimacao(p.getValor());
                    case SILKSCREEN -> est.setLucroSilkscreen(p.getValor());
                    case BRINDES -> est.setLucroBrindes(p.getValor());
                }

                switch (origemPecas) {
                    case "Nossas" -> est.setQtdPedidosPecasNossas(1);
                    case "Do Cliente" -> est.setQtdPedidosPecasCliente(1);
                    case "Ambos" -> est.setQtdPedidosPecasAmbos(1);
                }

                estatisticaDAO.salvarOuAtualizar(est);
            }

            card.setStyle(
                    "-fx-background-color: #d5f5e3; -fx-background-radius: 8; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2);"
            );
            lblNome.setText("✅ " + lblNome.getText());

            FadeTransition fade = new FadeTransition(Duration.millis(1500), card);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setDelay(Duration.millis(800));
            fade.setOnFinished(ev -> carregarPedidos());
            fade.play();
        });

        btnEditar.setOnAction(e -> abrirEdicao(p));

        btnRemover.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Cancelar pedido de " + p.getClienteNome() + "?");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    pedidoDAO.deletar(p.getId());
                    carregarPedidos();
                }
            });
        });

        HBox linhaNome = new HBox(8, lblNome, iconeSinalDtf);
        linhaNome.setAlignment(Pos.CENTER_LEFT);

        VBox colunaEsquerda = new VBox(10, linhaNome, lblTipo, lblTelefone, lblEntrega, infoValor, lblPagamento, lblNF, lblCupomFiscal, lblAtendente);
        colunaEsquerda.setPrefWidth(200);
        HBox.setHgrow(colunaEsquerda, Priority.ALWAYS);

        VBox colunaDireita = new VBox(6);
        colunaDireita.setPrefWidth(180);
        colunaDireita.setMinWidth(180);

        Label lblTituloAnotacao = new Label("📝 Anotações");
        lblTituloAnotacao.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #7f8c8d;");

        lblAnotacao.setMaxWidth(180);

        colunaDireita.getChildren().addAll(lblTituloAnotacao, lblAnotacao);

        HBox conteudo = new HBox(14, colunaEsquerda, colunaDireita);
        conteudo.setAlignment(Pos.TOP_LEFT);

        VBox topo = new VBox(10, barra, conteudo);
        VBox.setVgrow(topo, Priority.ALWAYS);

        card.getChildren().addAll(topo, acoes);
        return card;
    }

    private void abrirEdicao(Pedido p) {
        Dialog<Pedido> dialog = new Dialog<>();
        dialog.setTitle("Editar Pedido");

        TextField tfNome = new TextField(p.getClienteNome());
        TextField tfTelefone = new TextField(p.getClienteTelefone());
        TextArea taAnotacao = new TextArea(p.getAnotacao());
        taAnotacao.setPrefRowCount(3);
        DatePicker dpEntrega = new DatePicker(p.getDataEntrega());

        ComboBox<TipoPersonalizacao> cbTipo = new ComboBox<>();
        cbTipo.getItems().addAll(TipoPersonalizacao.values());
        cbTipo.setValue(p.getTipoPersonalizacao());

        ComboBox<StatusPagamento> cbPagamento = new ComboBox<>();
        cbPagamento.getItems().addAll(StatusPagamento.values());
        cbPagamento.setValue(p.getStatusPagamento());
        cbPagamento.setDisable(p.isValorACombinar());

        TextField tfValor = new TextField(p.getValor() != null ? p.getValor().toString() : "");

        ToggleButton tbACombinarEdicao = new ToggleButton("A combinar");
        tbACombinarEdicao.setSelected(p.isValorACombinar());
        tfValor.setDisable(p.isValorACombinar());
        tbACombinarEdicao.setStyle(p.isValorACombinar()
                ? "-fx-font-size: 12px; -fx-background-radius: 6; -fx-background-color: #8e44ad; -fx-text-fill: white;"
                : "-fx-font-size: 12px; -fx-background-radius: 6; -fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50;"
        );

        tbACombinarEdicao.selectedProperty().addListener((obs, oldVal, novoValor) -> {
            tfValor.setDisable(novoValor);
            if (novoValor) tfValor.clear();
            tbACombinarEdicao.setStyle(novoValor
                    ? "-fx-font-size: 12px; -fx-background-radius: 6; -fx-background-color: #8e44ad; -fx-text-fill: white;"
                    : "-fx-font-size: 12px; -fx-background-radius: 6; -fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50;"
            );
            if (novoValor) cbPagamento.setValue(StatusPagamento.PAGAR_NA_RETIRADA);
            cbPagamento.setDisable(novoValor);
        });

        TextField tfResta = new TextField(p.getResta() != null ? p.getResta().toString() : "");
        tfResta.setVisible(p.getStatusPagamento() == StatusPagamento.PARCIAL);
        tfResta.setManaged(p.getStatusPagamento() == StatusPagamento.PARCIAL);
        Label lblResta = new Label("Resta (R$)");
        lblResta.setVisible(p.getStatusPagamento() == StatusPagamento.PARCIAL);
        lblResta.setManaged(p.getStatusPagamento() == StatusPagamento.PARCIAL);

        cbPagamento.setOnAction(e -> {
            boolean isParcial = cbPagamento.getValue() == StatusPagamento.PARCIAL;
            tfResta.setVisible(isParcial);
            tfResta.setManaged(isParcial);
            lblResta.setVisible(isParcial);
            lblResta.setManaged(isParcial);
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        grid.add(new Label("Cliente:"), 0, 0); grid.add(tfNome, 1, 0);
        grid.add(new Label("Telefone:"), 0, 1); grid.add(tfTelefone, 1, 1);
        grid.add(new Label("Anotação:"), 0, 2); grid.add(taAnotacao, 1, 2);
        grid.add(new Label("Tipo:"), 0, 3); grid.add(cbTipo, 1, 3);
        grid.add(new Label("Entrega:"), 0, 4); grid.add(dpEntrega, 1, 4);
        grid.add(new Label("Valor:"), 0, 5);
        HBox linhaValorEdicao = new HBox(6, tfValor, tbACombinarEdicao);
        grid.add(linhaValorEdicao, 1, 5);
        grid.add(new Label("Pagamento:"), 0, 6); grid.add(cbPagamento, 1, 6);
        grid.add(lblResta, 0, 7); grid.add(tfResta, 1, 7);

        ComboBox<String> cbNF = new ComboBox<>();
        cbNF.getItems().addAll("Não precisa de NF", "Precisa de NF");
        cbNF.setValue(p.isPrecisaNf() ? "Precisa de NF" : "Não precisa de NF");
        grid.add(new Label("🧾 Nota Fiscal:"), 0, 8); grid.add(cbNF, 1, 8);

        ComboBox<String> cbCupomFiscal = new ComboBox<>();
        cbCupomFiscal.getItems().addAll("Não precisa de cupom fiscal", "Precisa de cupom fiscal");
        cbCupomFiscal.setValue(p.isPrecisaCupomFiscal() ? "Precisa de cupom fiscal" : "Não precisa de cupom fiscal");
        grid.add(new Label("🧾 Cupom Fiscal:"), 0, 9); grid.add(cbCupomFiscal, 1, 9);

        ComboBox<String> cbAtendenteEdicao = new ComboBox<>();
        cbAtendenteEdicao.getItems().addAll("Kennia", "Pedro", "Bia", "Marco");
        cbAtendenteEdicao.setValue(p.getAtendente());
        grid.add(new Label("👩‍💼 Atendente:"), 0, 10); grid.add(cbAtendenteEdicao, 1, 10);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                p.setClienteNome(tfNome.getText());
                p.setClienteTelefone(tfTelefone.getText());
                p.setAnotacao(taAnotacao.getText());
                p.setTipoPersonalizacao(cbTipo.getValue());
                p.setDataEntrega(dpEntrega.getValue());
                p.setStatusPagamento(cbPagamento.getValue());
                p.setValorACombinar(tbACombinarEdicao.isSelected());
                if (!tbACombinarEdicao.isSelected()) {
                    try { p.setValor(new java.math.BigDecimal(tfValor.getText())); } catch (Exception ex) {}
                } else {
                    p.setValor(null);
                }
                try {
                    if (!tfResta.getText().isBlank())
                        p.setResta(new java.math.BigDecimal(tfResta.getText()));
                } catch (Exception ex) {}
                p.setPrecisaNf(cbNF.getValue().equals("Precisa de NF"));
                p.setPrecisaCupomFiscal(cbCupomFiscal.getValue().equals("Precisa de Cupom Fiscal"));
                p.setAtendente(cbAtendenteEdicao.getValue());
                return p;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(pedidoAtualizado -> {
            pedidoDAO.atualizar(pedidoAtualizado);
            carregarPedidos();
        });
    }

    // ============================================================
    // PÁGINA 2 — DEMANDAS DE PEDIDOS
    // ============================================================

    private VBox construirPaginaDemandas() {
        VBox pagina = new VBox(15);

        Label titulo = new Label("📢 Demandas de Pedidos");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Button btnNovaDemanda = new Button("➕ Nova Demanda");
        btnNovaDemanda.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-size: 13px;");
        btnNovaDemanda.setOnAction(e -> abrirBuscaPedidoParaDemanda());

        HBox cabecalho = new HBox(14, titulo, btnNovaDemanda);
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        Button btnFiltroTodosDemanda = new Button("Todos");
        btnFiltroTodosDemanda.setStyle(estiloChip(false));

        ToggleButton chipMarco = criarChipFiltro("MARCO");
        ToggleButton chipSimone = criarChipFiltro("SIMONE");

        chipMarco.setOnAction(e -> {
            if (chipMarco.isSelected()) filtrosDemandaSelecionados.add("MARCO");
            else filtrosDemandaSelecionados.remove("MARCO");
            btnFiltroTodosDemanda.setStyle(estiloChip(filtrosDemandaSelecionados.isEmpty()));
            carregarDemandas();
        });
        chipSimone.setOnAction(e -> {
            if (chipSimone.isSelected()) filtrosDemandaSelecionados.add("SIMONE");
            else filtrosDemandaSelecionados.remove("SIMONE");
            btnFiltroTodosDemanda.setStyle(estiloChip(filtrosDemandaSelecionados.isEmpty()));
            carregarDemandas();
        });
        btnFiltroTodosDemanda.setOnAction(e -> {
            filtrosDemandaSelecionados.clear();
            chipMarco.setSelected(false);
            chipSimone.setSelected(false);
            btnFiltroTodosDemanda.setStyle(estiloChip(true));
            carregarDemandas();
        });

        Label lblFiltrarTitulo = new Label("Filtrar:");
        lblFiltrarTitulo.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white;");

        FlowPane filtros = new FlowPane(8, 8);
        filtros.setAlignment(Pos.CENTER_LEFT);
        filtros.getChildren().addAll(lblFiltrarTitulo, btnFiltroTodosDemanda, chipMarco, chipSimone);

        VBox compartimentoFiltros = new VBox(filtros);
        compartimentoFiltros.setPadding(new Insets(14));
        compartimentoFiltros.setStyle("-fx-background-color: #2980b9; -fx-background-radius: 10;");

        TextField tfPesquisaDemanda = new TextField();
        tfPesquisaDemanda.setPromptText("🔍 Pesquisar por nome, código ou data do pedido...");
        tfPesquisaDemanda.setPrefWidth(320);
        tfPesquisaDemanda.setStyle(
                "-fx-font-size: 13px; -fx-background-radius: 6; " +
                        "-fx-border-color: #dcdde1; -fx-border-radius: 6;"
        );
        tfPesquisaDemanda.textProperty().addListener((obs, oldVal, newVal) -> {
            textoPesquisaDemanda = newVal.toLowerCase().trim();
            carregarDemandas();
        });

        VBox linhaFiltrosEPesquisa = new VBox(10, compartimentoFiltros, tfPesquisaDemanda);

        gridDemandas = new GridPane();
        gridDemandas.setHgap(12);
        gridDemandas.setVgap(12);
        gridDemandas.setPadding(new Insets(4));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        col1.setFillWidth(true);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        col2.setFillWidth(true);
        gridDemandas.getColumnConstraints().addAll(col1, col2);

        ScrollPane scroll = new ScrollPane(gridDemandas);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        pagina.getChildren().addAll(cabecalho, linhaFiltrosEPesquisa, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        return pagina;
    }

    private void carregarDemandas() {
        if (gridDemandas == null) return;
        gridDemandas.getChildren().clear();

        List<DemandaPedido> todasDemandas = demandaPedidoDAO.listarTodas();

        if (lblContadorDemandas != null) {
            int total = todasDemandas.size();
            if (total > 0) {
                lblContadorDemandas.setText(String.valueOf(total));
                lblContadorDemandas.setVisible(true);
                lblContadorDemandas.setManaged(true);
            } else {
                lblContadorDemandas.setVisible(false);
                lblContadorDemandas.setManaged(false);
            }
        }

        List<DemandaPedido> demandas = todasDemandas.stream()
                .filter(d -> filtrosDemandaSelecionados.isEmpty() || filtrosDemandaSelecionados.contains(d.getDestinatario()))
                .filter(d -> {
                    if (textoPesquisaDemanda.isBlank()) return true;
                    String nome = d.getPedidoClienteNome() != null ? d.getPedidoClienteNome().toLowerCase() : "";
                    String codigo = extrairCodigo(d.getPedidoClienteTelefone()).toLowerCase();
                    String data = d.getPedidoDataEntrega() != null ? d.getPedidoDataEntrega().format(fmt).toLowerCase() : "";
                    return nome.contains(textoPesquisaDemanda) || codigo.contains(textoPesquisaDemanda) || data.contains(textoPesquisaDemanda);
                })
                .toList();

        if (demandas.isEmpty()) {
            Label vazio = new Label("Nenhuma demanda encontrada.");
            vazio.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");
            gridDemandas.add(vazio, 0, 0, 2, 1);
            return;
        }

        gridDemandas.getRowConstraints().clear();
        for (int i = 0; i < demandas.size(); i++) {
            VBox card = criarCardDemanda(demandas.get(i));
            card.setMinHeight(160);
            GridPane.setValignment(card, javafx.geometry.VPos.TOP);
            gridDemandas.add(card, i % 2, i / 2);
        }
        for (int row = 0; row <= demandas.size() / 2; row++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.NEVER);
            rc.setFillHeight(false);
            gridDemandas.getRowConstraints().add(rc);
        }
    }

    private VBox criarCardDemanda(DemandaPedido d) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(14));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 2);"
        );

        HBox barra = new HBox();
        barra.setPrefHeight(6);
        barra.setMaxWidth(Double.MAX_VALUE);
        barra.setStyle("-fx-background-color: " + getCorPrazo(d.getPedidoDataEntrega()) + "; -fx-background-radius: 4;");

        String codigo = extrairCodigo(d.getPedidoClienteTelefone());
        Label lblNome = new Label(d.getPedidoClienteNome() + (codigo.isEmpty() ? "" : " " + codigo));
        lblNome.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        lblNome.setWrapText(true);

        Label lblDestinatario = new Label("→ " + d.getDestinatario());
        lblDestinatario.setStyle(
                "-fx-background-color: #ecf0f1; -fx-padding: 2 8 2 8; " +
                        "-fx-background-radius: 10; -fx-font-size: 12px; -fx-text-fill: #7f8c8d;"
        );

        HBox cabecalho = new HBox(10, lblNome, lblDestinatario);
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        Label lblTexto = new Label(d.getTexto());
        lblTexto.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
        lblTexto.setWrapText(true);

        HBox acoes = new HBox(8);
        acoes.setAlignment(Pos.CENTER_RIGHT);

        Button btnEditar = new Button("✏ Editar");
        btnEditar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 12px;");
        btnEditar.setOnAction(e -> abrirEdicaoDemanda(d));

        Button btnRemover = new Button("🗑 Cancelar");
        btnRemover.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-size: 13px;");
        btnRemover.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Cancelar esta demanda?");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    demandaPedidoDAO.deletar(d.getId());
                    carregarDemandas();
                }
            });
        });

        acoes.getChildren().addAll(btnEditar, btnRemover);

        card.getChildren().addAll(barra, cabecalho, lblTexto, acoes);
        return card;
    }

    private void abrirEdicaoDemanda(DemandaPedido d) {
        Dialog<DemandaPedido> dialog = new Dialog<>();
        dialog.setTitle("Editar Demanda");

        TextArea taTexto = new TextArea(d.getTexto());
        taTexto.setPrefRowCount(4);

        ComboBox<String> cbDestinatario = new ComboBox<>();
        cbDestinatario.getItems().addAll("MARCO", "SIMONE");
        cbDestinatario.setValue(d.getDestinatario());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Texto:"), 0, 0); grid.add(taTexto, 1, 0);
        grid.add(new Label("Destinatário:"), 0, 1); grid.add(cbDestinatario, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                d.setTexto(taTexto.getText().trim());
                d.setDestinatario(cbDestinatario.getValue());
                return d;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(demandaAtualizada -> {
            demandaPedidoDAO.atualizar(demandaAtualizada);
            carregarDemandas();
        });
    }

    private void abrirBuscaPedidoParaDemanda() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Nova Demanda");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setPrefWidth(420);

        TextField tfBusca = new TextField();
        tfBusca.setPromptText("🔍 Pesquisar pedido por nome, código ou data...");
        tfBusca.setStyle(
                "-fx-font-size: 13px; -fx-background-radius: 6; " +
                        "-fx-border-color: #dcdde1; -fx-border-radius: 6;"
        );

        VBox resultados = new VBox(8);
        ScrollPane scrollResultados = new ScrollPane(resultados);
        scrollResultados.setFitToWidth(true);
        scrollResultados.setPrefHeight(300);

        Runnable atualizarResultados = () -> {
            resultados.getChildren().clear();
            String texto = tfBusca.getText().toLowerCase().trim();
            List<Pedido> pedidos = pedidoDAO.listarTodos().stream()
                    .filter(p -> p.getSituacao() != SituacaoPedido.PRONTO)
                    .filter(p -> {
                        if (texto.isBlank()) return true;
                        String nome = p.getClienteNome() != null ? p.getClienteNome().toLowerCase() : "";
                        String codigo = extrairCodigo(p.getClienteTelefone()).toLowerCase();
                        String data = p.getDataEntrega() != null ? p.getDataEntrega().format(fmt).toLowerCase() : "";
                        return nome.contains(texto) || codigo.contains(texto) || data.contains(texto);
                    })
                    .sorted((a, b) -> a.getDataEntrega().compareTo(b.getDataEntrega()))
                    .toList();

            if (pedidos.isEmpty()) {
                Label vazio = new Label("Nenhum pedido encontrado.");
                vazio.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");
                resultados.getChildren().add(vazio);
                return;
            }

            for (Pedido p : pedidos) {
                String codigo = extrairCodigo(p.getClienteTelefone());
                Button btnPedido = new Button(p.getClienteNome() + (codigo.isEmpty() ? "" : " " + codigo) +
                        "  •  " + p.getDataEntrega().format(fmt));
                btnPedido.setMaxWidth(Double.MAX_VALUE);
                btnPedido.setStyle(
                        "-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; " +
                                "-fx-alignment: CENTER_LEFT; -fx-font-size: 13px; -fx-background-radius: 6;"
                );
                btnPedido.setOnAction(e -> {
                    dialog.close();
                    javafx.application.Platform.runLater(() -> abrirFormularioNovaDemanda(p));
                });
                resultados.getChildren().add(btnPedido);
            }
        };

        tfBusca.textProperty().addListener((obs, oldVal, newVal) -> atualizarResultados.run());
        atualizarResultados.run();

        box.getChildren().addAll(new Label("Selecione o pedido:"), tfBusca, scrollResultados);
        dialog.getDialogPane().setContent(box);
        dialog.showAndWait();
    }

    private void abrirFormularioNovaDemanda(Pedido p) {
        Dialog<DemandaPedido> dialog = new Dialog<>();
        dialog.setTitle("Nova Demanda para " + p.getClienteNome());

        TextArea taTexto = new TextArea();
        taTexto.setPromptText("Escreva a demanda...");
        taTexto.setPrefRowCount(4);

        ComboBox<String> cbDestinatario = new ComboBox<>();
        cbDestinatario.getItems().addAll("MARCO", "SIMONE");
        cbDestinatario.setPromptText("Selecione...");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Texto:"), 0, 0); grid.add(taTexto, 1, 0);
        grid.add(new Label("Destinatário:"), 0, 1); grid.add(cbDestinatario, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK && !taTexto.getText().isBlank() && cbDestinatario.getValue() != null) {
                DemandaPedido d = new DemandaPedido();
                d.setPedidoId(p.getId());
                d.setTexto(taTexto.getText().trim());
                d.setDestinatario(cbDestinatario.getValue());
                return d;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(demanda -> {
            demandaPedidoDAO.salvar(demanda);
            carregarDemandas();
        });
    }

    // ============================================================
    // AUXILIARES
    // ============================================================

    private String extrairCodigo(String telefone) {
        if (telefone == null || telefone.isBlank()) return "";
        String numeros = telefone.replaceAll("[^0-9]", "");
        if (numeros.length() < 4) return "";
        return numeros.substring(numeros.length() - 4);
    }

    private String getCorPrazo(LocalDate dataEntrega) {
        LocalDate hoje = LocalDate.now();
        if (dataEntrega.isBefore(hoje)) return "#000000";
        if (dataEntrega.isEqual(hoje)) return "#e74c3c";
        if (dataEntrega.isEqual(hoje.plusDays(1)) || dataEntrega.isEqual(hoje.plusDays(2))) return "#f39c12";
        return "#27ae60";
    }

    private ToggleButton criarChipFiltro(String texto) {
        ToggleButton tb = new ToggleButton(texto);
        tb.setStyle(estiloChip(false));
        tb.selectedProperty().addListener((obs, oldVal, novoValor) -> tb.setStyle(estiloChip(novoValor)));
        return tb;
    }

    private String estiloChip(boolean selecionado) {
        if (selecionado) {
            return "-fx-background-color: rgba(255,255,255,0.25); -fx-text-fill: white; " +
                    "-fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold; " +
                    "-fx-padding: 5 14 5 14; -fx-cursor: hand;";
        } else {
            return "-fx-background-color: white; -fx-text-fill: #2980b9; " +
                    "-fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold; " +
                    "-fx-padding: 5 14 5 14; -fx-cursor: hand;";
        }
    }

    private void imprimirEtiqueta(Pedido p) {
        String codigo = extrairCodigo(p.getClienteTelefone());
        String nomeCompleto = p.getClienteNome() + (codigo.isEmpty() ? "" : " " + codigo);

        java.util.List<String> linhas = new java.util.ArrayList<>();
        linhas.add("INVENTY STAMP");
        linhas.add("--------------");
        linhas.add("Cliente: " + nomeCompleto);
        if (p.getClienteTelefone() != null && !p.getClienteTelefone().isBlank())
            linhas.add("Telefone: " + p.getClienteTelefone());
        linhas.add("Prazo: " + p.getDataEntrega().format(fmt));
        linhas.add("Tipo: " + p.getTipoPersonalizacao().name());
        linhas.add("Pagamento: " + p.getStatusPagamento().name());
        if (p.getAtendente() != null && !p.getAtendente().isBlank())
            linhas.add("Atendente: " + p.getAtendente());
        if (p.isPrecisaNf())
            linhas.add("Precisa de NF");
        if (p.isPrecisaCupomFiscal())
            linhas.add("Precisa de Cupom Fiscal");
        if (p.getValor() != null)
            linhas.add("Valor: R$ " + p.getValor().setScale(2, java.math.RoundingMode.HALF_UP));
        if (p.getResta() != null && p.getStatusPagamento() == StatusPagamento.PARCIAL)
            linhas.add("Resta: R$ " + p.getResta().setScale(2, java.math.RoundingMode.HALF_UP));
        linhas.add("--------------");
        if (p.getAnotacao() != null && !p.getAnotacao().isBlank()) {
            linhas.add("Obs:");
            String anotacao = p.getAnotacao()
                    .replace("\r\n", " ")
                    .replace("\n", " ")
                    .replace("\r", " ")
                    .trim();
            int maxChars = 30;
            while (anotacao.length() > maxChars) {
                int corte = anotacao.lastIndexOf(" ", maxChars);
                if (corte == -1) corte = maxChars;
                linhas.add(anotacao.substring(0, corte).trim());
                anotacao = anotacao.substring(corte).trim();
            }
            if (!anotacao.isBlank()) linhas.add(anotacao);
        }

        try {
            int largura = 283;
            int altura = 425;
            java.awt.image.BufferedImage imagem = new java.awt.image.BufferedImage(
                    largura, altura, java.awt.image.BufferedImage.TYPE_INT_RGB);

            java.awt.Graphics2D g = imagem.createGraphics();
            g.setColor(java.awt.Color.WHITE);
            g.fillRect(0, 0, largura, altura);
            g.setColor(java.awt.Color.BLACK);
            g.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 14));

            int y = 30;
            for (String linha : linhas) {
                if (linha.equals("INVENTY STAMP")) {
                    g.setFont(new java.awt.Font("Monospaced", java.awt.Font.BOLD, 16));
                } else {
                    g.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 13));
                }
                g.drawString(linha, 10, y);
                y += 22;
            }
            g.dispose();

            java.awt.print.PrinterJob printerJob = java.awt.print.PrinterJob.getPrinterJob();

            javax.print.PrintService[] services = javax.print.PrintServiceLookup.lookupPrintServices(null, null);
            javax.print.PrintService servico = null;
            for (javax.print.PrintService s : services) {
                if (s.getName().equalsIgnoreCase("LABEL em inventy-kennia") ||
                        s.getName().equalsIgnoreCase("LABEL") ||
                        s.getName().toLowerCase().contains("label")) {
                    servico = s;
                    break;
                }
            }

            if (servico == null) {
                mostrarAlerta("Impressora LABEL não encontrada!");
                return;
            }

            printerJob.setPrintService(servico);

            java.awt.print.PageFormat pf = printerJob.defaultPage();
            java.awt.print.Paper paper = new java.awt.print.Paper();
            double larguraPaper = 100 / 25.4 * 72;
            double alturaPaper = 150 / 25.4 * 72;
            paper.setSize(larguraPaper, alturaPaper);
            paper.setImageableArea(0, 0, larguraPaper, alturaPaper);
            pf.setPaper(paper);
            pf.setOrientation(java.awt.print.PageFormat.PORTRAIT);

            java.awt.image.BufferedImage imagemFinal = imagem;
            printerJob.setPrintable((graphics, pageFormat, pageIndex) -> {
                if (pageIndex > 0) return java.awt.print.Printable.NO_SUCH_PAGE;
                graphics.drawImage(imagemFinal, 0, 0,
                        (int) pageFormat.getImageableWidth(),
                        (int) pageFormat.getImageableHeight(), null);
                return java.awt.print.Printable.PAGE_EXISTS;
            }, pf);

            printerJob.print();
            mostrarAlerta("✅ Enviado para impressora!");

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro: " + e.getClass().getName() + "\n" + e.getMessage());
        }
    }

    private void mostrarAlerta(String mensagem) {
        Alert.AlertType tipo = mensagem.startsWith("✅")
                ? Alert.AlertType.INFORMATION
                : Alert.AlertType.ERROR;
        Alert alert = new Alert(tipo, mensagem);
        alert.showAndWait();
    }

    private void abrirWhatsAppCliente(Pedido p) {
        if (p.getClienteTelefone() == null || p.getClienteTelefone().isBlank()) {
            mostrarAlerta("Este pedido não tem telefone cadastrado.");
            return;
        }
        try {
            String numeros = p.getClienteTelefone().replaceAll("[^0-9]", "");
            String numeroComDDI = numeros.startsWith("55") ? numeros : "55" + numeros;
            String url = "whatsapp://send?phone=" + numeroComDDI;
            Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", url});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void gerarECopiarCupomFiscal(Pedido p) {
        if (p.isValorACombinar() || p.getValor() == null || p.getValor().compareTo(BigDecimal.ZERO) == 0) {
            mostrarAlerta("⚠ Não é possível gerar o Cupom Fiscal sem um valor informado.");
            return;
        }

        String codigo = extrairCodigo(p.getClienteTelefone());
        String nomeCompleto = p.getClienteNome() + (codigo.isEmpty() ? "" : " " + codigo);

        java.util.List<String> linhas = new java.util.ArrayList<>();
        linhas.add("INVENTY STAMP");
        linhas.add("");
        if (!codigo.isEmpty()) {
            linhas.add("CODIGO_RETIRADA:" + codigo);
        }
        linhas.add(p.getClienteNome());
        if (p.getClienteTelefone() != null && !p.getClienteTelefone().isBlank())
            linhas.add("Tel: " + p.getClienteTelefone());
        linhas.add("");
        linhas.add("Personalização: " + p.getTipoPersonalizacao().name());
        if (p.getValor() != null)
            linhas.add("Valor: R$ " + p.getValor().setScale(2, java.math.RoundingMode.HALF_UP));
        if (p.getResta() != null && p.getStatusPagamento() == StatusPagamento.PARCIAL)
            linhas.add("Resta: R$ " + p.getResta().setScale(2, java.math.RoundingMode.HALF_UP));
        linhas.add("Pagamento: " + p.getStatusPagamento().name());
        linhas.add("Confirmado em: " + p.getDataConfirmacao().format(fmt));
        linhas.add("Entrega: " + p.getDataEntrega().format(fmt));
        if (p.getAtendente() != null && !p.getAtendente().isBlank())
            linhas.add("Atendente: " + p.getAtendente());

        try {
            int largura = 420;
            int altura = 60 + (linhas.size() * 28);
            java.awt.image.BufferedImage imagem = new java.awt.image.BufferedImage(
                    largura, altura, java.awt.image.BufferedImage.TYPE_INT_RGB);

            java.awt.Graphics2D g = imagem.createGraphics();
            g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(java.awt.Color.WHITE);
            g.fillRect(0, 0, largura, altura);
            g.setColor(java.awt.Color.BLACK);

            int y = 40;
            for (String linha : linhas) {
                if (linha.equals("INVENTY STAMP")) {
                    g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 20));
                    g.drawString(linha, 20, y);
                } else if (linha.startsWith("CODIGO_RETIRADA:")) {
                    g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 22));
                    g.drawString("Código de Retirada: " + linha.substring("CODIGO_RETIRADA:".length()), 20, y);
                } else if (linha.equals(p.getClienteNome())) {
                    g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
                    g.drawString(linha, 20, y);
                } else {
                    g.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 14));
                    g.drawString(linha, 20, y);
                }
                y += 28;
            }
            g.dispose();

            java.awt.datatransfer.Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
            java.awt.datatransfer.Transferable transferableImage = new java.awt.datatransfer.Transferable() {
                @Override
                public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors() {
                    return new java.awt.datatransfer.DataFlavor[]{java.awt.datatransfer.DataFlavor.imageFlavor};
                }
                @Override
                public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor flavor) {
                    return java.awt.datatransfer.DataFlavor.imageFlavor.equals(flavor);
                }
                @Override
                public Object getTransferData(java.awt.datatransfer.DataFlavor flavor) {
                    return imagem;
                }
            };
            clipboard.setContents(transferableImage, null);

            mostrarAlerta("✅ Cupom Fiscal copiado! Cole com Ctrl+V.");

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro ao gerar Cupom Fiscal: " + e.getMessage());
        }
    }

    public VBox getView() {
        return root;
    }
}