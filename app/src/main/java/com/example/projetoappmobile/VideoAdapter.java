package com.example.projetoappmobile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private Context context;
    private List<VideoModel> listaVideos;

    // Base URL para concatenar com o caminho relativo que vem do JSON
    private static final String BASE_URL = "http://10.0.2.2:5187";
    private static final String PLAYLISTS_URL = "http://10.0.2.2:5187/playlists";

    public VideoAdapter(Context context, List<VideoModel> listaVideos) {
        this.context = context;
        this.listaVideos = listaVideos;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoModel video = listaVideos.get(position);

        holder.txtTitulo.setText(video.getTitulo());
        holder.txtTipo.setText(video.getTipo());

        // Monta a URL completa da imagem com verificação de barra
        String relativePath = video.getThumbnailUrl();
        if (relativePath != null && !relativePath.startsWith("/")) {
            relativePath = "/" + relativePath;
        }
        String fullThumbUrl = BASE_URL + relativePath;

        // Usando Glide para carregar a imagem/gif
        Glide.with(context)
                .load(fullThumbUrl)
                .placeholder(android.R.drawable.ic_menu_gallery) // Imagem enquanto carrega
                .error(android.R.drawable.stat_notify_error) // Imagem se der erro
                .into(holder.imgThumb);

        // Clique no item (abre o player)
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlayerActivity.class);
            intent.putExtra("VIDEO_DADOS", video);
            context.startActivity(intent);
        });

        // Clique no botão "+" para adicionar à playlist
        holder.btnAddToPlaylist.setOnClickListener(v -> {
            // Chama função para buscar playlists e exibir o popup
            buscarPlaylistsEExibirPopup(video.getId());
        });
    }

    private void buscarPlaylistsEExibirPopup(int videoId) {
        // Recupera Token do SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("auth_token", "");

        if (token.isEmpty()) {
            Toast.makeText(context, "Usuário não autenticado!", Toast.LENGTH_SHORT).show();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String result = null;
            try {
                URL url = new URL(PLAYLISTS_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setConnectTimeout(5000);

                int code = conn.getResponseCode();
                if (code == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                    result = sb.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            String finalResult = result;
            handler.post(() -> {
                if (finalResult != null && !finalResult.isEmpty()) {
                    processarPlaylists(finalResult, videoId, token);
                } else {
                    new AlertDialog.Builder(context)
                            .setTitle("Aviso")
                            .setMessage("Não foi possível carregar as playlists ou você ainda não possui nenhuma.")
                            .setPositiveButton("OK", null)
                            .show();
                }
            });
        });
    }

    private void processarPlaylists(String jsonString, int videoId, String token) {
        List<PlaylistModel> playlists = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                int id = obj.getInt("id_Playlist");
                String nome = obj.getString("nome");
                playlists.add(new PlaylistModel(id, nome));
            }

            if (playlists.isEmpty()) {
                Toast.makeText(context, "Nenhuma playlist encontrada.", Toast.LENGTH_SHORT).show();
                return;
            }

            exibirPopupSelecaoPlaylist(playlists, videoId, token);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Erro ao processar playlists.", Toast.LENGTH_SHORT).show();
        }
    }

    private void exibirPopupSelecaoPlaylist(List<PlaylistModel> playlists, int videoId, String token) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Adicionar à Playlist");

        // Cria um Adapter Customizado para a lista dentro do Dialog
        PlaylistAdapter playlistAdapter = new PlaylistAdapter(context, playlists, videoId, token);

        builder.setAdapter(playlistAdapter, null); // O clique é gerenciado dentro do Adapter
        AlertDialog dialog = builder.create();
        
        // Passa a referência do dialog para o adapter poder fechar depois
        playlistAdapter.setDialog(dialog);
        
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return listaVideos.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo, txtTipo;
        ImageView imgThumb;
        Button btnAddToPlaylist;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTituloVideo);
            txtTipo = itemView.findViewById(R.id.txtTipoVideo);
            imgThumb = itemView.findViewById(R.id.imgThumbnail);
            btnAddToPlaylist = itemView.findViewById(R.id.btnAddToPlaylist);
        }
    }

    // --- Classes Auxiliares ---

    private static class PlaylistModel {
        int id;
        String nome;
        PlaylistModel(int id, String nome) {
            this.id = id;
            this.nome = nome;
        }
        @Override
        public String toString() { return nome; }
    }

    private class PlaylistAdapter extends ArrayAdapter<PlaylistModel> {
        private int videoId;
        private String token;
        private AlertDialog dialog; // Para fechar o popup

        public PlaylistAdapter(Context context, List<PlaylistModel> playlists, int videoId, String token) {
            super(context, 0, playlists);
            this.videoId = videoId;
            this.token = token;
        }

        public void setDialog(AlertDialog dialog) {
            this.dialog = dialog;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_playlist_selection, parent, false);
            }

            PlaylistModel playlist = getItem(position);
            TextView txtNome = convertView.findViewById(R.id.txtPlaylistName);
            Button btnAdd = convertView.findViewById(R.id.btnAddVideoToPlaylist);

            if (playlist != null) {
                txtNome.setText(playlist.nome);

                btnAdd.setOnClickListener(v -> {
                    adicionarVideoNaPlaylist(playlist.id, videoId, token, dialog);
                });
            }

            return convertView;
        }
    }

    private void adicionarVideoNaPlaylist(int playlistId, int videoId, String token, AlertDialog dialog) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            boolean sucesso = false;
            String msgErro = "";
            try {
                // POST /playlists/{playlistId}/videos/{conteudoId}
                String endpoint = String.format("%s/%d/videos/%d", PLAYLISTS_URL, playlistId, videoId);
                URL url = new URL(endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setConnectTimeout(5000);
                // POST sem body, ou body vazio se necessario. Alguns servidores exigem Content-Length: 0
                conn.setRequestProperty("Content-Length", "0");
                conn.setDoOutput(true); // Aciona POST

                int code = conn.getResponseCode();
                if (code >= 200 && code < 300) {
                    sucesso = true;
                } else {
                    msgErro = "Erro: " + code;
                }

            } catch (Exception e) {
                e.printStackTrace();
                msgErro = e.getMessage();
            }

            boolean finalSucesso = sucesso;
            String finalMsgErro = msgErro;

            handler.post(() -> {
                if (finalSucesso) {
                    Toast.makeText(context, "Vídeo adicionado com sucesso!", Toast.LENGTH_SHORT).show();
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                } else {
                    new AlertDialog.Builder(context)
                            .setTitle("Erro")
                            .setMessage("Falha ao adicionar vídeo: " + finalMsgErro)
                            .setPositiveButton("OK", null)
                            .show();
                }
            });
        });
    }
}