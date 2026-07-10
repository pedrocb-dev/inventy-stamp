package com.inventystamp.model;

public class Usuario {
    private int id;
    private String nome;
    private Perfil perfil;

    public Usuario() {}

    public Usuario(int id, String nome, Perfil perfil) {
        this.id = id;
        this.nome = nome;
        this.perfil = perfil;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Perfil getPerfil() { return perfil; }
    public void setPerfil(Perfil perfil) { this.perfil = perfil; }

    @Override
    public String toString() { return nome; }
}