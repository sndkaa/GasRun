package com.example.gasrun;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Menunda selama (2,5 detik)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            // Panggil SessionManager untuk cek status login
            SessionManager sessionManager = new SessionManager(SplashActivity.this);

            if (sessionManager.isLoggedIn()) {
                // Kalau sudah login, bawa ke Dashboard
                startActivity(new Intent(SplashActivity.this, HomeActivity.class));
            } else {
                // Kalau belum, bawa ke halaman Login
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }

            finish();

        }, 2500);
    }
}