package com.example.gasrun;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RiwayatActivity extends AppCompatActivity {

    private RecyclerView rvRiwayat;
    private LinearLayout layoutEmptyState;
    private RunLogAdapter adapter;
    private List<RunLog> logList;
    private SessionManager sessionManager;

    private static final String URL_GET_LOGS = "https://untying-slinky-rigging.ngrok-free.dev/gasrun_api/api/get_logs.php?id_user=";
    private static final String URL_DELETE_LOG = "https://untying-slinky-rigging.ngrok-free.dev/gasrun_api/api/delete_log.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat);

        rvRiwayat = findViewById(R.id.rvRiwayat);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);

        rvRiwayat.setLayoutManager(new LinearLayoutManager(this));

        sessionManager = new SessionManager(this);
        logList = new ArrayList<>();

        adapter = new RunLogAdapter(logList, (log, position) -> {
            tampilkanDialogOpsi(log, position);
        });

        rvRiwayat.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRiwayatLari();
    }

    private void cekDataKosong() {
        if (logList.isEmpty()) {
            rvRiwayat.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvRiwayat.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    private void loadRiwayatLari() {
        logList.clear();
        String urlApi = URL_GET_LOGS + sessionManager.getIdUser();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlApi,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject logObj = jsonArray.getJSONObject(i);
                                logList.add(new RunLog(
                                        logObj.getString("id_log"),
                                        logObj.getString("id_category"),
                                        logObj.getString("nama_kategori"),
                                        logObj.getString("tanggal_lari"),
                                        logObj.getString("jarak_km"),
                                        logObj.getString("durasi_menit")
                                ));
                            }
                        }
                        adapter.notifyDataSetChanged();
                        cekDataKosong();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        cekDataKosong();
                    }
                },
                error -> {
                    Toast.makeText(RiwayatActivity.this, "Koneksi Error", Toast.LENGTH_SHORT).show();
                    cekDataKosong();
                }) {

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

    private void tampilkanDialogOpsi(RunLog log, int position) {
        String[] opsi = {"✏️ Edit Aktivitas", "🗑️ Hapus Riwayat"};
        new AlertDialog.Builder(this)
                .setTitle("Pilih Aksi")
                .setItems(opsi, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(RiwayatActivity.this, EditLariActivity.class);
                        intent.putExtra("id_log", log.getIdLog());
                        intent.putExtra("id_category", log.getIdCategory());
                        intent.putExtra("jarak_km", log.getJarakKm());
                        intent.putExtra("durasi_menit", log.getDurasiMenit());
                        intent.putExtra("tanggal_lari", log.getTanggalLari());
                        startActivity(intent);
                    } else if (which == 1) {
                        konfirmasiHapus(log, position);
                    }
                })
                .show();
    }

    private void konfirmasiHapus(RunLog log, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Riwayat")
                .setMessage("Yakin mau hapus catatan lari " + log.getNamaKategori() + " ini?")
                .setPositiveButton("Hapus", (dialog, which) -> hapusDataLari(log.getIdLog(), position))
                .setNegativeButton("Batal", null)
                .show();
    }

    private void hapusDataLari(String idLog, int position) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_DELETE_LOG,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            Toast.makeText(RiwayatActivity.this, "Berhasil dihapus!", Toast.LENGTH_SHORT).show();
                            logList.remove(position);
                            adapter.notifyItemRemoved(position);
                            cekDataKosong();
                        } else {
                            Toast.makeText(RiwayatActivity.this, "Gagal: " + jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(RiwayatActivity.this, "Server Error", Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_log", idLog);
                params.put("id_user", sessionManager.getIdUser());
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