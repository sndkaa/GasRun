package com.example.gasrun;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EditLariActivity extends AppCompatActivity {

    private EditText etEditJarak, etEditDurasi, etEditTanggal, etEditKategori;
    private Button btnSimpanEdit;
    private String idLog;
    private SessionManager sessionManager;

    private static final String URL_UPDATE_LOG = "https://untying-slinky-rigging.ngrok-free.dev/gasrun_api/api/update_log.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_lari);

        sessionManager = new SessionManager(this);

        etEditJarak = findViewById(R.id.etEditJarak);
        etEditDurasi = findViewById(R.id.etEditDurasi);
        etEditTanggal = findViewById(R.id.etEditTanggal);
        etEditKategori = findViewById(R.id.etEditKategori);
        btnSimpanEdit = findViewById(R.id.btnSimpanEdit);

        idLog = getIntent().getStringExtra("id_log");
        etEditKategori.setText(getIntent().getStringExtra("id_category"));
        etEditJarak.setText(getIntent().getStringExtra("jarak_km"));
        etEditDurasi.setText(getIntent().getStringExtra("durasi_menit"));
        etEditTanggal.setText(getIntent().getStringExtra("tanggal_lari"));

        btnSimpanEdit.setOnClickListener(v -> simpanPerubahan());
    }

    private void simpanPerubahan() {
        String jarak = etEditJarak.getText().toString().trim();
        String durasi = etEditDurasi.getText().toString().trim();
        String tanggal = etEditTanggal.getText().toString().trim();
        String kategori = etEditKategori.getText().toString().trim();

        if (jarak.isEmpty() || durasi.isEmpty() || tanggal.isEmpty() || kategori.isEmpty()) {
            Toast.makeText(this, "Semua kolom wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_UPDATE_LOG,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            Toast.makeText(this, "Berhasil diupdate!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Gagal: " + jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error Server", Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_log", idLog);
                params.put("id_user", sessionManager.getIdUser());
                params.put("id_category", kategori);
                params.put("jarak_km", jarak);
                params.put("durasi_menit", durasi);
                params.put("tanggal_lari", tanggal);
                return params;
            }

            // TAMBAHAN GET HEADERS
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("ngrok-skip-browser-warning", "12345");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }
}