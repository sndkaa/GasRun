package com.example.gasrun; // Pastikan nama package ini sesuai dengan punyamu!

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etNama, etEmail, etPassword;
    private Button btnRegister;
    private TextView tvKeHalamanLogin; // 👇 Variabel baru untuk teks login

    // Link API Ngrok
    private static final String URL_REGISTER = "https://untying-slinky-rigging.ngrok-free.dev/gasrun_api/api/register.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Hubungkan variabel Java dengan ID di file XML
        etNama = findViewById(R.id.etNama);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);

        // 👇 HUBUNGKAN DAN HIDUPKAN TEKS LINK LOGIN 👇
        tvKeHalamanLogin = findViewById(R.id.tvKeHalamanLogin);
        tvKeHalamanLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lempar kembali user ke halaman Login
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Tutup halaman register biar memori HP nggak penuh
            }
        });

        // 2. Berikan aksi saat tombol "DAFTAR SEKARANG" diklik
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nama = etNama.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                // 👇 VALIDASI LAPIS BAJA DIMULAI 👇

                // A. Validasi form tidak boleh kosong
                if (nama.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Ups! Semua kolom harus diisi ya!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // B. Validasi Nama (Hanya boleh huruf dan spasi)
                if (!nama.matches("^[a-zA-Z\\s]+$")) {
                    Toast.makeText(RegisterActivity.this, "Nama lengkap tidak boleh mengandung angka atau simbol!", Toast.LENGTH_LONG).show();
                    return;
                }

                // C. Validasi Email (Cek format email standar)
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(RegisterActivity.this, "Format email tidak valid (contoh: gasrun@gmail.com)!", Toast.LENGTH_LONG).show();
                    return;
                }

                // D. Validasi Password (Min 6 karakter, ada huruf besar, huruf kecil, dan angka)
                if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{6,}$")) {
                    Toast.makeText(RegisterActivity.this, "Password minimal 6 karakter, wajib ada huruf besar, huruf kecil, dan angka!", Toast.LENGTH_LONG).show();
                    return;
                }

                // Kalau lolos semua hadangan di atas, baru eksekusi Volley!
                registerUser(nama, email, password);
            }
        });
    }

    private void registerUser(String nama, String email, String password) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_REGISTER,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");
                        String message = jsonObject.getString("message");

                        if (success) {
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(RegisterActivity.this, "Error parsing JSON", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String errorResponse = new String(error.networkResponse.data, "UTF-8");
                            JSONObject jsonObject = new JSONObject(errorResponse);
                            String message = jsonObject.getString("message");
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(RegisterActivity.this, "Error membaca respon server", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Gagal nyambung ke server. Cek koneksi atau Ngrok kamu!", Toast.LENGTH_SHORT).show();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("nama", nama);
                params.put("email", email);
                params.put("password", password);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("ngrok-skip-browser-warning", "12345");
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}