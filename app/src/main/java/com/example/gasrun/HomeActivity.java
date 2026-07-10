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

// LOGOUT GOOGLE & FIREBASE
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

    private static final String URL_GET_SUMMARY = "http://gasrun-001-site1.dtempurl.com/api/get_summary.php?id_user=";
    private static final String URL_GET_TIPS = "http://gasrun-001-site1.dtempurl.com/api/get_tips.php";

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
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(HomeActivity.this, gso);

            mGoogleSignInClient.signOut().addOnCompleteListener(HomeActivity.this, task -> {
                FirebaseAuth.getInstance().signOut();
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
                                tipList.add(new Tip(
                                        tipObj.getString("id_tip"),
                                        tipObj.getString("judul_tip"),
                                        tipObj.getString("konten"),
                                        tipObj.getString("image_url")
                                ));
                            }
                            tipAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(HomeActivity.this, "Gagal mengambil data tips.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(HomeActivity.this, "Gagal nyambung server: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
                headers.put("Accept", "application/json, text/html, */*");
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

                            // 👇 ANTI-CRASH: Pakai optString, kalau gagal kembalikan "0" 👇
                            String jarak = dataObj.optString("total_jarak", "0");
                            String durasi = dataObj.optString("total_durasi", "0");

                            // Jaga-jaga MySQL ngembaliin kata "null"
                            if (jarak.equals("null")) jarak = "0";
                            if (durasi.equals("null")) durasi = "0";

                            tvTotalJarak.setText(jarak + " KM");
                            tvTotalDurasi.setText(durasi + " Menit");
                        } else {
                            // Munculin pesan kalau sukses = false dari PHP
                            Toast.makeText(HomeActivity.this, "Gagal muat summary: " + jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(HomeActivity.this, "Error baca JSON summary", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // 👇 JANGAN DIKOSONGIN! Biar kita tau errornya apa 👇
                    Toast.makeText(HomeActivity.this, "Error Server Summary: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
                headers.put("Accept", "application/json, text/html, */*");
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