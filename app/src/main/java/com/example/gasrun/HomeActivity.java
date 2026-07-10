package com.example.gasrun;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

// 👇 IMPORT TAMBAHAN UNTUK LOGOUT GOOGLE & FIREBASE 👇
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView rvTips;
    private TipAdapter tipAdapter;
    private List<Tip> tipList;

    private TextView tvWelcomeName;
    private SessionManager sessionManager;

    private Button btnKeHalamanCatat;
    private Button btnLihatRiwayat;
    private Button btnLogout;

    private TextView tvTotalJarak, tvTotalDurasi;

    private static final String URL_GET_SUMMARY = "https://untying-slinky-rigging.ngrok-free.dev/gasrun_api/api/get_summary.php?id_user=";
    private static final String URL_GET_TIPS = "https://untying-slinky-rigging.ngrok-free.dev/gasrun_api/api/get_tips.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);

        tvWelcomeName = findViewById(R.id.tvWelcomeName);
        tvTotalJarak = findViewById(R.id.tvTotalJarak);
        tvTotalDurasi = findViewById(R.id.tvTotalDurasi);
        btnKeHalamanCatat = findViewById(R.id.btnKeHalamanCatat);
        btnLihatRiwayat = findViewById(R.id.btnLihatRiwayat);
        rvTips = findViewById(R.id.rvTips);

        String namaUser = sessionManager.getNama();
        if (namaUser != null) {
            tvWelcomeName.setText("Halo, " + namaUser + "!");
        }

        loadSummaryLatihan();
        loadTips();

        btnLihatRiwayat.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, RiwayatActivity.class);
            startActivity(intent);
        });

        btnKeHalamanCatat.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CatatLariActivity.class);
            startActivity(intent);
        });

        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            // 1. Konfigurasi Google Client
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(HomeActivity.this, gso);

            // 2. Paksa Google untuk hapus cache akun terakhir (Sign Out)
            mGoogleSignInClient.signOut().addOnCompleteListener(HomeActivity.this, task -> {
                // 3. Keluar juga dari Firebase Auth
                FirebaseAuth.getInstance().signOut();

                // 4. Jalankan fungsi logout bawaan aplikasi kamu (SessionManager MySQL)
                sessionManager.logoutUser();
                finish();
            });
        });

        rvTips.setLayoutManager(new LinearLayoutManager(this));
        tipList = new ArrayList<>();
        tipAdapter = new TipAdapter(tipList);
        rvTips.setAdapter(tipAdapter);
    }

    private void loadTips() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_GET_TIPS,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            JSONArray jsonArray = jsonObject.getJSONArray("data");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject tipObj = jsonArray.getJSONObject(i);

                                // 👇 INI YANG DIUPDATE: Nangkep image_url dari JSON 👇
                                tipList.add(new Tip(
                                        tipObj.getString("id_tip"),
                                        tipObj.getString("judul_tip"),
                                        tipObj.getString("konten"),
                                        tipObj.getString("image_url") // 👈 Eksekusi nangkep URL gambar
                                ));
                            }
                            tipAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(HomeActivity.this, "Yah, gagal mengambil data nih.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(HomeActivity.this, "Gagal nyambung server: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("ngrok-skip-browser-warning", "12345");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void loadSummaryLatihan() {
        String urlApi = URL_GET_SUMMARY + sessionManager.getIdUser();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlApi,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            JSONObject dataObj = jsonObject.getJSONObject("data");
                            String jarak = dataObj.getString("total_jarak");
                            String durasi = dataObj.getString("total_durasi");

                            tvTotalJarak.setText(jarak + " KM");
                            tvTotalDurasi.setText(durasi + " Menit");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                }) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("ngrok-skip-browser-warning", "12345");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSummaryLatihan();
    }
}