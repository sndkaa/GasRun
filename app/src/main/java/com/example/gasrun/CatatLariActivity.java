package com.example.gasrun;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CatatLariActivity extends AppCompatActivity {

    private EditText etJarak, etDurasi;
    private Spinner spinnerKategori;
    private Button btnSimpanLari;
    private SessionManager sessionManager;

    private List<String> listNamaKategori;
    private List<String> listIdKategori;
    private ArrayAdapter<String> spinnerAdapter;

    private static final String URL_GET_CATEGORIES = "http://gasrun-001-site1.dtempurl.com/api/get_categories.php";
    private static final String URL_CATAT_LARI = "http://gasrun-001-site1.dtempurl.com/api/create_log.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catat_lari);

        etJarak = findViewById(R.id.etJarak);
        etDurasi = findViewById(R.id.etDurasi);
        spinnerKategori = findViewById(R.id.spinnerKategori);
        btnSimpanLari = findViewById(R.id.btnSimpanLari);

        sessionManager = new SessionManager(this);

        listNamaKategori = new ArrayList<>();
        listIdKategori = new ArrayList<>();
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listNamaKategori);
        spinnerKategori.setAdapter(spinnerAdapter);

        loadCategories();

        btnSimpanLari.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String jarak = etJarak.getText().toString().trim();
                String durasi = etDurasi.getText().toString().trim();

                if (listIdKategori.isEmpty()) {
                    Toast.makeText(CatatLariActivity.this, "Tunggu sebentar, sedang memuat kategori...", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (jarak.isEmpty() || durasi.isEmpty()) {
                    Toast.makeText(CatatLariActivity.this, "Jarak dan durasi wajib diisi!", Toast.LENGTH_SHORT).show();
                } else {
                    simpanDataLari(jarak, durasi);
                }
            }
        });
    }

    private void loadCategories() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_GET_CATEGORIES,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");

                        if (success) {
                            JSONArray jsonArray = jsonObject.getJSONArray("data");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject catObj = jsonArray.getJSONObject(i);
                                listIdKategori.add(catObj.getString("id_category"));
                                listNamaKategori.add(catObj.getString("nama_kategori"));
                            }
                            spinnerAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(CatatLariActivity.this, "Gagal memuat kategori.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(CatatLariActivity.this, "Error membaca data kategori", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(CatatLariActivity.this, "Gagal nyambung server: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {

            // TAMBAHAN GET HEADERS
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
                headers.put("Accept", "application/json, text/html, */*");
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void simpanDataLari(String jarak, String durasi) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_CATAT_LARI,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");
                        String message = jsonObject.getString("message");

                        if (success) {
                            Toast.makeText(CatatLariActivity.this, message, Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(CatatLariActivity.this, "Gagal: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(CatatLariActivity.this, "Error membaca respon server", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(CatatLariActivity.this, "Gagal nyambung server: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                String tanggalHariIni = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                int posisiPilihan = spinnerKategori.getSelectedItemPosition();
                String idKategoriTerpilih = listIdKategori.get(posisiPilihan);

                params.put("id_user", sessionManager.getIdUser());
                params.put("id_category", idKategoriTerpilih);
                params.put("jarak_km", jarak);
                params.put("durasi_menit", durasi);
                params.put("tanggal_lari", tanggalHariIni);

                return params;
            }

            // GET HEADERS
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
                headers.put("Accept", "application/json, text/html, */*");
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}