package com.inventystamp.view;

import com.inventystamp.controller.EstatisticaController;
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

    private final EstatisticaController estatisticaController = new EstatisticaController();
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

        estatisticas = estatisticaController.listarEstatisticas();

        if (estatisticas.isEmpty()) {
            Label vazio = new Label("Nenhuma estatística disponível ainda.\nConclua um pedido para começar.");
            vazio.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-text-alignment: center;");
            vazio.setWrapText(true);
            root.getChildren().addAll(titulo, vazio);
            return;
        }

        indiceAtual = estatisticas.size() - 1;

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

        // Gráfico de barras — evolução do lucro bruto
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

        // Gráfico de pizza — lucro por personalização
        Label lblGraficoPersonalizacao = new Label("📊 Lucro por Personalização");
        lblGraficoPersonalizacao.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        PieChart graficoPersonalizacao = new PieChart();
        graficoPersonalizacao.setPrefSize(500, 320);
        graficoPersonalizacao.setMinSize(500, 320);
        graficoPersonalizacao.setMaxSize(500, 320);
        graficoPersonalizacao.setLegendVisible(true);
        graficoPersonalizacao.setLabelsVisible(true);

        adicionarFatiaLucro(graficoPersonalizacao, "Bordado", e.getLucroBordado());
        adicionarFatiaLucro(graficoPersonalizacao, "DTF", e.getLucroDtf());
        adicionarFatiaLucro(graficoPersonalizacao, "Sublimação", e.getLucroSublimacao());
        adicionarFatiaLucro(graficoPersonalizacao, "Silkscreen", e.getLucroSilkscreen());
        adicionarFatiaLucro(graficoPersonalizacao, "Brindes", e.getLucroBrindes());
        instalarTooltipsPizza(graficoPersonalizacao);

        VBox blocoPersonalizacao = new VBox(10, lblGraficoPersonalizacao, graficoPersonalizacao);
        blocoPersonalizacao.setPadding(new Insets(20));
        blocoPersonalizacao.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(blocoPersonalizacao, Priority.ALWAYS);
        blocoPersonalizacao.setStyle(
                "-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                        "-fx-border-color: #dcdde1; -fx-border-radius: 10;"
        );

        // Histograma de peças por pedido
        Label lblGraficoPecas = new Label("📦 Peças por Pedido");
        lblGraficoPecas.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        CategoryAxis xAxisPecas = new CategoryAxis();
        NumberAxis yAxisPecas = new NumberAxis();
        yAxisPecas.setLabel("Qtd. de pedidos");

        BarChart<String, Number> graficoPecas = new BarChart<>(xAxisPecas, yAxisPecas);
        graficoPecas.setPrefSize(500, 320);
        graficoPecas.setMinSize(500, 320);
        graficoPecas.setMaxSize(500, 320);
        graficoPecas.setLegendVisible(false);

        List<Integer> pecasDoMes = estatisticaController.listarPecasDoMes(e.getAno(), e.getMes());

        int[] faixas = new int[6]; // 1-10, 11-20, 21-30, 31-40, 41-50, 51+
        for (int qtd : pecasDoMes) {
            if (qtd <= 10) faixas[0]++;
            else if (qtd <= 20) faixas[1]++;
            else if (qtd <= 30) faixas[2]++;
            else if (qtd <= 40) faixas[3]++;
            else if (qtd <= 50) faixas[4]++;
            else faixas[5]++;
        }

        String[] nomesFaixas = {"1-10", "11-20", "21-30", "31-40", "41-50", "51+"};
        XYChart.Series<String, Number> seriePecas = new XYChart.Series<>();
        for (int i = 0; i < faixas.length; i++) {
            seriePecas.getData().add(new XYChart.Data<>(nomesFaixas[i], faixas[i]));
        }
        graficoPecas.getData().add(seriePecas);
        instalarTooltipsBarras(graficoPecas, false);

        VBox blocoPecas = new VBox(10, lblGraficoPecas, graficoPecas);
        blocoPecas.setPadding(new Insets(20));
        blocoPecas.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(blocoPecas, Priority.ALWAYS);
        blocoPecas.setStyle(
                "-fx-background-color: #f8f9fa; -fx-background-radius: 10; " +
                        "-fx-border-color: #dcdde1; -fx-border-radius: 10;"
        );

        HBox linhaGraficos = new HBox(40, blocoPersonalizacao, blocoPecas);
        linhaGraficos.setAlignment(Pos.TOP_LEFT);
        linhaGraficos.setMaxWidth(Double.MAX_VALUE);

        Separator sep1 = new Separator();
        Separator sep2 = new Separator();

        // Lucro bruto e líquido
        Label lblLucroBruto = new Label("💰 Lucro Bruto: R$ " +
                e.getLucroBruto().setScale(2, RoundingMode.HALF_UP));
        lblLucroBruto.setStyle("-fx-font-size: 15px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");

        List<Gasto> gastos = estatisticaController.listarGastos(e.getId());
        BigDecimal totalGastos = estatisticaController.calcularTotalGastos(gastos);
        BigDecimal lucroLiquido = estatisticaController.calcularLucroLiquido(e.getLucroBruto(), totalGastos);

        Label lblLucroLiquido = new Label("💵 Lucro Líquido: R$ " +
                lucroLiquido.setScale(2, RoundingMode.HALF_UP));
        lblLucroLiquido.setStyle("-fx-font-size: 15px; -fx-text-fill: #2980b9; -fx-font-weight: bold;");

        Label lblTotalGastos = new Label("💸 Total de Gastos: R$ " +
                totalGastos.setScale(2, RoundingMode.HALF_UP));
        lblTotalGastos.setStyle("-fx-font-size: 15px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        Button btnAdicionarGasto = new Button("➕ Adicionar Gasto");
        btnAdicionarGasto.setStyle(
                "-fx-background-color: #e67e22; -fx-text-fill: white; " +
                        "-fx-background-radius: 6; -fx-font-size: 13px;"
        );

        VBox listaGastos = new VBox(6);
        listaGastos.setPadding(new Insets(8, 0, 0, 0));
        atualizarListaGastos(listaGastos, gastos, e, lblLucroLiquido, lblTotalGastos);

        btnAdicionarGasto.setOnAction(ev -> abrirDialogGasto(e, lblLucroLiquido, lblTotalGastos, gastos));

        HBox rodape = new HBox(20, lblLucroBruto, lblLucroLiquido, lblTotalGastos, btnAdicionarGasto);
        rodape.setAlignment(Pos.CENTER_LEFT);

        cardAtual.getChildren().addAll(
                lblGraficoLinha, lineChart,
                sep1,
                linhaGraficos,
                sep2,
                rodape, listaGastos
        );
    }

    private void abrirDialogGasto(EstatisticasMes e, Label lblLucroLiquido, Label lblTotalGastos, List<Gasto> gastos) {
        Dialog<ButtonType> dialog = new Dialog<>();
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

        dialog.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;

            String resultado = estatisticaController.adicionarGasto(e.getId(), tfCategoria.getText(), tfValor.getText());
            if (resultado == null) return;

            List<Gasto> gastosAtualizados = estatisticaController.listarGastos(e.getId());
            gastos.clear();
            gastos.addAll(gastosAtualizados);

            BigDecimal totalGastosAtualizado = estatisticaController.calcularTotalGastos(gastos);
            BigDecimal lucroLiquido = estatisticaController.calcularLucroLiquido(e.getLucroBruto(), totalGastosAtualizado);
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
                estatisticaController.removerGasto(g.getId());
                gastos.remove(g);

                BigDecimal totalGastosAtualizado = estatisticaController.calcularTotalGastos(gastos);
                BigDecimal lucroLiquido = estatisticaController.calcularLucroLiquido(e.getLucroBruto(), totalGastosAtualizado);
                lblLucroLiquido.setText("💵 Lucro Líquido: R$ " + lucroLiquido.setScale(2, RoundingMode.HALF_UP));
                lblTotalGastos.setText("💸 Total de Gastos: R$ " + totalGastosAtualizado.setScale(2, RoundingMode.HALF_UP));

                atualizarListaGastos(listaGastos, gastos, e, lblLucroLiquido, lblTotalGastos);
            });

            linha.getChildren().addAll(lblGasto, spacer, btnRemover);
            listaGastos.getChildren().add(linha);
        }
    }

    private void adicionarFatiaLucro(PieChart grafico, String nome, BigDecimal valor) {
        if (valor != null && valor.compareTo(BigDecimal.ZERO) > 0) {
            grafico.getData().add(new PieChart.Data(nome, valor.doubleValue()));
        }
    }

    private void instalarTooltipsPizza(PieChart grafico) {
        double total = grafico.getData().stream().mapToDouble(PieChart.Data::getPieValue).sum();
        javafx.application.Platform.runLater(() -> {
            for (PieChart.Data fatia : grafico.getData()) {
                if (fatia.getNode() != null) {
                    double porcentagem = fatia.getPieValue() / total * 100;
                    String texto = String.format("R$ %.2f (%.1f%%)", fatia.getPieValue(), porcentagem);
                    Tooltip.install(fatia.getNode(), new Tooltip(texto));
                }
            }
        });
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

    public VBox getView() {
        return root;
    }
}