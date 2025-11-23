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

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private Context context;
    private List<PlaylistModel> playlists;

    public PlaylistAdapter(Context context, List<PlaylistModel> playlists) {
        this.context = context;
        this.playlists = playlists;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item_playlist, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        PlaylistModel playlist = playlists.get(position);

        holder.txtNomePlaylist.setText(playlist.getNome());
        holder.txtQtdVideos.setText(playlist.getVideos().size() + " vídeos");

        List<VideoModel> vids = playlist.getVideos();
        int total = vids.size();

        // ====== THUMB 1 ======
        if (total > 0)
            Glide.with(context).load(vids.get(0).getThumbnailUrl()).into(holder.thumb1);

        // ====== THUMB 2 ======
        if (total > 1)
            Glide.with(context).load(vids.get(1).getThumbnailUrl()).into(holder.thumb2);

        // ====== THUMB 3 ======
        if (total > 2)
            Glide.with(context).load(vids.get(2).getThumbnailUrl()).into(holder.thumb3);

        // ====== THUMB 4 ======
        if (total > 3)
            Glide.with(context).load(vids.get(3).getThumbnailUrl()).into(holder.thumb4);

        // ====== LÓGICA DO +X ======
        if (total > 4) {
            holder.overlayMais.setVisibility(View.VISIBLE);
            holder.txtMais.setVisibility(View.VISIBLE);

            int extra = total - 3;
            if (extra > 99) extra = 99;

            holder.txtMais.setText("+" + extra);
        } else {
            holder.overlayMais.setVisibility(View.GONE);
            holder.txtMais.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public static class PlaylistViewHolder extends RecyclerView.ViewHolder {

        ImageView thumb1, thumb2, thumb3, thumb4;
        View overlayMais;
        TextView txtMais, txtNomePlaylist, txtQtdVideos;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);

            thumb1 = itemView.findViewById(R.id.thumb1);
            thumb2 = itemView.findViewById(R.id.thumb2);
            thumb3 = itemView.findViewById(R.id.thumb3);
            thumb4 = itemView.findViewById(R.id.thumb4);

            overlayMais = itemView.findViewById(R.id.overlayMais);
            txtMais = itemView.findViewById(R.id.txtMais);

            txtNomePlaylist = itemView.findViewById(R.id.txtNomePlaylist);
            txtQtdVideos = itemView.findViewById(R.id.txtQtdVideos);
        }
    }
}
