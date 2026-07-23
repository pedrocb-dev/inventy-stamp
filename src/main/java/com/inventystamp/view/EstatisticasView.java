package com.inventystamp.view;

import com.inventystamp.dao.EstatisticaDAO;
import com.inventystamp.model.EstatisticasMes;
import com.inventystamp.model.Gasto;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class EstatisticasView {

    private final EstatisticaDAO dao = new EstatisticaDAO();
    private VBox root;
    private List<EstatisticasMes> estatisticas;
    private int indiceAtual = 0;
    private VBox cardAtual;

    public EstatisticasView() {
        construir();
    }

    private void construir() {
        root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #e8eaed;");

        Label titulo = new Label("📊 Estatísticas");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        estatisticas = dao.listarTodos();

        if (estatisticas.isEmpty()) {
            Label vazio = new Label("Nenhuma estatística disponível ainda.\nConclua um pedido para começar.");
            vazio.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-text-alignment: center;");
            vazio.setWrapText(true);
            root.getChildren().addAll(titulo, vazio);
            return;
        }

        indiceAtual = estatisticas.size() - 1;

        // Navegação
        Button btnAnterior = new Button("◀ Anterior");
        btnAnterior.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-font-size: 13px;"
        );

        Button btnProximo = new Button("Próximo ▶");
        btnProximo.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-font-size: 13px;"
        );

        Label lblMesAtual = new Label();
        lblMesAtual.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox navegacao = new HBox(16, btnAnterior, lblMesAtual, btnProximo);
        navegacao.setAlignment(Pos.CENTER);

        cardAtual = new VBox();

        ScrollPane scroll = new ScrollPane(cardAtual);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        btnAnterior.setOnAction(e -> {
            if (indiceAtual > 0) {
                indiceAtual--;
                atualizarCard(lblMesAtual);
            }
        });

        btnProximo.setOnAction(e -> {
            if (indiceAtual < estatisticas.size() - 1) {
                indiceAtual++;
                atualizarCard(lblMesAtual);
            }
        });

        atualizarCard(lblMesAtual);

        root.getChildren().addAll(titulo, navegacao, scroll);
    }

    private void atualizarCard(Label lblMesAtual) {
        EstatisticasMes e = estatisticas.get(indiceAtual);

        String nomeMes = Month.of(e.getMes())
                .getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
        nomeMes = nomeMes.substring(0, 1).toUpperCase() + nomeMes.substring(1);
        lblMesAtual.setText(nomeMes + " / " + e.getAno());

        cardAtual.getChildren().clear();
        cardAtual.setSpacing(20);
        cardAtual.setPadding(new Insets(20));
        cardAtual.setStyle(
                "-fx-background-color: white; -fx-background-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);"
        );

        // Gráfico de linha — evolução do lucro bruto
        Label lblGraficoLinha = new Label("📈 Evolução do Lucro Bruto");
        lblGraficoLinha.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("R$");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("");
        lineChart.setLegendVisible(false);
        lineChart.setPrefHeight(250);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (EstatisticasMes est : estatisticas) {
            String mes = Month.of(est.getMes())
                    .getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"));
            series.getData().add(new XYChart.Data<>(mes + "/" + est.getAno(), est.getLucroBruto()));
        }
        lineChart.getData().add(series);

        // Gráfico de pizza — bordado vs outros
        Label lblGraficoPizza = new Label("🥧 Lucro por Tipo de Personalização");
        lblGraficoPizza.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        PieChart pieChart = new PieChart();
        pieChart.setPrefHeight(250);

        if (e.getLucroBordado().compareTo(BigDecimal.ZERO) > 0) {
            pieChart.getData().add(new PieChart.Data("Bordado (R$ " +
                    e.getLucroBordado().setScale(2, RoundingMode.HALF_UP) + ")", e.getLucroBordado().doubleValue()));
        }
        if (e.getLucroOutros().compareTo(BigDecimal.ZERO) > 0) {
            pieChart.getData().add(new PieChart.Data("Outros (R$ " +
                    e.getLucroOutros().setScale(2, RoundingMode.HALF_UP) + ")", e.getLucroOutros().doubleValue()));
        }

        if (pieChart.getData().isEmpty()) {
            pieChart.getData().add(new PieChart.Data("Sem dados", 1));
        }

        // Separador
        Separator sep1 = new Separator();
        Separator sep2 = new Separator();

        // Lucro bruto e líquido
        Label lblLucroBruto = new Label("💰 Lucro Bruto: R$ " +
                e.getLucroBruto().setScale(2, RoundingMode.HALF_UP));
        lblLucroBruto.setStyle("-fx-font-size: 15px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");

        // Calcula lucro líquido
        List<Gasto> gastos = dao.listarGastos(e.getId());
        BigDecimal totalGastos = gastos.stream()
                .map(Gasto::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal lucroLiquido = e.getLucroBruto().subtract(totalGastos);

        Label lblLucroLiquido = new Label("💵 Lucro Líquido: R$ " +
                lucroLiquido.setScale(2, RoundingMode.HALF_UP));
        lblLucroLiquido.setStyle("-fx-font-size: 15px; -fx-text-fill: #2980b9; -fx-font-weight: bold;");

        // Botão adicionar gasto
        Button btnAdicionarGasto = new Button("➕ Adicionar Gasto");
        btnAdicionarGasto.setStyle(
                "-fx-background-color: #e67e22; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-font-size: 13px;"
        );
        btnAdicionarGasto.setOnAction(ev -> abrirDialogGasto(e, lblLucroLiquido, gastos));

        // Lista de gastos
        VBox listaGastos = new VBox(6);
        listaGastos.setPadding(new Insets(8, 0, 0, 0));
        atualizarListaGastos(listaGastos, gastos, e, lblLucroLiquido);

        HBox rodape = new HBox(20, lblLucroBruto, lblLucroLiquido, btnAdicionarGasto);
        rodape.setAlignment(Pos.CENTER_LEFT);

        cardAtual.getChildren().addAll(
                lblGraficoLinha, lineChart,
                sep1,
                lblGraficoPizza, pieChart,
                sep2,
                rodape, listaGastos
        );
    }

    private void abrirDialogGasto(EstatisticasMes e, Label lblLucroLiquido, List<Gasto> gastos) {
        Dialog<Gasto> dialog = new Dialog<>();
        dialog.setTitle("Adicionar Gasto");

        TextField tfCategoria = new TextField();
        tfCategoria.setPromptText("Ex: Aluguel, Imposto, Alimentação...");
        tfCategoria.setPrefWidth(250);

        TextField tfValor = new TextField();
        tfValor.setPromptText("Ex: 150.00");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Categoria:"), 0, 0); grid.add(tfCategoria, 1, 0);
        grid.add(new Label("Valor (R$):"), 0, 1); grid.add(tfValor, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK && !tfCategoria.getText().isBlank() && !tfValor.getText().isBlank()) {
                Gasto g = new Gasto();
                g.setEstatisticaId(e.getId());
                g.setCategoria(tfCategoria.getText().trim());
                try {
                    g.setValor(new BigDecimal(tfValor.getText().replace(",", ".")));
                } catch (Exception ex) {
                    return null;
                }
                return g;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(g -> {
            dao.adicionarGasto(g);
            gastos.add(g);

            BigDecimal totalGastos = gastos.stream()
                    .map(Gasto::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal lucroLiquido = e.getLucroBruto().subtract(totalGastos);
            lblLucroLiquido.setText("💵 Lucro Líquido: R$ " + lucroLiquido.setScale(2, RoundingMode.HALF_UP));

            VBox listaGastos = (VBox) lblLucroLiquido.getParent().getParent().getChildrenUnmodifiable()
                    .stream().filter(n -> n instanceof VBox).findFirst().orElse(null);
            if (listaGastos != null) atualizarListaGastos(listaGastos, gastos, e, lblLucroLiquido);
        });
    }

    private void atualizarListaGastos(VBox listaGastos, List<Gasto> gastos, EstatisticasMes e, Label lblLucroLiquido) {
        listaGastos.getChildren().clear();

        if (gastos.isEmpty()) {
            Label vazio = new Label("Nenhum gasto registrado.");
            vazio.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");
            listaGastos.getChildren().add(vazio);
            return;
        }

        Label lblTitulo = new Label("📋 Gastos:");
        lblTitulo.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        listaGastos.getChildren().add(lblTitulo);

        for (Gasto g : new java.util.ArrayList<>(gastos)) {
            HBox linha = new HBox(10);
            linha.setAlignment(Pos.CENTER_LEFT);

            Label lblGasto = new Label("• " + g.getCategoria() + ": R$ " +
                    g.getValor().setScale(2, RoundingMode.HALF_UP));
            lblGasto.setStyle("-fx-font-size: 13px; -fx-text-fill: #e74c3c;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button btnRemover = new Button("🗑");
            btnRemover.setStyle(
                    "-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                            "-fx-background-radius: 4; -fx-font-size: 11px;"
            );
            btnRemover.setOnAction(ev -> {
                dao.deletarGasto(g.getId());
                gastos.remove(g);

                BigDecimal totalGastos = gastos.stream()
                        .map(Gasto::getValor)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal lucroLiquido = e.getLucroBruto().subtract(totalGastos);
                lblLucroLiquido.setText("💵 Lucro Líquido: R$ " + lucroLiquido.setScale(2, RoundingMode.HALF_UP));

                atualizarListaGastos(listaGastos, gastos, e, lblLucroLiquido);
            });

            linha.getChildren().addAll(lblGasto, spacer, btnRemover);
            listaGastos.getChildren().add(linha);
        }
    }

    public VBox getView() {
        return root;
    }
}