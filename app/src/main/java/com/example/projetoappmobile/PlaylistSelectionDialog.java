package com.example.projetoappmobile;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlaylistSelectionDialog extends Dialog {

    private List<PlaylistModel> playlists;
    private OnPlaylistSelected listener;
    private int videoId;

    public interface OnPlaylistSelected {
        void onSelect(int playlistId, int videoId);
        void onCreateNewPlaylist(String playlistName, int videoId);
    }

    public PlaylistSelectionDialog(@NonNull Context context, List<PlaylistModel> playlists, int videoId, OnPlaylistSelected listener) {
        super(context);
        this.playlists = playlists;
        this.listener = listener;
        this.videoId = videoId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_dialog_playlist_selection);

        RecyclerView recycler = findViewById(R.id.recyclerPlaylistSelection);
        Button btnCriar = findViewById(R.id.btnCriarPlaylist);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        PlaylistSelectionAdapter adapter = new PlaylistSelectionAdapter(playlists, videoId, listener);
        recycler.setAdapter(adapter);

        btnCriar.setOnClickListener(view -> {
            Dialog criarDialog = new Dialog(getContext());
            criarDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            criarDialog.setContentView(R.layout.activity_dialog_playlist_selection);

            EditText edtNome = criarDialog.findViewById(R.id.edtNomePlaylist);
            Button btnSalvar = criarDialog.findViewById(R.id.btnSalvarPlaylist);

            btnSalvar.setOnClickListener(v -> {
                String nome = edtNome.getText().toString().trim();
                if (!nome.isEmpty()) {
                    listener.onCreateNewPlaylist(nome, videoId);
                    criarDialog.dismiss();
                    dismiss();
                }
            });

            criarDialog.show();
        });
    }
}
