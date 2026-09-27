package com.inventystamp.controller;

import com.inventystamp.dao.DemandaPedidoDAO;
import com.inventystamp.dao.PedidoDAO;
import com.inventystamp.model.DemandaPedido;
import com.inventystamp.model.Pedido;
import com.inventystamp.model.SituacaoPedido;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class DemandaController {

    private final DemandaPedidoDAO demandaPedidoDAO = new DemandaPedidoDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();

    public List<DemandaPedido> listarDemandas() {
        return demandaPedidoDAO.listarTodas();
    }

    public String salvarNovaDemanda(int pedidoId, String texto, String destinatario) {
        if (texto == null || texto.isBlank() || destinatario == null) {
            return "⚠ Preencha o texto e o destinatário da demanda.";
        }
        DemandaPedido d = new DemandaPedido();
        d.setPedidoId(pedidoId);
        d.setTexto(texto.trim());
        d.setDestinatario(destinatario);
        demandaPedidoDAO.salvar(d);
        return null;
    }

    public void editarDemanda(DemandaPedido d, String novoTexto, String novoDestinatario) {
        d.setTexto(novoTexto.trim());
        d.setDestinatario(novoDestinatario);
        demandaPedidoDAO.atualizar(d);
    }

    public void removerDemanda(int id) {
        demandaPedidoDAO.deletar(id);
    }

    public List<Pedido> buscarPedidosParaVincular(String textoBusca, DateTimeFormatter fmt) {
        String texto = textoBusca == null ? "" : textoBusca.toLowerCase().trim();
        return pedidoDAO.listarTodos().stream()
                .filter(p -> p.getSituacao() != SituacaoPedido.PRONTO)
                .filter(p -> {
                    if (texto.isBlank()) return true;
                    String nome = p.getClienteNome() != null ? p.getClienteNome().toLowerCase() : "";
                    String telefone = p.getClienteTelefone() != null ? p.getClienteTelefone().replaceAll("[^0-9]", "") : "";
                    String codigo = telefone.length() >= 4 ? telefone.substring(telefone.length() - 4) : "";
                    String data = p.getDataEntrega() != null ? p.getDataEntrega().format(fmt).toLowerCase() : "";
                    return nome.contains(texto) || codigo.contains(texto) || data.contains(texto);
                })
                .sorted((a, b) -> a.getDataEntrega().compareTo(b.getDataEntrega()))
                .toList();
    }
}