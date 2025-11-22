package com.example.projetoappmobile;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

public class PlayerActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.0.2.2:5187";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        VideoView videoView = findViewById(R.id.videoView);
        TextView txtTitulo = findViewById(R.id.lblTituloPlayer);
        TextView txtTipo = findViewById(R.id.lblTipoPlayer);

        // Recupera os dados passados pelo Adapter
        VideoModel video = (VideoModel) getIntent().getSerializableExtra("VIDEO_DADOS");

        if (video != null) {
            txtTitulo.setText(video.getTitulo());
            txtTipo.setText(video.getTipo());

            // Monta URL do vídeo com verificação de barra
            String relativePath = video.getVideoUrl();
            if (relativePath != null && !relativePath.startsWith("/")) {
                relativePath = "/" + relativePath;
            }
            String videoFullUrl = BASE_URL + relativePath;
            
            Uri uri = Uri.parse(videoFullUrl);

            // Adiciona controles de mídia (Play/Pause/Barra de progresso)
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);

            videoView.setMediaController(mediaController);
            videoView.setVideoURI(uri);
            videoView.start(); // Inicia automaticamente
        }
    }
}