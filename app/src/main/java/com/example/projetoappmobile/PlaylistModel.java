package com.example.projetoappmobile;

import java.io.Serializable;
import java.util.List;

public class PlaylistModel implements Serializable {

    private int id_Playlist;
    private String nome;
    private int usuario_Id;
    private List<VideoModel> videos;

    // Construtor usado para listar playlists (id + nome)
    public PlaylistModel(int id_Playlist, String nome) {
        this.id_Playlist = id_Playlist;
        this.nome = nome;
    }

    // Construtor completo opcional
    public PlaylistModel(int id_Playlist, String nome, int usuario_Id, List<VideoModel> videos) {
        this.id_Playlist = id_Playlist;
        this.nome = nome;
        this.usuario_Id = usuario_Id;
        this.videos = videos;
    }

    public int getId_Playlist() {
        return id_Playlist;
    }

    public String getNome() {
        return nome;
    }

    public int getUsuario_Id() {
        return usuario_Id;
    }

    public List<VideoModel> getVideos() {
        return videos;
    }

    public void setVideos(List<VideoModel> videos) {
        this.videos = videos;
    }
}
