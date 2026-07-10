package com.inventystamp.model;

import java.time.LocalDateTime;

public class Tarefa {
    private int id;
    private String descricao;
    private Pedido pedido;
    private Perfil responsavelDestino;
    private OrigemTarefa origem;
    private boolean concluida;
    private LocalDateTime criadoEm;

    public Tarefa() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }

    public Perfil getResponsavelDestino() { return responsavelDestino; }
    public void setResponsavelDestino(Perfil responsavelDestino) { this.responsavelDestino = responsavelDestino; }

    public OrigemTarefa getOrigem() { return origem; }
    public void setOrigem(OrigemTarefa origem) { this.origem = origem; }

    public boolean isConcluida() { return concluida; }
    public void setConcluida(boolean concluida) { this.concluida = concluida; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}