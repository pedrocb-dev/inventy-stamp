package com.inventystamp.view;

import com.inventystamp.dao.PedidoDAO;
import com.inventystamp.dao.TarefaDAO;
import com.inventystamp.dao.DemandaDAO;
import com.inventystamp.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AtendimentoView {

    private Usuario usuario;
    private PedidoDAO pedidoDAO = new PedidoDAO();
    private TarefaDAO tarefaDAO = new TarefaDAO();
    private VBox root;

    public AtendimentoView(Usuario usuario) {
        this.usuario = usuario;
        construir();
    }

    private void construir() {
        root = new VBox(15);
        root.setPadding(new Insets(20));

        Label titulo = new Label("🎧 Atendimento — Novo Pedido");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        TextField tfNome = new TextField();
        tfNome.setPromptText("Nome do cliente");
        tfNome.setPrefWidth(300);

        TextField tfTelefone = new TextField();
        tfTelefone.setPromptText("Ex: (11) 99999-9999");

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
        taAnotacao.setPrefWidth(300);

        ComboBox<TipoPersonalizacao> cbTipo = new ComboBox<>();
        cbTipo.getItems().addAll(TipoPersonalizacao.values());
        cbTipo.setPromptText("Selecione...");
        cbTipo.setPrefWidth(300);

        DatePicker dpConfirmacao = new DatePicker(LocalDate.now());
        DatePicker dpEntrega = new DatePicker();
        dpEntrega.setPromptText("Data de entrega");

        TextField tfValor = new TextField();
        tfValor.setPromptText("Ex: 65.00");

        TextField tfResta = new TextField();
        tfResta.setPromptText("Valor restante");
        tfResta.setVisible(false);
        tfResta.setManaged(false);

        Label lblResta = new Label("Resta (R$)");
        lblResta.setVisible(false);
        lblResta.setManaged(false);

        ComboBox<StatusPagamento> cbPagamento = new ComboBox<>();
        cbPagamento.getItems().addAll(StatusPagamento.values());
        cbPagamento.setPromptText("Selecione...");
        cbPagamento.setPrefWidth(300);

        // Mostra campo "Resta" apenas quando PARCIAL for selecionado
        cbPagamento.setOnAction(e -> {
            boolean isParcial = cbPagamento.getValue() == StatusPagamento.PARCIAL;
            tfResta.setVisible(isParcial);
            tfResta.setManaged(isParcial);
            lblResta.setVisible(isParcial);
            lblResta.setManaged(isParcial);
        });

        int row = 0;
        grid.add(new Label("Cliente *"), 0, row); grid.add(tfNome, 1, row++);
        grid.add(new Label("Telefone"), 0, row); grid.add(tfTelefone, 1, row++);
        grid.add(new Label("Anotação"), 0, row); grid.add(taAnotacao, 1, row++);
        grid.add(new Label("Personalização *"), 0, row); grid.add(cbTipo, 1, row++);
        grid.add(new Label("Data confirmação *"), 0, row); grid.add(dpConfirmacao, 1, row++);
        grid.add(new Label("Data entrega *"), 0, row); grid.add(dpEntrega, 1, row++);
        grid.add(new Label("Valor (R$)"), 0, row); grid.add(tfValor, 1, row++);
        grid.add(new Label("Pagamento *"), 0, row); grid.add(cbPagamento, 1, row++);
        grid.add(lblResta, 0, row); grid.add(tfResta, 1, row++);

        Label lblFeedback = new Label();
        lblFeedback.setStyle("-fx-font-size: 13px;");

        Button btnSalvar = new Button("💾 Salvar Pedido");
        btnSalvar.setPrefWidth(200);
        btnSalvar.setPrefHeight(40);
        btnSalvar.setStyle(
                "-fx-background-color: #27ae60; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-font-size: 13px;"
        );

        btnSalvar.setOnAction(e -> {
            if (tfNome.getText().isBlank() || cbTipo.getValue() == null ||
                    dpEntrega.getValue() == null || cbPagamento.getValue() == null) {
                lblFeedback.setText("⚠ Preencha todos os campos obrigatórios (*).");
                lblFeedback.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
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

            try {
                if (!tfValor.getText().isBlank())
                    p.setValor(new BigDecimal(tfValor.getText().replace(",", ".")));
            } catch (Exception ex) {
                lblFeedback.setText("⚠ Valor inválido. Use o formato: 65.00");
                lblFeedback.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
                return;
            }

            try {
                if (cbPagamento.getValue() == StatusPagamento.PARCIAL && !tfResta.getText().isBlank())
                    p.setResta(new BigDecimal(tfResta.getText().replace(",", ".")));
            } catch (Exception ex) {
                lblFeedback.setText("⚠ Valor de 'Resta' inválido. Use o formato: 32.50");
                lblFeedback.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
                return;
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

            tfNome.clear();
            tfTelefone.clear();
            taAnotacao.clear();
            cbTipo.setValue(null);
            dpConfirmacao.setValue(LocalDate.now());
            dpEntrega.setValue(null);
            tfValor.clear();
            tfResta.clear();
            cbPagamento.setValue(null);

            lblFeedback.setText("✅ Pedido salvo com sucesso!");
            lblFeedback.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 13px;");
        });

        ScrollPane scroll = new ScrollPane();
        VBox conteudo = new VBox(15, grid, btnSalvar, lblFeedback);
        conteudo.setPadding(new Insets(10));
        scroll.setContent(conteudo);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        root.getChildren().addAll(titulo, scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
    }

    public VBox getView() {
        return root;
    }
}