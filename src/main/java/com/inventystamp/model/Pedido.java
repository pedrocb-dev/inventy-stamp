package com.inventystamp.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Pedido {
    private int id;
    private String clienteNome;
    private String clienteTelefone;
    private String anotacao;
    private TipoPersonalizacao tipoPersonalizacao;
    private LocalDate dataConfirmacao;
    private LocalDate dataEntrega;
    private BigDecimal valor;
    private StatusPagamento statusPagamento;
    private SituacaoPedido situacao;
    private String fotoConclusao;
    private java.math.BigDecimal resta;
    private LocalDateTime criadoEm;
    private boolean precisaNf;
    private boolean dtfMontado;
    private String atendente;
    private String tamanhoPedido;
    private boolean precisaNotinha;

    public Pedido() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getClienteNome() { return clienteNome; }
    public void setClienteNome(String clienteNome) { this.clienteNome = clienteNome; }

    public String getClienteTelefone() { return clienteTelefone; }
    public void setClienteTelefone(String clienteTelefone) { this.clienteTelefone = clienteTelefone; }

    public String getAnotacao() { return anotacao; }
    public void setAnotacao(String anotacao) { this.anotacao = anotacao; }

    public TipoPersonalizacao getTipoPersonalizacao() { return tipoPersonalizacao; }
    public void setTipoPersonalizacao(TipoPersonalizacao tipoPersonalizacao) { this.tipoPersonalizacao = tipoPersonalizacao; }

    public LocalDate getDataConfirmacao() { return dataConfirmacao; }
    public void setDataConfirmacao(LocalDate dataConfirmacao) { this.dataConfirmacao = dataConfirmacao; }

    public LocalDate getDataEntrega() { return dataEntrega; }
    public void setDataEntrega(LocalDate dataEntrega) { this.dataEntrega = dataEntrega; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public StatusPagamento getStatusPagamento() { return statusPagamento; }
    public void setStatusPagamento(StatusPagamento statusPagamento) { this.statusPagamento = statusPagamento; }

    public SituacaoPedido getSituacao() { return situacao; }
    public void setSituacao(SituacaoPedido situacao) { this.situacao = situacao; }

    public String getFotoConclusao() { return fotoConclusao; }
    public void setFotoConclusao(String fotoConclusao) { this.fotoConclusao = fotoConclusao; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public java.math.BigDecimal getResta() { return resta; }
    public void setResta(java.math.BigDecimal resta) { this.resta = resta; }

    public boolean isPrecisaNf() { return precisaNf; }
    public void setPrecisaNf(boolean precisaNf) { this.precisaNf = precisaNf; }

    public boolean isDtfMontado() { return dtfMontado; }
    public void setDtfMontado(boolean dtfMontado) { this.dtfMontado = dtfMontado; }

    public String getAtendente() { return atendente; }
    public void setAtendente(String atendente) { this.atendente = atendente; }

    public String getTamanhoPedido() { return tamanhoPedido; }
    public void setTamanhoPedido(String tamanhoPedido) { this.tamanhoPedido = tamanhoPedido; }

    public boolean isPrecisaNotinha() { return precisaNotinha; }
    public void setPrecisaNotinha(boolean precisaNotinha) { this.precisaNotinha = precisaNotinha; }
}