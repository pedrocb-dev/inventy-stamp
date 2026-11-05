package com.inventystamp.dao;

import com.inventystamp.model.RascunhoPedido;
import com.inventystamp.util.ConexaoDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RascunhoDAO {

    public List<RascunhoPedido> listarTodos() {
        List<RascunhoPedido> lista = new ArrayList<>();
        String sql = "SELECT * FROM rascunhos_pedido ORDER BY criado_em DESC";

        try (Connection conn = ConexaoDB.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    public void salvar(RascunhoPedido r) {
        String sql = "INSERT INTO rascunhos_pedido (cliente_nome, cliente_telefone, anotacao, " +
                "tipo_personalizacao, data_confirmacao, data_entrega, valor, status_pagamento, resta, precisa_nf, atendente, demanda_texto, demanda_destinatario, precisa_cupomfiscal) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, r.getClienteNome());
            ps.setString(2, r.getClienteTelefone());
            ps.setString(3, r.getAnotacao());
            ps.setString(4, r.getTipoPersonalizacao());
            ps.setString(5, r.getDataConfirmacao());
            ps.setString(6, r.getDataEntrega());
            ps.setString(7, r.getValor());
            ps.setString(8, r.getStatusPagamento());
            ps.setString(9, r.getResta());
            ps.setBoolean(10, r.isPrecisaNf());
            ps.setString(11, r.getAtendente());
            ps.setString(12, r.getDemandaTexto());
            ps.setString(13, r.getDemandaDestinatario());
            ps.setBoolean(14, r.isPrecisaCupomFiscal());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) r.setId(rs.getInt(1));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletar(int id) {
        String sql = "DELETE FROM rascunhos_pedido WHERE id=?";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private RascunhoPedido mapear(ResultSet rs) throws SQLException {
        RascunhoPedido r = new RascunhoPedido();
        r.setId(rs.getInt("id"));
        r.setClienteNome(rs.getString("cliente_nome"));
        r.setClienteTelefone(rs.getString("cliente_telefone"));
        r.setAnotacao(rs.getString("anotacao"));
        r.setTipoPersonalizacao(rs.getString("tipo_personalizacao"));
        r.setDataConfirmacao(rs.getString("data_confirmacao"));
        r.setDataEntrega(rs.getString("data_entrega"));
        r.setValor(rs.getString("valor"));
        r.setStatusPagamento(rs.getString("status_pagamento"));
        r.setResta(rs.getString("resta"));
        r.setPrecisaNf(rs.getBoolean("precisa_nf"));
        r.setAtendente(rs.getString("atendente"));
        r.setDemandaTexto(rs.getString("demanda_texto"));
        r.setDemandaDestinatario(rs.getString("demanda_destinatario"));
        r.setPrecisaCupomFiscal(rs.getBoolean("precisa_cupomfiscal"));
        r.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        return r;
    }
}