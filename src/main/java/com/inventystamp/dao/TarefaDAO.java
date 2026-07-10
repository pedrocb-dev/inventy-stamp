package com.inventystamp.dao;

import com.inventystamp.model.*;
import com.inventystamp.util.ConexaoDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TarefaDAO {

    public List<Tarefa> listarPorPedido(int pedidoId) {
        List<Tarefa> tarefas = new ArrayList<>();
        String sql = "SELECT * FROM tarefas WHERE pedido_id = ?";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pedidoId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) tarefas.add(mapear(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tarefas;
    }

    public List<Tarefa> listarPorDestino(Perfil destino) {
        List<Tarefa> tarefas = new ArrayList<>();
        String sql = "SELECT * FROM tarefas WHERE responsavel_destino = ?";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, destino.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) tarefas.add(mapear(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tarefas;
    }

    public void salvar(Tarefa t) {
        String sql = "INSERT INTO tarefas (descricao, pedido_id, responsavel_destino, origem) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, t.getDescricao());
            if (t.getPedido() != null) ps.setInt(2, t.getPedido().getId());
            else ps.setNull(2, Types.INTEGER);
            ps.setString(3, t.getResponsavelDestino() != null ? t.getResponsavelDestino().name() : null);
            ps.setString(4, t.getOrigem().name());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) t.setId(rs.getInt(1));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void atualizar(Tarefa t) {
        String sql = "UPDATE tarefas SET descricao=?, concluida=? WHERE id=?";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.getDescricao());
            ps.setBoolean(2, t.isConcluida());
            ps.setInt(3, t.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletar(int id) {
        String sql = "DELETE FROM tarefas WHERE id=?";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Tarefa mapear(ResultSet rs) throws SQLException {
        Tarefa t = new Tarefa();
        t.setId(rs.getInt("id"));
        t.setDescricao(rs.getString("descricao"));
        t.setOrigem(OrigemTarefa.valueOf(rs.getString("origem")));
        t.setConcluida(rs.getBoolean("concluida"));
        t.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());

        String destino = rs.getString("responsavel_destino");
        if (destino != null) t.setResponsavelDestino(Perfil.valueOf(destino));

        return t;
    }
}