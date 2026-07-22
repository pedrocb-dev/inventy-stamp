package com.inventystamp.dao;

import com.inventystamp.model.*;
import com.inventystamp.util.ConexaoDB;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DemandaDAO {

    public int proximoNumeroDia(Perfil destinatario) {
        String sql = "SELECT COUNT(*) FROM demandas WHERE destinatario = ? AND DATE(criado_em) = CURDATE()";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, destinatario.name());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) + 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public List<Demanda> listarPorDestinatario(Perfil destinatario) {
        List<Demanda> demandas = new ArrayList<>();
        String sql = "SELECT * FROM demandas WHERE destinatario = ? ORDER BY criado_em DESC";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, destinatario.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) demandas.add(mapear(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return demandas;
    }

    public void salvar(Demanda d) {
        String sql = "INSERT INTO demandas (descricao, solicitante, destinatario, prazo_data, prazo_hora, numero_dia) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, d.getDescricao());
            ps.setString(2, d.getSolicitante().name());
            ps.setString(3, d.getDestinatario().name());
            ps.setDate(4, d.getPrazoData() != null ? Date.valueOf(d.getPrazoData()) : null);
            ps.setString(5, d.getPrazoHora());
            ps.setInt(6, d.getNumeroDia());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) d.setId(rs.getInt(1));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void atualizar(Demanda d) {
        String sql = "UPDATE demandas SET descricao=?, concluida=?, prazo_data=?, prazo_hora=? WHERE id=?";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, d.getDescricao());
            ps.setBoolean(2, d.isConcluida());
            ps.setDate(3, d.getPrazoData() != null ? Date.valueOf(d.getPrazoData()) : null);
            ps.setString(4, d.getPrazoHora());
            ps.setInt(5, d.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deletar(int id) {
        String sql = "DELETE FROM demandas WHERE id=?";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Demanda mapear(ResultSet rs) throws SQLException {
        Demanda d = new Demanda();
        d.setId(rs.getInt("id"));
        d.setDescricao(rs.getString("descricao"));
        d.setSolicitante(Perfil.valueOf(rs.getString("solicitante")));
        d.setDestinatario(Perfil.valueOf(rs.getString("destinatario")));
        d.setConcluida(rs.getBoolean("concluida"));
        d.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
        d.setNumeroDia(rs.getInt("numero_dia"));
        d.setPrazoHora(rs.getString("prazo_hora"));
        Date prazoData = rs.getDate("prazo_data");
        if (prazoData != null) d.setPrazoData(prazoData.toLocalDate());
        return d;
    }
}