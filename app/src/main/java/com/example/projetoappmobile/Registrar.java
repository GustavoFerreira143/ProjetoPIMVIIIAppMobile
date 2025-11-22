package com.example.projetoappmobile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Registrar extends AppCompatActivity {

    private EditText etNome, etEmail;
    private Button btnPerformRegister, btnBackToLogin;

    // URL para o Emulador (10.0.2.2 substitui o localhost)
    private static final String API_URL = "http://10.0.2.2:5187/usuario/cadastraUser";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        // Vincular componentes
        etNome = findViewById(R.id.etNomeRegister);
        etEmail = findViewById(R.id.etEmailRegister);
        btnPerformRegister = findViewById(R.id.btnPerformRegister);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        // Ação do Botão Cadastrar (ViewModel: RegisterCommand)
        btnPerformRegister.setOnClickListener(v -> {
            String nome = etNome.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            if (validarEntradas(nome, email)) {
                realizarCadastro(nome, email);
            }
        });

        // Ação do Botão Voltar (ViewModel: GoToLoginCommand)
        btnBackToLogin.setOnClickListener(v -> {
            finish(); // Fecha a tela de registro e volta para a anterior (Login)
        });
    }

    // Lógica de Validação (Equivalente ao RegisterAsync 'if' checks)
    private boolean validarEntradas(String nome, String email) {

        // 1. Valida Nome Vazio
        if (TextUtils.isEmpty(nome)) {
            mostrarAlerta("Campo Obrigatório", "Por favor, insira o seu nome.");
            return false;
        }

        // 2. Valida Tamanho do Nome
        if (nome.length() < 3) {
            mostrarAlerta("Nome Inválido", "O nome deve ter pelo menos 3 caracteres.");
            return false;
        }

        // 3. Valida Email Vazio
        if (TextUtils.isEmpty(email)) {
            mostrarAlerta("Campo Obrigatório", "Por favor, insira o seu e-mail.");
            return false;
        }

        // 4. Valida Formato do Email (Equivalente ao MailAddress.TryCreate)
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mostrarAlerta("E-mail Inválido", "O formato do e-mail inserido não é válido.");
            return false;
        }

        return true;
    }

    // Lógica de Rede (Equivalente ao Model Registro.EfetuarCadastro)
    private void realizarCadastro(String nome, String email) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            boolean sucesso = false;
            String mensagemErro = "";

            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);

                // Criar JSON Body: { "nome": "...", "email": "..." }
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("nome", nome);
                jsonParam.put("email", email);

                // Enviar
                try(OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonParam.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();

                // No C#: if (response.IsSuccessStatusCode)
                if (responseCode >= 200 && responseCode < 300) {
                    sucesso = true;
                } else {
                    mensagemErro = "Erro do servidor: " + responseCode;
                }

            } catch (Exception e) {
                e.printStackTrace();
                mensagemErro = "Não foi possível conectar: " + e.getMessage();
            }

            // Voltar para a UI Thread
            boolean finalSucesso = sucesso;
            String finalMensagemErro = mensagemErro;

            handler.post(() -> {
                if (finalSucesso) {
                    // Sucesso: Alerta e volta para o login
                    new AlertDialog.Builder(Registrar.this)
                            .setTitle("Sucesso")
                            .setMessage("Cadastro Realizado com Sucesso")
                            .setPositiveButton("OK", (dialog, which) -> {
                                // Navega para login (fechando esta tela)
                                finish();
                            })
                            .show();
                } else {
                    mostrarAlerta("Erro", "Não foi possível realizar o cadastro: " + finalMensagemErro);
                }
            });
        });
    }

    // Helper para mostrar alertas (Equivalente ao DisplayAlert do MAUI)
    private void mostrarAlerta(String titulo, String mensagem) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensagem)
                .setPositiveButton("OK", null)
                .show();
    }
}