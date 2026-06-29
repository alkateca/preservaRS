package com.alkateca.preservars.models;

public class Postagem {

    private String idPost;
    private String titulo;
    private String descricao;
    private String urlImagem;
    private String idUser;
    private long timestamp;

    public Postagem(){}

    public Postagem(String idPost, String titulo, String descricao, String urlImagem, String idUser, long timestamp) {
        this.idPost = idPost;
        this.titulo = titulo;
        this.descricao = descricao;
        this.urlImagem = urlImagem;
        this.idUser = idUser;
        this.timestamp = timestamp;
    }

    public String getIdPost() {
        return idPost;
    }

    public void setIdPost(String idPost) {
        this.idPost = idPost;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getUrlImagem() {
        return urlImagem;
    }

    public void setUrlImagem(String urlImagem) {
        this.urlImagem = urlImagem;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
