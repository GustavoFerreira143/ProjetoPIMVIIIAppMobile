package com.example.projetoappmobile;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnLogin, btnRegister;

    private static final String API_URL = "http://10.0.2.2:5187/usuario/loginUsuario";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Vincular componentes da tela
        etEmail = findViewById(R.id.etEmail);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // Ação do Botão Login
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Preencha o e-mail");
                return;
            }
            efetuarLogin(email);
        });

        // Ação do Botão Cadastro
        btnRegister.setOnClickListener(v -> {
            // Lógica de navegação para tela de registro
             Intent intent = new Intent(MainActivity.this, Registrar.class);
             startActivity(intent);
        });
    }

    private void efetuarLogin(String email) {
        // No Android, redes não podem rodar na Thread Principal (UI).
        // Usamos ExecutorService para rodar em background.
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String result = null;
            boolean success = false;

            try {
                // 1. Configurar Conexão
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);

                // 2. Criar JSON do Body
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("email", email);

                // 3. Enviar dados
                try(OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonParam.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // 4. Ler Resposta
                int code = conn.getResponseCode();
                if (code == 200 || code == 201) {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    result = response.toString();
                    success = true;
                } else {
                    result = "Erro no servidor: " + code;
                }

            } catch (Exception e) {
                e.printStackTrace();
                result = "Erro de conexão: " + e.getMessage();
            }

            // Voltar para a Thread Principal para atualizar a tela
            String finalResult = result;
            boolean finalSuccess = success;

            handler.post(() -> {
                if (finalSuccess) {
                    processarLoginSucesso(finalResult);
                } else {
                    Toast.makeText(MainActivity.this, finalResult, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void processarLoginSucesso(String jsonResponse) {
        try {
            // 1. Parse do JSON (equivalente ao LoginResponse do C#)
            JSONObject jsonObject = new JSONObject(jsonResponse);

            // Verifica se token existe
            if (jsonObject.has("token")) {
                String token = jsonObject.getString("token");

                JSONObject usuarioObj = jsonObject.getJSONObject("usuario");
                String nomeUsuario = usuarioObj.getString("nome");
                int idUsuario = usuarioObj.getInt("id_Usuario");

                // 2. Salvar dados (Equivalente ao SecureStorage/Preferences)
                // Nota: Para dados sensíveis como Token, em produção use EncryptedSharedPreferences
                SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("auth_token", token);
                editor.putString("usuario_nome", nomeUsuario);
                editor.putInt("usuario_id", idUsuario);
                editor.apply();

                Toast.makeText(this, "Login realizado!", Toast.LENGTH_SHORT).show();

                // 3. Navegar para Home
                Intent intent = new Intent(MainActivity.this, Home.class);
                // Limpar a pilha para o usuário não voltar ao login com o botão voltar
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Resposta inválida do servidor", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao processar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}