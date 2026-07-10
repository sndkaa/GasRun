package com.example.gasrun;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {

    private LinearLayout cardTotalUsers;
    private TextView tvTotalUsers;
    private Button btnTambahTipMenu, btnAdminLogout;

    private RecyclerView rvAdminTips;
    private AdminTipAdapter adapter;
    private List<Tip> tipList;
    private SessionManager sessionManager;

    // Jendela Dialog PopUp
    private BottomSheetDialog formDialog, userListDialog;

    // Komponen di dalam PopUp Form
    private EditText etJudulTip, etKontenTip;
    private ImageView ivPreviewGambar;
    private Button btnSimpanTip;
    private Bitmap bitmapGambar = null;

    private boolean isEditMode = false;
    private String selectedTipId = "";
    private static final int PICK_IMAGE_REQUEST = 1;

    // 🚨 ENDPOINT API 🚨
    private static final String BASE_URL = "https://untying-slinky-rigging.ngrok-free.dev/gasrun_api/api/";
    private static final String URL_INSERT = BASE_URL + "insert_tip.php";
    private static final String URL_UPDATE = BASE_URL + "update_tip.php";
    private static final String URL_DELETE = BASE_URL + "delete_tip.php";
    private static final String URL_GET_TIPS = BASE_URL + "get_tips.php";
    private static final String URL_STATS    = BASE_URL + "get_admin_stats.php";
    private static final String URL_USERS    = BASE_URL + "get_users.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        sessionManager = new SessionManager(this);

        cardTotalUsers = findViewById(R.id.cardTotalUsers);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        btnTambahTipMenu = findViewById(R.id.btnTambahTipMenu);
        btnAdminLogout = findViewById(R.id.btnAdminLogout);

        rvAdminTips = findViewById(R.id.rvAdminTips);
        rvAdminTips.setLayoutManager(new LinearLayoutManager(this));
        tipList = new ArrayList<>();

        adapter = new AdminTipAdapter(tipList, new AdminTipAdapter.OnTipActionListener() {
            @Override
            public void onEdit(Tip tip) {
                bukaDialogForm(tip); // Masuk mode edit bawa data
            }

            @Override
            public void onDelete(String idTip) {
                hapusTipsFromServer(idTip);
            }
        });
        rvAdminTips.setAdapter(adapter);

        // Aksi klik kartu total user
        cardTotalUsers.setOnClickListener(v -> bukaDialogUserList());

        // Aksi klik menu tambah tips
        btnTambahTipMenu.setOnClickListener(v -> bukaDialogForm(null));

        btnAdminLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            startActivity(new Intent(AdminDashboardActivity.this, LoginActivity.class));
            finish();
        });

        loadDashboardData();
    }

    private void loadDashboardData() {
        loadTotalUsersCount();
        loadDaftarTipsAdmin();
    }

    private void loadTotalUsersCount() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_STATS,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            tvTotalUsers.setText(jsonObject.getInt("total_users") + " User");
                        }
                    } catch (JSONException e) { e.printStackTrace(); }
                }, error -> {}) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("ngrok-skip-browser-warning", "12345");
                return headers;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void loadDaftarTipsAdmin() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_GET_TIPS,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            tipList.clear();
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
                            adapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) { e.printStackTrace(); }
                }, error -> {}) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("ngrok-skip-browser-warning", "12345");
                return headers;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
    }

    // 👇 POPUP DIALOG FORM TAMBAH / EDIT 👇
    private void bukaDialogForm(@Nullable Tip tip) {
        formDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_form_tip, null);
        formDialog.setContentView(view);

        TextView tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        etJudulTip = view.findViewById(R.id.etJudulTip);
        etKontenTip = view.findViewById(R.id.etKontenTip);
        Button btnPilihGambar = view.findViewById(R.id.btnPilihGambar);
        ivPreviewGambar = view.findViewById(R.id.ivPreviewGambar);
        btnSimpanTip = view.findViewById(R.id.btnSimpanTip);

        bitmapGambar = null; // Reset tampungan gambar

        if (tip != null) {
            isEditMode = true;
            selectedTipId = tip.getId();
            tvDialogTitle.setText("Edit Konten Artikel");
            etJudulTip.setText(tip.getJudul());
            etKontenTip.setText(tip.getKonten());
            btnSimpanTip.setText("UPDATE ARTIKEL");
        } else {
            isEditMode = false;
            tvDialogTitle.setText("Tambah Artikel Tips Baru");
            btnSimpanTip.setText("PUBLIKASIKAN ARTIKEL");
        }

        btnPilihGambar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), PICK_IMAGE_REQUEST);
        });

        btnSimpanTip.setOnClickListener(v -> {
            String judul = etJudulTip.getText().toString().trim();
            String konten = etKontenTip.getText().toString().trim();

            if (judul.isEmpty() || konten.isEmpty()) {
                Toast.makeText(this, "Judul dan konten wajib diisi!", Toast.LENGTH_SHORT).show();
                return;
            }

            String imageBase64 = (bitmapGambar != null) ? ubahGambarKeBase64(bitmapGambar) : "";

            if (isEditMode) {
                eksekusiUpdateTips(selectedTipId, judul, konten, imageBase64);
            } else {
                if (bitmapGambar == null) {
                    Toast.makeText(this, "Foto wajib dipilih!", Toast.LENGTH_SHORT).show();
                    return;
                }
                eksekusiTambahTips(judul, konten, imageBase64);
            }
        });

        formDialog.show();
    }

    // 👇 POPUP DIALOG DAFTAR USER 👇
    private void bukaDialogUserList() {
        userListDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_user_list, null);
        userListDialog.setContentView(view);

        RecyclerView rvUserList = view.findViewById(R.id.rvUserList);
        rvUserList.setLayoutManager(new LinearLayoutManager(this));
        List<HashMap<String, String>> users = new ArrayList<>();
        UserAdapter userAdapter = new UserAdapter(users);
        rvUserList.setAdapter(userAdapter);

        // Ambil data user dari server
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_USERS,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            JSONArray array = jsonObject.getJSONArray("data");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                HashMap<String, String> map = new HashMap<>();
                                map.put("nama", obj.getString("nama"));
                                map.put("email", obj.getString("email"));
                                users.add(map);
                            }
                            userAdapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) { e.printStackTrace(); }
                }, error -> {}) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("ngrok-skip-browser-warning", "12345");
                return headers;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
        userListDialog.show();
    }

    private void eksekusiTambahTips(String judul, String konten, String imageBase64) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_INSERT,
                response -> {
                    formDialog.dismiss();
                    loadDashboardData();
                    Toast.makeText(this, "Tips Berhasil Ditambahkan!", Toast.LENGTH_SHORT).show();
                }, error -> {}) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("judul_tip", judul);
                params.put("konten", konten);
                params.put("image_base64", imageBase64);
                params.put("id_user", sessionManager.getIdUser());
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void eksekusiUpdateTips(String idTip, String judul, String konten, String imageBase64) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_UPDATE,
                response -> {
                    formDialog.dismiss();
                    loadDashboardData();
                    Toast.makeText(this, "Tips Berhasil Diperbarui!", Toast.LENGTH_SHORT).show();
                }, error -> {}) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_tip", idTip);
                params.put("judul_tip", judul);
                params.put("konten", konten);
                params.put("image_base64", imageBase64);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void hapusTipsFromServer(String idTip) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_DELETE,
                response -> {
                    loadDashboardData();
                    Toast.makeText(this, "Tips Berhasil Dihapus!", Toast.LENGTH_SHORT).show();
                }, error -> {}) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id_tip", idTip);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                bitmapGambar = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                if (ivPreviewGambar != null) {
                    ivPreviewGambar.setImageBitmap(bitmapGambar);
                }
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    private String ubahGambarKeBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }
}