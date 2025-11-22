package com.example.projetoappmobile;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class VideosFragment extends Fragment {

    private RecyclerView recyclerView;
    private VideoAdapter adapter;
    private List<VideoModel> listaVideos = new ArrayList<>();

    // URL da API
    private static final String API_URL = "http://10.0.2.2:5187/upload/recebeVideosUser";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_videos, container, false);

        recyclerView = view.findViewById(R.id.recyclerVideos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        carregarVideos();

        return view;
    }

    private void carregarVideos() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String result = null;
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);

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
                    processarJson(finalResult);
                } else {
                    // Opcional: Mostrar mensagem ou logar erro silenciosamente se preferir não incomodar o usuário na falha inicial
                    Toast.makeText(getContext(), "Erro ao carregar vídeos. Verifique a conexão com a API.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void processarJson(String jsonString) {
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            listaVideos.clear();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                int id = obj.getInt("id_Conteudo");
                String titulo = obj.getString("titulo");
                String tipo = obj.getString("tipo");
                
                // Normaliza caminhos que podem vir com barra invertida do Windows
                String video = obj.getString("video").replace("\\", "/"); 
                String thumb = obj.getString("thumbnail").replace("\\", "/"); 

                listaVideos.add(new VideoModel(id, titulo, tipo, video, thumb));
            }

            adapter = new VideoAdapter(getContext(), listaVideos);
            recyclerView.setAdapter(adapter);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Erro ao processar dados dos vídeos.", Toast.LENGTH_SHORT).show();
        }
    }
}