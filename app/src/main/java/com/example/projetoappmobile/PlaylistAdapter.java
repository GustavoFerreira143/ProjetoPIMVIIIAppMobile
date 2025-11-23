package com.example.projetoappmobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    private Context context;
    private List<PlaylistModel> playlists;
    private static final String BASE_URL = "http://10.0.2.2:5187/";

    public PlaylistAdapter(Context context, List<PlaylistModel> playlists) {
        this.context = context;
        this.playlists = playlists;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.activity_item_playlist, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        PlaylistModel playlist = playlists.get(position);

        holder.txtNome.setText(playlist.getNome());
        holder.txtQtd.setText(playlist.getQtdVideos() + " vídeos");

        List<VideoModel> itens = playlist.getItensPlaylist();

        // Limpando o estado anterior
        holder.overlayMais.setVisibility(View.GONE);
        holder.txtMais.setVisibility(View.GONE);

        // Se não tiver vídeos
        if (itens == null || itens.isEmpty()) {
            holder.thumb1.setImageResource(android.R.color.darker_gray);
            holder.thumb2.setImageResource(android.R.color.darker_gray);
            holder.thumb3.setImageResource(android.R.color.darker_gray);
            holder.thumb4.setImageResource(android.R.color.darker_gray);
            return;
        }

        // Carrega thumbs conforme quantidade disponível
        if (itens.size() > 0) loadThumb(holder.thumb1, itens.get(0).getThumbnailUrl());
        if (itens.size() > 1) loadThumb(holder.thumb2, itens.get(1).getThumbnailUrl());
        if (itens.size() > 2) loadThumb(holder.thumb3, itens.get(2).getThumbnailUrl());
        if (itens.size() > 3) loadThumb(holder.thumb4, itens.get(3).getThumbnailUrl());

        // Se tiver MAIS de 4 vídeos → mostrar overlay +X
        if (itens.size() > 4) {
            int extra = itens.size() - 4;
            holder.overlayMais.setVisibility(View.VISIBLE);
            holder.txtMais.setVisibility(View.VISIBLE);
            holder.txtMais.setText("+" + extra);
        }
    }

    private void loadThumb(ImageView img, String relative) {

        if (relative != null && !relative.startsWith("/"))
            relative = "/" + relative;

        Glide.with(context)
                .load(BASE_URL + relative)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.stat_notify_error)
                .into(img);
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView thumb1, thumb2, thumb3, thumb4;
        View overlayMais;
        TextView txtMais, txtNome, txtQtd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            thumb1 = itemView.findViewById(R.id.thumb1);
            thumb2 = itemView.findViewById(R.id.thumb2);
            thumb3 = itemView.findViewById(R.id.thumb3);
            thumb4 = itemView.findViewById(R.id.thumb4);

            overlayMais = itemView.findViewById(R.id.overlayMais);
            txtMais = itemView.findViewById(R.id.txtMais);

            txtNome = itemView.findViewById(R.id.txtNomePlaylist);
            txtQtd = itemView.findViewById(R.id.txtQtdVideos);
        }
    }
}
