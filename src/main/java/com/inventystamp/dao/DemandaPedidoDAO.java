package com.inventystamp.dao;

import com.inventystamp.model.DemandaPedido;
import com.inventystamp.util.ConexaoDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DemandaPedidoDAO {

    public List<DemandaPedido> listarTodas() {
        List<DemandaPedido> lista = new ArrayList<>();
        String sql = "SELECT d.*, p.cliente_nome, p.cliente_telefone, p.data_entrega " +
                "FROM demandas_pedido d " +
                "JOIN pedidos p ON d.pedido_id = p.id " +
                "ORDER BY p.data_entrega ASC";

        try (Connection conn = ConexaoDB.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    public void salvar(DemandaPedido d) {
        String sql = "INSERT INTO demandas_pedido (pedido_id, destinatario, texto) VALUES (?, ?, ?)";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, d.getPedidoId());
            ps.setString(2, d.getDestinatario());
            ps.setString(3, d.getTexto());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) d.setId(rs.getInt(1));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void atualizar(DemandaPedido d) {
        String sql = "UPDATE demandas_pedido SET destinatario=?, texto=? WHERE id=?";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, d.getDestinatario());
            ps.setString(2, d.getTexto());
            ps.setInt(3, d.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletar(int id) {
        String sql = "DELETE FROM demandas_pedido WHERE id=?";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int contarTodas() {
        String sql = "SELECT COUNT(*) FROM demandas_pedido";
        try (Connection conn = ConexaoDB.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private DemandaPedido mapear(ResultSet rs) throws SQLException {
        DemandaPedido d = new DemandaPedido();
        d.setId(rs.getInt("id"));
        d.setPedidoId(rs.getInt("pedido_id"));
        d.setDestinatario(rs.getString("destinatario"));
        d.setTexto(rs.getString("texto"));
        d.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        d.setPedidoClienteNome(rs.getString("cliente_nome"));
        d.setPedidoClienteTelefone(rs.getString("cliente_telefone"));
        d.setPedidoDataEntrega(rs.getDate("data_entrega").toLocalDate());
        return d;
    }
}