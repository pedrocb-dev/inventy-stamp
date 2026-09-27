package com.inventystamp.controller;

import com.inventystamp.dao.DemandaPedidoDAO;
import com.inventystamp.dao.PedidoDAO;
import com.inventystamp.model.*;

import java.math.BigDecimal;

public class PedidoController {

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final DemandaPedidoDAO demandaPedidoDAO = new DemandaPedidoDAO();

    public static class ResultadoSalvar {
        public final boolean sucesso;
        public final String mensagem;

        public ResultadoSalvar(boolean sucesso, String mensagem) {
            this.sucesso = sucesso;
            this.mensagem = mensagem;
        }
    }

    public ResultadoSalvar salvarPedido(
            String nome, String telefone, String anotacao,
            TipoPersonalizacao tipo, java.time.LocalDate dataConfirmacao, java.time.LocalDate dataEntrega,
            StatusPagamento statusPagamento, String textoValor, boolean valorACombinar, String textoResta,
            boolean precisaNf, boolean precisaCupomFiscal, String atendente,
            String demandaTexto, String demandaDestinatario
    ) {
        if (nome == null || nome.isBlank() || tipo == null || dataEntrega == null || statusPagamento == null) {
            return new ResultadoSalvar(false, "⚠ Preencha todos os campos obrigatórios (*).");
        }

        Pedido p = new Pedido();
        p.setClienteNome(nome.trim());
        p.setClienteTelefone(telefone != null ? telefone.trim() : "");
        p.setAnotacao(anotacao != null ? anotacao.trim() : "");
        p.setTipoPersonalizacao(tipo);
        p.setDataConfirmacao(dataConfirmacao);
        p.setDataEntrega(dataEntrega);
        p.setStatusPagamento(statusPagamento);
        p.setSituacao(SituacaoPedido.PREPARANDO);
        p.setPrecisaNf(precisaNf);
        p.setPrecisaCupomFiscal(precisaCupomFiscal);
        p.setAtendente(atendente);
        p.setValorACombinar(valorACombinar);

        if (!valorACombinar) {
            try {
                if (textoValor != null && !textoValor.isBlank())
                    p.setValor(new BigDecimal(textoValor.replace(",", ".")));
            } catch (Exception ex) {
                return new ResultadoSalvar(false, "⚠ Valor inválido. Use o formato: 65.00");
            }
        }

        try {
            if (statusPagamento == StatusPagamento.PARCIAL && textoResta != null && !textoResta.isBlank())
                p.setResta(new BigDecimal(textoResta.replace(",", ".")));
        } catch (Exception ex) {
            return new ResultadoSalvar(false, "⚠ Valor de 'Resta' inválido. Use o formato: 32.50");
        }

        pedidoDAO.salvar(p);

        if (demandaTexto != null && !demandaTexto.isBlank() && demandaDestinatario != null) {
            DemandaPedido demanda = new DemandaPedido();
            demanda.setPedidoId(p.getId());
            demanda.setDestinatario(demandaDestinatario);
            demanda.setTexto(demandaTexto.trim());
            demandaPedidoDAO.salvar(demanda);
        }

        return new ResultadoSalvar(true, "✅ Pedido salvo com sucesso!");
    }

    public String validarConclusao(Pedido p) {
        if (p.isValorACombinar() || p.getValor() == null || p.getValor().compareTo(BigDecimal.ZERO) == 0) {
            return "⚠ Informe o valor do pedido antes de concluir.";
        }
        return null;
    }

    public String validarQuantidadePecas(int quantidadePecas) {
        if (quantidadePecas < 1) {
            return "⚠ A quantidade de peças deve ser pelo menos 1.";
        }
        return null;
    }

    public void concluirPedido(Pedido p, int quantidadePecas) {
        p.setSituacao(SituacaoPedido.PRONTO);
        pedidoDAO.atualizar(p);

        com.inventystamp.dao.PecasPedidoDAO pecasDAO = new com.inventystamp.dao.PecasPedidoDAO();
        pecasDAO.salvar(java.time.LocalDate.now().getYear(), java.time.LocalDate.now().getMonthValue(), quantidadePecas);

        if (p.getValor() != null) {
            com.inventystamp.dao.EstatisticaDAO estatisticaDAO = new com.inventystamp.dao.EstatisticaDAO();
            EstatisticasMes est = new EstatisticasMes();
            est.setAno(java.time.LocalDate.now().getYear());
            est.setMes(java.time.LocalDate.now().getMonthValue());
            est.setLucroBruto(p.getValor());
            est.setLucroBordado(BigDecimal.ZERO);
            est.setLucroDtf(BigDecimal.ZERO);
            est.setLucroSublimacao(BigDecimal.ZERO);
            est.setLucroSilkscreen(BigDecimal.ZERO);
            est.setLucroBrindes(BigDecimal.ZERO);

            switch (p.getTipoPersonalizacao()) {
                case BORDADO -> est.setLucroBordado(p.getValor());
                case DTF -> est.setLucroDtf(p.getValor());
                case SUBLIMACAO -> est.setLucroSublimacao(p.getValor());
                case SILKSCREEN -> est.setLucroSilkscreen(p.getValor());
                case BRINDES -> est.setLucroBrindes(p.getValor());
            }

            estatisticaDAO.salvarOuAtualizar(est);
        }
    }

    public void editarPedido(Pedido p) {
        pedidoDAO.atualizar(p);
    }

    public void removerPedido(int id) {
        pedidoDAO.deletar(id);
    }

    public java.util.List<Pedido> listarPedidos() {
        return pedidoDAO.listarTodos();
    }
}