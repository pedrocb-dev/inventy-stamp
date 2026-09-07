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
                "lucro_sublimacao, lucro_silkscreen, lucro_brindes, qtd_pedidos_pecas_nossas, qtd_pedidos_pecas_cliente, qtd_pedidos_pecas_ambos) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                "lucro_bruto = lucro_bruto + VALUES(lucro_bruto), " +
                "lucro_bordado = lucro_bordado + VALUES(lucro_bordado), " +
                "lucro_dtf = lucro_dtf + VALUES(lucro_dtf), " +
                "lucro_sublimacao = lucro_sublimacao + VALUES(lucro_sublimacao), " +
                "lucro_silkscreen = lucro_silkscreen + VALUES(lucro_silkscreen), " +
                "lucro_brindes = lucro_brindes + VALUES(lucro_brindes), " +
                "qtd_pedidos_pecas_nossas = qtd_pedidos_pecas_nossas + VALUES(qtd_pedidos_pecas_nossas), " +
                "qtd_pedidos_pecas_cliente = qtd_pedidos_pecas_cliente + VALUES(qtd_pedidos_pecas_cliente), " +
                "qtd_pedidos_pecas_ambos = qtd_pedidos_pecas_ambos + VALUES(qtd_pedidos_pecas_ambos)";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.getAno());
            ps.setInt(2, e.getMes());
            ps.setBigDecimal(3, e.getLucroBruto());
            ps.setBigDecimal(4, e.getLucroBordado());
            ps.setBigDecimal(5, e.getLucroDtf());
            ps.setBigDecimal(6, e.getLucroSublimacao());
            ps.setBigDecimal(7, e.getLucroSilkscreen());
            ps.setBigDecimal(8, e.getLucroBrindes());
            ps.setInt(9, e.getQtdPedidosPecasNossas());
            ps.setInt(10, e.getQtdPedidosPecasCliente());
            ps.setInt(11, e.getQtdPedidosPecasAmbos());
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
        e.setLucroSublimacao(rs.getBigDecimal("lucro_sublimacao"));
        e.setLucroSilkscreen(rs.getBigDecimal("lucro_silkscreen"));
        e.setLucroBrindes(rs.getBigDecimal("lucro_brindes"));
        e.setQtdPedidosPecasNossas(rs.getInt("qtd_pedidos_pecas_nossas"));
        e.setQtdPedidosPecasCliente(rs.getInt("qtd_pedidos_pecas_cliente"));
        e.setQtdPedidosPecasAmbos(rs.getInt("qtd_pedidos_pecas_ambos"));
        return e;
    }
}