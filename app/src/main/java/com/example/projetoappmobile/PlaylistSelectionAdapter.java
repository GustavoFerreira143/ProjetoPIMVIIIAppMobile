package com.example.projetoappmobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlaylistSelectionAdapter extends RecyclerView.Adapter<PlaylistSelectionAdapter.ViewHolder> {

    private List<PlaylistModel> playlists;
    private PlaylistSelectionDialog.OnPlaylistSelected listener;
    private int videoId;

    public PlaylistSelectionAdapter(List<PlaylistModel> playlists, int videoId, PlaylistSelectionDialog.OnPlaylistSelected listener) {
        this.playlists = playlists;
        this.listener = listener;
        this.videoId = videoId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_dialog_playlist_selection, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlaylistModel playlist = playlists.get(position);
        holder.txtName.setText(playlist.getNome());

        holder.btnAdd.setOnClickListener(v ->
                listener.onSelect(playlist.getId_Playlist(), videoId)

        );
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtName;
        Button btnAdd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtPlaylistName);
            btnAdd = itemView.findViewById(R.id.btnAddVideoToPlaylist);
        }
    }
}
