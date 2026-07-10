package com.example.gasrun;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class SessionManager {
    // Deklarasi variabel
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    // TAMBAHAN: Kita harus pesan tempat buat menyimpan context
    private Context context;

    // Nama dompet penyimpanan kita
    private static final String PREF_NAME = "GasRunSession";

    // Kunci untuk masing-masing data
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    public static final String KEY_ID_USER = "id_user";
    public static final String KEY_NAMA = "nama";

    public SessionManager(Context context) {
        // Hubungkan context dari luar ke variabel milik class ini
        this.context = context;

        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Fungsi untuk menyimpan data saat user berhasil login
    public void createLoginSession(String idUser, String nama) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_ID_USER, idUser);
        editor.putString(KEY_NAMA, nama);
        editor.apply();
    }

    // Fungsi untuk mengecek apakah user sudah login
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Fungsi untuk mengambil ID User (sangat penting buat Catat Lari nanti!)
    public String getIdUser() {
        return prefs.getString(KEY_ID_USER, null);
    }

    // Fungsi untuk mengambil Nama User
    public String getNama() {
        return prefs.getString(KEY_NAMA, null);
    }

    // Fungsi untuk Logout yang sudah disempurnakan
    public void logoutUser() {
        // Hapus semua data di SharedPreferences
        editor.clear();
        editor.apply();

        // Lempar user kembali ke halaman Login (pakai huruf kecil 'context')
        Intent i = new Intent(context, LoginActivity.class);

        // Bersihkan riwayat halaman sebelumnya biar user gak bisa pencet tombol 'Back' ke Home
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(i);
    }
}