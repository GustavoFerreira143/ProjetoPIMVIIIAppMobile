package com.example.projetoappmobile;

import java.io.Serializable;

// Implementa Serializable para podermos passar esse objeto de uma tela para outra
public class VideoModel implements Serializable {
    private int id;
    private String titulo;
    private String tipo;
    private String videoUrl;
    private String thumbnailUrl;

    public VideoModel(int id, String titulo, String tipo, String videoUrl, String thumbnailUrl) {
        this.id = id;
        this.titulo = titulo;
        this.tipo = tipo;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
    }

    // Getters
    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getTipo() { return tipo; }
    public String getVideoUrl() { return videoUrl; }
    public String getThumbnailUrl() { return thumbnailUrl; }
}