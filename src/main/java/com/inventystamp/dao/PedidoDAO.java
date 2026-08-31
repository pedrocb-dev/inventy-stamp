package com.inventystamp.dao;

import com.inventystamp.model.*;
import com.inventystamp.util.ConexaoDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    public List<Pedido> listarTodos() {
        List<Pedido> pedidos = new ArrayList<>();
        String sql = "SELECT * FROM pedidos ORDER BY data_entrega ASC";

        try (Connection conn = ConexaoDB.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                pedidos.add(mapear(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pedidos;
    }

    public void salvar(Pedido p) {
        String sql = "INSERT INTO pedidos (cliente_nome, cliente_telefone, anotacao, tipo_personalizacao, " +
                "data_confirmacao, data_entrega, valor, status_pagamento, situacao, resta, precisa_nf, dtf_montado, atendente, tamanho_pedido, precisa_cupomfiscal, valor_a_combinar) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getClienteNome());
            ps.setString(2, p.getClienteTelefone());
            ps.setString(3, p.getAnotacao());
            ps.setString(4, p.getTipoPersonalizacao().name());
            ps.setDate(5, Date.valueOf(p.getDataConfirmacao()));
            ps.setDate(6, Date.valueOf(p.getDataEntrega()));
            ps.setBigDecimal(7, p.getValor());
            ps.setString(8, p.getStatusPagamento().name());
            ps.setString(9, p.getSituacao().name());
            ps.setBigDecimal(10, p.getResta());
            ps.setBoolean(11, p.isPrecisaNf());
            ps.setBoolean(12, p.isDtfMontado());
            ps.setString(13, p.getAtendente());
            ps.setString(14, p.getTamanhoPedido());
            ps.setBoolean(15, p.isPrecisaCupomFiscal());
            ps.setBoolean(16, p.isValorACombinar());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) p.setId(rs.getInt(1));

        } catch (SQLException e) {
            System.out.println("ERRO ao salvar pedido: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void atualizar(Pedido p) {
        String sql = "UPDATE pedidos SET cliente_nome=?, cliente_telefone=?, anotacao=?, tipo_personalizacao=?, " +
                "data_confirmacao=?, data_entrega=?, valor=?, status_pagamento=?, situacao=?, foto_conclusao=?, resta=?, precisa_nf=?, dtf_montado=?, atendente=?, tamanho_pedido=?, precisa_cupomfiscal=?, valor_a_combinar=? WHERE id=?";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getClienteNome());
            ps.setString(2, p.getClienteTelefone());
            ps.setString(3, p.getAnotacao());
            ps.setString(4, p.getTipoPersonalizacao().name());
            ps.setDate(5, Date.valueOf(p.getDataConfirmacao()));
            ps.setDate(6, Date.valueOf(p.getDataEntrega()));
            ps.setBigDecimal(7, p.getValor());
            ps.setString(8, p.getStatusPagamento().name());
            ps.setString(9, p.getSituacao().name());
            ps.setString(10, p.getFotoConclusao());
            ps.setBigDecimal(11, p.getResta());
            ps.setBoolean(12, p.isPrecisaNf());
            ps.setBoolean(13, p.isDtfMontado());
            ps.setString(14, p.getAtendente());
            ps.setString(15, p.getTamanhoPedido());
            ps.setBoolean(16, p.isPrecisaCupomFiscal());
            ps.setBoolean(17, p.isValorACombinar());
            ps.setInt(18, p.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletar(int id) {
        String sql = "DELETE FROM pedidos WHERE id=?";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Pedido mapear(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setId(rs.getInt("id"));
        p.setClienteNome(rs.getString("cliente_nome"));
        p.setClienteTelefone(rs.getString("cliente_telefone"));
        p.setAnotacao(rs.getString("anotacao"));
        p.setTipoPersonalizacao(TipoPersonalizacao.valueOf(rs.getString("tipo_personalizacao")));
        p.setDataConfirmacao(rs.getDate("data_confirmacao").toLocalDate());
        p.setDataEntrega(rs.getDate("data_entrega").toLocalDate());
        p.setValor(rs.getBigDecimal("valor"));
        p.setStatusPagamento(StatusPagamento.valueOf(rs.getString("status_pagamento")));
        p.setSituacao(SituacaoPedido.valueOf(rs.getString("situacao")));
        p.setFotoConclusao(rs.getString("foto_conclusao"));
        p.setResta(rs.getBigDecimal("resta"));
        p.setPrecisaNf(rs.getBoolean("precisa_nf"));
        p.setDtfMontado(rs.getBoolean("dtf_montado"));
        p.setAtendente(rs.getString("atendente"));
        p.setTamanhoPedido(rs.getString("tamanho_pedido"));
        p.setPrecisaCupomFiscal(rs.getBoolean("precisa_cupomfiscal"));
        p.setValorACombinar(rs.getBoolean("valor_a_combinar"));
        p.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        return p;
    }
}