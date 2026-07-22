package com.inventystamp.view;

import com.inventystamp.dao.PedidoDAO;
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

public class PedidosView {

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private VBox root;
    private GridPane gridPedidos;
    private String filtroAtual = "TODOS";
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PedidosView() {
        construir();
    }

    private void construir() {
        root = new VBox(15);
        root.setPadding(new Insets(20));

        Label titulo = new Label("📋 Pedidos");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox filtros = new HBox(10);
        filtros.setAlignment(Pos.CENTER_LEFT);

        Button btnTodos = criarBotaoFiltro("Todos");
        Button btnBordado = criarBotaoFiltro("Bordado");
        Button btnDemais = criarBotaoFiltro("Demais");

        btnTodos.setOnAction(e -> { filtroAtual = "TODOS"; carregarPedidos(); });
        btnBordado.setOnAction(e -> { filtroAtual = "BORDADO"; carregarPedidos(); });
        btnDemais.setOnAction(e -> { filtroAtual = "DEMAIS"; carregarPedidos(); });

        filtros.getChildren().addAll(new Label("Filtrar:"), btnTodos, btnBordado, btnDemais);

        gridPedidos = new GridPane();
        gridPedidos.setHgap(12);
        gridPedidos.setVgap(12);
        gridPedidos.setPadding(new Insets(4));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        gridPedidos.getColumnConstraints().addAll(col1, col2);

        ScrollPane scroll = new ScrollPane(gridPedidos);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        root.getChildren().addAll(titulo, filtros, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        carregarPedidos();
    }

    private void carregarPedidos() {
        gridPedidos.getChildren().clear();

        List<Pedido> pedidos = pedidoDAO.listarTodos().stream()
                .filter(p -> p.getSituacao() != SituacaoPedido.PRONTO)
                .filter(p -> {
                    if (filtroAtual.equals("BORDADO")) return p.getTipoPersonalizacao() == TipoPersonalizacao.BORDADO;
                    if (filtroAtual.equals("DEMAIS")) return p.getTipoPersonalizacao() != TipoPersonalizacao.BORDADO;
                    return true;
                })
                .sorted((a, b) -> a.getDataEntrega().compareTo(b.getDataEntrega()))
                .toList();

        if (pedidos.isEmpty()) {
            Label vazio = new Label("Nenhum pedido encontrado.");
            vazio.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");
            gridPedidos.add(vazio, 0, 0, 2, 1);
            return;
        }

        for (int i = 0; i < pedidos.size(); i++) {
            VBox card = criarCardPedido(pedidos.get(i));
            gridPedidos.add(card, i % 2, i / 2);
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

        // Barra colorida por prazo no topo
        HBox barra = new HBox();
        barra.setPrefHeight(6);
        barra.setMaxWidth(Double.MAX_VALUE);
        barra.setStyle("-fx-background-color: " + getCorPrazo(p.getDataEntrega()) + "; -fx-background-radius: 4;");

        // Nome + código
        String codigo = extrairCodigo(p.getClienteTelefone());
        Label lblNome = new Label(p.getClienteNome() + (codigo.isEmpty() ? "" : " " + codigo));
        lblNome.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        lblNome.setWrapText(true);

        // Tipo de personalização
        Label lblTipo = new Label(p.getTipoPersonalizacao().name());
        lblTipo.setStyle(
                "-fx-background-color: #ecf0f1; -fx-padding: 2 8 2 8; " +
                        "-fx-background-radius: 10; -fx-font-size: 13px; -fx-text-fill: #7f8c8d;"
        );

        // Data de entrega + prazo
        long dias = java.time.temporal.ChronoUnit.DAYS.between(
                p.getDataConfirmacao(), p.getDataEntrega());
        Label lblEntrega = new Label("📅 Entrega: " + p.getDataEntrega().format(fmt) + "  ⏱ " + dias + " dia(s)");
        lblEntrega.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        lblEntrega.setWrapText(true);

        // Valor
        VBox infoValor = new VBox(2);
        if (p.getValor() != null) {
            Label lblValor = new Label("💰 Valor: R$ " + p.getValor().setScale(2, java.math.RoundingMode.HALF_UP));
            lblValor.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
            infoValor.getChildren().add(lblValor);
        }
        if (p.getResta() != null) {
            Label lblResta = new Label("⚠ Resta: R$ " + p.getResta().setScale(2, java.math.RoundingMode.HALF_UP));
            lblResta.setStyle("-fx-font-size: 14px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            infoValor.getChildren().add(lblResta);
        }

        // Status de pagamento
        Label lblPagamento = new Label("💳 " + p.getStatusPagamento().name());
        lblPagamento.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");

        // Anotação completa
        Label lblAnotacao = new Label(p.getAnotacao() != null ? p.getAnotacao() : "");
        lblAnotacao.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
        lblAnotacao.setWrapText(true);

        // Botões na parte inferior
        HBox acoes = new HBox(8);
        acoes.setAlignment(Pos.CENTER_RIGHT);

        Button btnConcluir = new Button("✅ Concluir");
        btnConcluir.setStyle(
                "-fx-background-color: #27ae60; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-font-size: 13px;"
        );

        Button btnEditar = new Button("✏ Editar");
        btnEditar.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-font-size: 13px;"
        );

        Button btnRemover = new Button("🗑 Remover");
        btnRemover.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-font-size: 13px;"
        );

        btnConcluir.setOnAction(e -> {
            p.setSituacao(SituacaoPedido.PRONTO);
            pedidoDAO.atualizar(p);

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
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Remover pedido de " + p.getClienteNome() + "?");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    pedidoDAO.deletar(p.getId());
                    carregarPedidos();
                }
            });
        });

        acoes.getChildren().addAll(btnConcluir, btnEditar, btnRemover);

        VBox topo = new VBox(10, barra, lblNome, lblTipo, lblEntrega, infoValor, lblPagamento, lblAnotacao);
        VBox.setVgrow(lblAnotacao, Priority.ALWAYS);

        card.getChildren().addAll(topo, acoes);
        VBox.setVgrow(topo, Priority.ALWAYS);
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

        TextField tfValor = new TextField(p.getValor() != null ? p.getValor().toString() : "");

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
        grid.add(new Label("Valor:"), 0, 5); grid.add(tfValor, 1, 5);
        grid.add(new Label("Pagamento:"), 0, 6); grid.add(cbPagamento, 1, 6);
        grid.add(lblResta, 0, 7); grid.add(tfResta, 1, 7);

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
                try { p.setValor(new java.math.BigDecimal(tfValor.getText())); } catch (Exception ex) {}
                try {
                    if (!tfResta.getText().isBlank())
                        p.setResta(new java.math.BigDecimal(tfResta.getText()));
                } catch (Exception ex) {}
                return p;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(pedidoAtualizado -> {
            pedidoDAO.atualizar(pedidoAtualizado);
            carregarPedidos();
        });
    }

    private String extrairCodigo(String telefone) {
        if (telefone == null || telefone.isBlank()) return "";
        String numeros = telefone.replaceAll("[^0-9]", "");
        if (numeros.length() < 4) return "";
        return numeros.substring(numeros.length() - 4);
    }

    private String getCorPrazo(LocalDate dataEntrega) {
        LocalDate hoje = LocalDate.now();
        if (dataEntrega.isEqual(hoje)) return "#e74c3c";
        if (dataEntrega.isEqual(hoje.plusDays(1))) return "#f39c12";
        return "#27ae60";
    }

    private Button criarBotaoFiltro(String texto) {
        Button btn = new Button(texto);
        btn.setStyle(
                "-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; " +
                        "-fx-background-radius: 5; -fx-font-size: 12px;"
        );
        return btn;
    }

    public VBox getView() {
        return root;
    }
}