package com.inventystamp.controller;

import com.inventystamp.dao.EstatisticaDAO;
import com.inventystamp.dao.PecasPedidoDAO;
import com.inventystamp.model.EstatisticasMes;
import com.inventystamp.model.Gasto;

import java.math.BigDecimal;
import java.util.List;

public class EstatisticaController {

    private final EstatisticaDAO estatisticaDAO = new EstatisticaDAO();
    private final PecasPedidoDAO pecasPedidoDAO = new PecasPedidoDAO();

    public List<EstatisticasMes> listarEstatisticas() {
        return estatisticaDAO.listarTodos();
    }

    public List<Gasto> listarGastos(int estatisticaId) {
        return estatisticaDAO.listarGastos(estatisticaId);
    }

    public List<Integer> listarPecasDoMes(int ano, int mes) {
        return pecasPedidoDAO.listarPorMes(ano, mes);
    }

    public String adicionarGasto(int estatisticaId, String categoria, String textoValor) {
        if (categoria == null || categoria.isBlank() || textoValor == null || textoValor.isBlank()) {
            return null;
        }
        Gasto g = new Gasto();
        g.setEstatisticaId(estatisticaId);
        g.setCategoria(categoria.trim());
        try {
            g.setValor(new BigDecimal(textoValor.replace(",", ".")));
        } catch (Exception ex) {
            return null;
        }
        estatisticaDAO.adicionarGasto(g);
        return "ok";
    }

    public void removerGasto(int id) {
        estatisticaDAO.deletarGasto(id);
    }

    public BigDecimal calcularTotalGastos(List<Gasto> gastos) {
        return gastos.stream().map(Gasto::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calcularLucroLiquido(BigDecimal lucroBruto, BigDecimal totalGastos) {
        return lucroBruto.subtract(totalGastos);
    }
}