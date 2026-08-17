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

        // Gráfico de barras — lucro por personalização
        CategoryAxis xAxisLucro = new CategoryAxis();
        xAxisLucro.setTickLabelRotation(0);
        NumberAxis yAxisLucro = new NumberAxis();
        yAxisLucro.setLabel("R$");

        BarChart<String, Number> pieChart = new BarChart<>(xAxisLucro, yAxisLucro);
        pieChart.setPrefSize(420, 320);
        pieChart.setMinSize(420, 320);
        pieChart.setMaxSize(420, 320);
        pieChart.setLegendVisible(false);

        XYChart.Series<String, Number> serieLucro = new XYChart.Series<>();
        adicionarBarraLucro(serieLucro, "Bordado", e.getLucroBordado());
        adicionarBarraLucro(serieLucro, "DTF", e.getLucroDtf());
        adicionarBarraLucro(serieLucro, "Plotter", e.getLucroPlotter());
        adicionarBarraLucro(serieLucro, "Sublimação", e.getLucroSublimacao());
        adicionarBarraLucro(serieLucro, "Transfer", e.getLucroTransfer());
        adicionarBarraLucro(serieLucro, "Silkscreen", e.getLucroSilkscreen());
        adicionarBarraLucro(serieLucro, "Sem person.", e.getLucroSemPersonalizacao());
        pieChart.getData().add(serieLucro);
        instalarTooltipsBarras(pieChart, true);

// Gráfico de barras — tamanhos de pedido
        Label lblGraficoTamanhos = new Label("📊 Pedidos por Tamanho");
        lblGraficoTamanhos.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        CategoryAxis xAxisTamanho = new CategoryAxis();
        NumberAxis yAxisTamanho = new NumberAxis();
        yAxisTamanho.setLabel("Quantidade");

        BarChart<String, Number> graficoRosca = new BarChart<>(xAxisTamanho, yAxisTamanho);
        graficoRosca.setPrefSize(420, 320);
        graficoRosca.setMinSize(420, 320);
        graficoRosca.setMaxSize(420, 320);
        graficoRosca.setLegendVisible(false);

        XYChart.Series<String, Number> serieTamanho = new XYChart.Series<>();
        adicionarBarraQuantidade(serieTamanho, "Pequeno\n(até 10)", e.getQtdPequeno());
        adicionarBarraQuantidade(serieTamanho, "Médio\n(até 30)", e.getQtdMedio());
        adicionarBarraQuantidade(serieTamanho, "Grande\n(até 50)", e.getQtdGrande());
        adicionarBarraQuantidade(serieTamanho, "Bem grande\n(+50)", e.getQtdBemGrande());
        graficoRosca.getData().add(serieTamanho);
        instalarTooltipsBarras(graficoRosca, false);

        StackPane containerRosca = new StackPane(graficoRosca);

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

        Label lblTotalGastos = new Label("💸 Total de Gastos: R$ " +
                totalGastos.setScale(2, RoundingMode.HALF_UP));
        lblTotalGastos.setStyle("-fx-font-size: 15px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        // Botão adicionar gasto
        Button btnAdicionarGasto = new Button("➕ Adicionar Gasto");
        btnAdicionarGasto.setStyle(
                "-fx-background-color: #e67e22; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-font-size: 13px;"
        );
        btnAdicionarGasto.setOnAction(ev -> abrirDialogGasto(e, lblLucroLiquido, lblTotalGastos, gastos));

        // Lista de gastos
        VBox listaGastos = new VBox(6);
        listaGastos.setPadding(new Insets(8, 0, 0, 0));
        atualizarListaGastos(listaGastos, gastos, e, lblLucroLiquido, lblTotalGastos);

        HBox rodape = new HBox(20, lblLucroBruto, lblLucroLiquido, lblTotalGastos, btnAdicionarGasto);
        rodape.setAlignment(Pos.CENTER_LEFT);

        VBox blocoPizza = new VBox(10, lblGraficoPizza, pieChart);
        blocoPizza.setPadding(new Insets(20));
        blocoPizza.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(blocoPizza, Priority.ALWAYS);
        blocoPizza.setStyle(
                "-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                        "-fx-border-color: #dcdde1; -fx-border-radius: 10;"
        );

        VBox blocoRosca = new VBox(10, lblGraficoTamanhos, containerRosca);
        blocoRosca.setPadding(new Insets(20));
        blocoRosca.setAlignment(Pos.TOP_RIGHT);
        blocoRosca.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(blocoRosca, Priority.ALWAYS);
        blocoRosca.setStyle(
                "-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                        "-fx-border-color: #dcdde1; -fx-border-radius: 10;"
        );

        HBox linhaGraficos = new HBox(40, blocoPizza, blocoRosca);
        linhaGraficos.setAlignment(Pos.TOP_LEFT);
        linhaGraficos.setMaxWidth(Double.MAX_VALUE);

        cardAtual.getChildren().addAll(
                lblGraficoLinha, lineChart,
                sep1,
                linhaGraficos,
                sep2,
                rodape, listaGastos
        );
    }

    private void abrirDialogGasto(EstatisticasMes e, Label lblLucroLiquido, Label lblTotalGastos, List<Gasto> gastos) {
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

            BigDecimal totalGastosAtualizado = gastos.stream()
                    .map(Gasto::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal lucroLiquido = e.getLucroBruto().subtract(totalGastosAtualizado);
            lblLucroLiquido.setText("💵 Lucro Líquido: R$ " + lucroLiquido.setScale(2, RoundingMode.HALF_UP));
            lblTotalGastos.setText("💸 Total de Gastos: R$ " + totalGastosAtualizado.setScale(2, RoundingMode.HALF_UP));

            VBox listaGastos = (VBox) lblLucroLiquido.getParent().getParent().getChildrenUnmodifiable()
                    .stream().filter(n -> n instanceof VBox).findFirst().orElse(null);
            if (listaGastos != null) atualizarListaGastos(listaGastos, gastos, e, lblLucroLiquido, lblTotalGastos);
        });
    }

    private void atualizarListaGastos(VBox listaGastos, List<Gasto> gastos, EstatisticasMes e, Label lblLucroLiquido, Label lblTotalGastos) {
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

                BigDecimal totalGastosAtualizado = gastos.stream()
                        .map(Gasto::getValor)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal lucroLiquido = e.getLucroBruto().subtract(totalGastosAtualizado);
                lblLucroLiquido.setText("💵 Lucro Líquido: R$ " + lucroLiquido.setScale(2, RoundingMode.HALF_UP));
                lblTotalGastos.setText("💸 Total de Gastos: R$ " + totalGastosAtualizado.setScale(2, RoundingMode.HALF_UP));

                atualizarListaGastos(listaGastos, gastos, e, lblLucroLiquido, lblTotalGastos);
            });

            linha.getChildren().addAll(lblGasto, spacer, btnRemover);
            listaGastos.getChildren().add(linha);
        }
    }

    public VBox getView() {

        return root;
    }

    private void adicionarBarraLucro(XYChart.Series<String, Number> serie, String nome, BigDecimal valor) {
        serie.getData().add(new XYChart.Data<>(nome, valor.doubleValue()));
    }

    private void adicionarBarraQuantidade(XYChart.Series<String, Number> serie, String nome, int quantidade) {
        serie.getData().add(new XYChart.Data<>(nome, quantidade));
    }

    private void instalarTooltipsBarras(BarChart<String, Number> chart, boolean valorEmReais) {
        javafx.application.Platform.runLater(() -> {
            for (XYChart.Series<String, Number> serie : chart.getData()) {
                for (XYChart.Data<String, Number> data : serie.getData()) {
                    if (data.getNode() != null) {
                        String texto = valorEmReais
                                ? "R$ " + new BigDecimal(data.getYValue().toString()).setScale(2, RoundingMode.HALF_UP)
                                : data.getYValue().toString();
                        Tooltip.install(data.getNode(), new Tooltip(texto));
                    }
                }
            }
        });
    }
}