package com.inventystamp.model;

import java.time.LocalDateTime;

public class RascunhoPedido {
    private int id;
    private String clienteNome;
    private String clienteTelefone;
    private String anotacao;
    private String tipoPersonalizacao;
    private String dataConfirmacao;
    private String dataEntrega;
    private String valor;
    private String statusPagamento;
    private String resta;
    private boolean precisaNf;
    private String atendente;
    private String tamanhoPedido;
    private String demandaTexto;
    private String demandaDestinatario;
    private boolean precisaCupomFiscal;
    private LocalDateTime criadoEm;

    public RascunhoPedido() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }

    public String getClienteTelefone() { return clienteTelefone; }
    public void setClienteTelefone(String clienteTelefone) { this.clienteTelefone = clienteTelefone; }

    public String getAnotacao() { return anotacao; }
    public void setAnotacao(String anotacao) { this.anotacao = anotacao; }

    public String getTipoPersonalizacao() { return tipoPersonalizacao; }
    public void setTipoPersonalizacao(String tipoPersonalizacao) { this.tipoPersonalizacao = tipoPersonalizacao; }

    public String getDataConfirmacao() { return dataConfirmacao; }
    public void setDataConfirmacao(String dataConfirmacao) { this.dataConfirmacao = dataConfirmacao; }

    public String getDataEntrega() { return dataEntrega; }
    public void setDataEntrega(String dataEntrega) { this.dataEntrega = dataEntrega; }

    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }

    public String getStatusPagamento() { return statusPagamento; }
    public void setStatusPagamento(String statusPagamento) { this.statusPagamento = statusPagamento; }

    public String getResta() { return resta; }
    public void setResta(String resta) { this.resta = resta; }

    public boolean isPrecisaNf() { return precisaNf; }
    public void setPrecisaNf(boolean precisaNf) { this.precisaNf = precisaNf; }

    public String getAtendente() { return atendente; }
    public void setAtendente(String atendente) { this.atendente = atendente; }

    public String getTamanhoPedido() { return tamanhoPedido; }
    public void setTamanhoPedido(String tamanhoPedido) { this.tamanhoPedido = tamanhoPedido; }

    public String getDemandaTexto() { return demandaTexto; }
    public void setDemandaTexto(String demandaTexto) { this.demandaTexto = demandaTexto; }

    public boolean isPrecisaCupomFiscal() { return precisaCupomFiscal; }
    public void setPrecisaCupomFiscal(boolean precisaCupomFiscal) { this.precisaCupomFiscal = precisaCupomFiscal; }

    public String getDemandaDestinatario() { return demandaDestinatario; }
    public void setDemandaDestinatario(String demandaDestinatario) { this.demandaDestinatario = demandaDestinatario; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
