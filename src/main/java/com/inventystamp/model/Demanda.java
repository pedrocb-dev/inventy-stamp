package com.inventystamp.model;

import java.time.LocalDateTime;

public class Demanda {
    private int id;
    private String descricao;
    private Perfil solicitante;
    private Perfil destinatario;
    private boolean concluida;
    private LocalDateTime criadoEm;

    public Demanda() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Perfil getSolicitante() { return solicitante; }
    public void setSolicitante(Perfil solicitante) { this.solicitante = solicitante; }

    public Perfil getDestinatario() { return destinatario; }
    public void setDestinatario(Perfil destinatario) { this.destinatario = destinatario; }

    public boolean isConcluida() { return concluida; }
    public void setConcluida(boolean concluida) { this.concluida = concluida; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}