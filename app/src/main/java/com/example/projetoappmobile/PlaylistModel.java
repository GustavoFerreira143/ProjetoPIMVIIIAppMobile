package com.example.projetoappmobile;

import java.io.Serializable;
import java.util.List;

public class PlaylistModel implements Serializable {

    private int id_Playlist;
    private String nome;
    private int usuario_Id;

    // a API manda "itensPlaylist", não vídeos
    private List<VideoModel> itensPlaylist;

    public PlaylistModel(int id_Playlist, String nome, int usuario_Id, List<VideoModel> itensPlaylist) {
        this.id_Playlist = id_Playlist;
        this.nome = nome;
        this.usuario_Id = usuario_Id;
        this.itensPlaylist = itensPlaylist;
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

    public List<VideoModel> getItensPlaylist() {
        return itensPlaylist;
    }

    public int getQtdVideos() {
        return itensPlaylist != null ? itensPlaylist.size() : 0;
    }
}
