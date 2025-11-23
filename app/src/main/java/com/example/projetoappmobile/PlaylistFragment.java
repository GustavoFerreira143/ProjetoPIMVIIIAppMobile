package com.example.projetoappmobile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlaylistFragment extends Fragment {

    private RecyclerView recycler;
    private LinearLayout emptyStateLayout;
    private Button btnCriarPlaylist;

    private PlaylistAdapter playlistAdapter;
    private List<PlaylistModel> listaPlaylists = new ArrayList<>();

    private static final String PLAYLISTS_URL = "http://10.0.2.2:5187/playlists";

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_fragment_playlist, container, false);

        recycler = view.findViewById(R.id.recyclerPlaylist);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        btnCriarPlaylist = view.findViewById(R.id.btnCriarPlaylist);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        playlistAdapter = new PlaylistAdapter(getContext(), listaPlaylists);
        recycler.setAdapter(playlistAdapter);

        btnCriarPlaylist.setOnClickListener(v -> abrirDialogCriarPlaylist());

        carregarPlaylists();

        return view;
    }

    private void carregarPlaylists() {
        SharedPreferences prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("auth_token", "");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String result = null;

            try {
                URL url = new URL(PLAYLISTS_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);

                if (conn.getResponseCode() == 200) {
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
                if (finalResult != null) {
                    processarPlaylists(finalResult);
                } else {
                    mostrarEstadoVazio();
                }
            });
        });
    }

    private void processarPlaylists(String json) {

        listaPlaylists.clear();

        try {
            JSONArray arr = new JSONArray(json);

            for (int i = 0; i < arr.length(); i++) {

                JSONObject obj = arr.getJSONObject(i);

                int id = obj.getInt("id_Playlist");
                String nome = obj.getString("nome");
                int usuarioId = obj.getInt("usuario_Id");

                JSONArray itens = obj.getJSONArray("itensPlaylist");
                List<VideoModel> listaVideos = new ArrayList<>();

                for (int j = 0; j < itens.length(); j++) {
                    JSONObject item = itens.getJSONObject(j);

                    JSONObject conteudo = item.getJSONObject("conteudo");

                    listaVideos.add(new VideoModel(
                            conteudo.getInt("id_Conteudo"),
                            conteudo.getString("titulo"),
                            conteudo.getString("tipo"),
                            conteudo.getString("video"),
                            conteudo.getString("thumbnail")
                    ));
                }

                listaPlaylists.add(new PlaylistModel(id, nome, usuarioId, listaVideos));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (listaPlaylists.isEmpty()) {
            mostrarEstadoVazio();
        } else {
            mostrarEstadoNormal();
        }

        playlistAdapter.notifyDataSetChanged();
    }

    private void mostrarEstadoVazio() {
        recycler.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
    }

    private void mostrarEstadoNormal() {
        emptyStateLayout.setVisibility(View.GONE);
        recycler.setVisibility(View.VISIBLE);
    }

    private void abrirDialogCriarPlaylist() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Criar nova playlist");

        View layout = getLayoutInflater().inflate(R.layout.activity_dialog_create_playlist, null);
        builder.setView(layout);

        Button btnSalvar = layout.findViewById(R.id.btnSalvarPlaylist);
        androidx.appcompat.widget.AppCompatEditText edtNome = layout.findViewById(R.id.edtNomePlaylist);

        AlertDialog dialog = builder.create();

        btnSalvar.setOnClickListener(v -> {
            String nome = edtNome.getText().toString().trim();

            if (nome.isEmpty()) {
                Toast.makeText(getContext(), "Digite um nome.", Toast.LENGTH_SHORT).show();
                return;
            }

            criarPlaylist(nome, dialog);
        });

        dialog.show();
    }

    private void criarPlaylist(String nome, AlertDialog dialog) {

        SharedPreferences prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("auth_token", "");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {

            boolean sucesso = false;

            try {
                URL url = new URL(PLAYLISTS_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject body = new JSONObject();
                body.put("nome", nome);

                conn.getOutputStream().write(body.toString().getBytes());

                sucesso = conn.getResponseCode() == 200 || conn.getResponseCode() == 201;

            } catch (Exception e) {
                e.printStackTrace();
            }

            boolean finalSucesso = sucesso;

            handler.post(() -> {
                if (finalSucesso) {
                    Toast.makeText(getContext(), "Playlist criada!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    carregarPlaylists();
                } else {
                    Toast.makeText(getContext(), "Erro ao criar playlist.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
