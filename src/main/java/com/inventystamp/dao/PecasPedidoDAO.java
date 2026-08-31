package com.inventystamp.dao;

import com.inventystamp.util.ConexaoDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PecasPedidoDAO {

    public void salvar(int ano, int mes, int quantidadePecas) {
        String sql = "INSERT INTO pecas_por_pedido (ano, mes, quantidade_pecas) VALUES (?, ?, ?)";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ano);
            ps.setInt(2, mes);
            ps.setInt(3, quantidadePecas);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Integer> listarPorMes(int ano, int mes) {
        List<Integer> lista = new ArrayList<>();
        String sql = "SELECT quantidade_pecas FROM pecas_por_pedido WHERE ano = ? AND mes = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ano);
            ps.setInt(2, mes);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(rs.getInt("quantidade_pecas"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}