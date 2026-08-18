package com.inventystamp.model;

import java.time.LocalDateTime;

public class DemandaPedido {
    private int id;
    private int pedidoId;
    private String destinatario;
    private String texto;
    private LocalDateTime criadoEm;

    // Dados do pedido vinculado, usados só para exibição nos cards
    private String pedidoClienteNome;
    private String pedidoClienteTelefone;
    private java.time.LocalDate pedidoDataEntrega;

    public DemandaPedido() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPedidoId() { return pedidoId; }
    public void setPedidoId(int pedidoId) { this.pedidoId = pedidoId; }

    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public String getPedidoClienteNome() { return pedidoClienteNome; }
    public void setPedidoClienteNome(String pedidoClienteNome) { this.pedidoClienteNome = pedidoClienteNome; }

    public String getPedidoClienteTelefone() { return pedidoClienteTelefone; }
    public void setPedidoClienteTelefone(String pedidoClienteTelefone) { this.pedidoClienteTelefone = pedidoClienteTelefone; }

    public java.time.LocalDate getPedidoDataEntrega() { return pedidoDataEntrega; }
    public void setPedidoDataEntrega(java.time.LocalDate pedidoDataEntrega) { this.pedidoDataEntrega = pedidoDataEntrega; }
}