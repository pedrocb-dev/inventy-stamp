package com.inventystamp.dao;

import com.inventystamp.model.EstatisticasMes;
import com.inventystamp.model.Gasto;
import com.inventystamp.util.ConexaoDB;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EstatisticaDAO {

    public EstatisticasMes buscarOuCriar(int ano, int mes) {
        String sql = "SELECT * FROM estatisticas_mes WHERE ano = ? AND mes = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ano);
            ps.setInt(2, mes);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void salvarOuAtualizar(EstatisticasMes e) {
        String sql = "INSERT INTO estatisticas_mes (ano, mes, lucro_bruto, lucro_bordado, lucro_dtf, " +
                "lucro_plotter, lucro_sublimacao, lucro_transfer, lucro_silkscreen, lucro_sem_personalizacao, " +
                "qtd_pequeno, qtd_medio, qtd_grande, qtd_bem_grande) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                "lucro_bruto = lucro_bruto + VALUES(lucro_bruto), " +
                "lucro_bordado = lucro_bordado + VALUES(lucro_bordado), " +
                "lucro_dtf = lucro_dtf + VALUES(lucro_dtf), " +
                "lucro_plotter = lucro_plotter + VALUES(lucro_plotter), " +
                "lucro_sublimacao = lucro_sublimacao + VALUES(lucro_sublimacao), " +
                "lucro_transfer = lucro_transfer + VALUES(lucro_transfer), " +
                "lucro_silkscreen = lucro_silkscreen + VALUES(lucro_silkscreen), " +
                "lucro_sem_personalizacao = lucro_sem_personalizacao + VALUES(lucro_sem_personalizacao), " +
                "qtd_pequeno = qtd_pequeno + VALUES(qtd_pequeno), " +
                "qtd_medio = qtd_medio + VALUES(qtd_medio), " +
                "qtd_grande = qtd_grande + VALUES(qtd_grande), " +
                "qtd_bem_grande = qtd_bem_grande + VALUES(qtd_bem_grande)";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.getAno());
            ps.setInt(2, e.getMes());
            ps.setBigDecimal(3, e.getLucroBruto());
            ps.setBigDecimal(4, e.getLucroBordado());
            ps.setBigDecimal(5, e.getLucroDtf());
            ps.setBigDecimal(6, e.getLucroPlotter());
            ps.setBigDecimal(7, e.getLucroSublimacao());
            ps.setBigDecimal(8, e.getLucroTransfer());
            ps.setBigDecimal(9, e.getLucroSilkscreen());
            ps.setBigDecimal(10, e.getLucroSemPersonalizacao());
            ps.setInt(11, e.getQtdPequeno());
            ps.setInt(12, e.getQtdMedio());
            ps.setInt(13, e.getQtdGrande());
            ps.setInt(14, e.getQtdBemGrande());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) e.setId(rs.getInt(1));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public List<EstatisticasMes> listarTodos() {
        List<EstatisticasMes> lista = new ArrayList<>();
        String sql = "SELECT * FROM estatisticas_mes ORDER BY ano ASC, mes ASC";
        try (Connection conn = ConexaoDB.getConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public void adicionarGasto(Gasto g) {
        String sql = "INSERT INTO gastos (estatistica_id, categoria, valor) VALUES (?, ?, ?)";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, g.getEstatisticaId());
            ps.setString(2, g.getCategoria());
            ps.setBigDecimal(3, g.getValor());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) g.setId(rs.getInt(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Gasto> listarGastos(int estatisticaId) {
        List<Gasto> gastos = new ArrayList<>();
        String sql = "SELECT * FROM gastos WHERE estatistica_id = ? ORDER BY criado_em ASC";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, estatisticaId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Gasto g = new Gasto();
                g.setId(rs.getInt("id"));
                g.setEstatisticaId(rs.getInt("estatistica_id"));
                g.setCategoria(rs.getString("categoria"));
                g.setValor(rs.getBigDecimal("valor"));
                g.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
                gastos.add(g);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return gastos;
    }

    public void deletarGasto(int id) {
        String sql = "DELETE FROM gastos WHERE id = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private EstatisticasMes mapear(ResultSet rs) throws SQLException {
        EstatisticasMes e = new EstatisticasMes();
        e.setId(rs.getInt("id"));
        e.setAno(rs.getInt("ano"));
        e.setMes(rs.getInt("mes"));
        e.setLucroBruto(rs.getBigDecimal("lucro_bruto"));
        e.setLucroBordado(rs.getBigDecimal("lucro_bordado"));
        e.setLucroDtf(rs.getBigDecimal("lucro_dtf"));
        e.setLucroPlotter(rs.getBigDecimal("lucro_plotter"));
        e.setLucroSublimacao(rs.getBigDecimal("lucro_sublimacao"));
        e.setLucroTransfer(rs.getBigDecimal("lucro_transfer"));
        e.setLucroSilkscreen(rs.getBigDecimal("lucro_silkscreen"));
        e.setLucroSemPersonalizacao(rs.getBigDecimal("lucro_sem_personalizacao"));
        e.setQtdPequeno(rs.getInt("qtd_pequeno"));
        e.setQtdMedio(rs.getInt("qtd_medio"));
        e.setQtdGrande(rs.getInt("qtd_grande"));
        e.setQtdBemGrande(rs.getInt("qtd_bem_grande"));
        return e;
    }
}