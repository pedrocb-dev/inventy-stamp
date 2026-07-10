package com.inventystamp.view;

import com.inventystamp.dao.PedidoDAO;
import com.inventystamp.dao.TarefaDAO;
import com.inventystamp.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PedidosView {

    private Usuario usuario;
    private PedidoDAO pedidoDAO = new PedidoDAO();
    private VBox root;
    private VBox listaPedidos;
    private String filtroAtual = "TODOS";
    private DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public PedidosView(Usuario usuario) {
        this.usuario = usuario;
        construir();
    }

    private void construir() {
        root = new VBox(15);
        root.setPadding(new Insets(20));

        Label titulo = new Label("📋 Pedidos");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Filtros
        HBox filtros = new HBox(10);
        filtros.setAlignment(Pos.CENTER_LEFT);

        Button btnTodos = criarBotaoFiltro("Todos");
        Button btnBordado = criarBotaoFiltro("Bordado");
        Button btnDemais = criarBotaoFiltro("Demais");

        btnTodos.setOnAction(e -> { filtroAtual = "TODOS"; carregarPedidos(); });
        btnBordado.setOnAction(e -> { filtroAtual = "BORDADO"; carregarPedidos(); });
        btnDemais.setOnAction(e -> { filtroAtual = "DEMAIS"; carregarPedidos(); });

        filtros.getChildren().addAll(new Label("Filtrar:"), btnTodos, btnBordado, btnDemais);

        listaPedidos = new VBox(10);
        ScrollPane scroll = new ScrollPane(listaPedidos);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        root.getChildren().addAll(titulo, filtros, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        carregarPedidos();
    }

    private void carregarPedidos() {
        listaPedidos.getChildren().clear();
        List<Pedido> pedidos = pedidoDAO.listarTodos();

        pedidos.stream()
                .filter(p -> {
                    if (filtroAtual.equals("BORDADO")) return p.getTipoPersonalizacao() == TipoPersonalizacao.BORDADO;
                    if (filtroAtual.equals("DEMAIS")) return p.getTipoPersonalizacao() != TipoPersonalizacao.BORDADO;
                    return true;
                })
                .sorted((a, b) -> a.getDataEntrega().compareTo(b.getDataEntrega()))
                .forEach(p -> listaPedidos.getChildren().add(criarCardPedido(p)));

        if (listaPedidos.getChildren().isEmpty()) {
            Label vazio = new Label("Nenhum pedido encontrado.");
            vazio.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");
            listaPedidos.getChildren().add(vazio);
        }
    }

    private VBox criarCardPedido(Pedido p) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0, 0, 2);"
        );

        // Cor da aba por prazo
        String corAba = getCorPrazo(p.getDataEntrega());
        HBox aba = new HBox();
        aba.setPrefHeight(6);
        aba.setStyle("-fx-background-color: " + corAba + "; -fx-background-radius: 4;");

        // Cabeçalho
        HBox cabecalho = new HBox(10);
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        Label nomeCliente = new Label(p.getClienteNome());
        nomeCliente.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label tipo = new Label(p.getTipoPersonalizacao().name());
        tipo.setStyle(
                "-fx-background-color: #ecf0f1; -fx-padding: 2 8 2 8; " +
                        "-fx-background-radius: 10; -fx-font-size: 11px; -fx-text-fill: #7f8c8d;"
        );

        Label dataEntrega = new Label("Entrega: " + p.getDataEntrega().format(fmt));
        dataEntrega.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        cabecalho.getChildren().addAll(nomeCliente, tipo, spacer, dataEntrega);

        // Situação
        Label situacao = new Label("● " + p.getSituacao().name());
        situacao.setStyle("-fx-font-size: 12px; -fx-text-fill: " +
                (p.getSituacao() == SituacaoPedido.PRONTO ? "#27ae60" : "#e67e22") + ";");

        // Prazo em dias
        long diasParaEntrega = java.time.temporal.ChronoUnit.DAYS.between(
                p.getDataConfirmacao(), p.getDataEntrega());
        Label lblPrazo = new Label("⏱ Prazo: " + diasParaEntrega + " dia(s)");
        lblPrazo.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        // Valor e Resta
        HBox infoValor = new HBox(15);
        if (p.getValor() != null) {
            Label lblValor = new Label("💰 Valor: R$ " + p.getValor().setScale(2, java.math.RoundingMode.HALF_UP));
            lblValor.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c3e50;");
            infoValor.getChildren().add(lblValor);
        }
        if (p.getResta() != null) {
            Label lblResta = new Label("⚠ Resta: R$ " + p.getResta().setScale(2, java.math.RoundingMode.HALF_UP));
            lblResta.setStyle("-fx-font-size: 12px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            infoValor.getChildren().add(lblResta);
        }

        // Anotação
        Label anotacao = new Label(p.getAnotacao() != null ? p.getAnotacao() : "");
        anotacao.setWrapText(true);
        anotacao.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

        // Tarefas do pedido
        TarefaDAO tarefaDAO = new TarefaDAO();
        List<Tarefa> tarefas = tarefaDAO.listarPorPedido(p.getId());
        VBox tarefasBox = new VBox(4);
        if (!tarefas.isEmpty()) {
            Label lblTarefas = new Label("Tarefas:");
            lblTarefas.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            tarefasBox.getChildren().add(lblTarefas);
            for (Tarefa t : tarefas) {
                CheckBox cb = new CheckBox(t.getDescricao());
                cb.setSelected(t.isConcluida());
                cb.setStyle("-fx-font-size: 12px;");
                cb.setOnAction(e -> {
                    t.setConcluida(cb.isSelected());
                    tarefaDAO.atualizar(t);
                });
                tarefasBox.getChildren().add(cb);
            }
        }

        // Botões editar/remover
        HBox acoes = new HBox(8);
        acoes.setAlignment(Pos.CENTER_RIGHT);

        Button btnEditar = new Button("✏ Editar");
        btnEditar.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-font-size: 11px;"
        );
        btnEditar.setOnAction(e -> abrirEdicao(p));

        Button btnRemover = new Button("🗑 Remover");
        btnRemover.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-font-size: 11px;"
        );
        btnRemover.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Remover pedido de " + p.getClienteNome() + "?");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    pedidoDAO.deletar(p.getId());
                    carregarPedidos();
                }
            });
        });

        acoes.getChildren().addAll(btnEditar, btnRemover);
        card.getChildren().addAll(aba, cabecalho, situacao, lblPrazo, infoValor, anotacao, tarefasBox, acoes);
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

        ComboBox<SituacaoPedido> cbSituacao = new ComboBox<>();
        cbSituacao.getItems().addAll(SituacaoPedido.values());
        cbSituacao.setValue(p.getSituacao());

        TextField tfValor = new TextField(p.getValor() != null ? p.getValor().toString() : "");

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
        grid.add(new Label("Situação:"), 0, 7); grid.add(cbSituacao, 1, 7);

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
                p.setSituacao(cbSituacao.getValue());
                try { p.setValor(new java.math.BigDecimal(tfValor.getText())); } catch (Exception ex) {}
                return p;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(pedidoAtualizado -> {
            pedidoDAO.atualizar(pedidoAtualizado);
            carregarPedidos();
        });
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