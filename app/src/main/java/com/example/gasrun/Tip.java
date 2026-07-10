package com.example.gasrun;

public class Tip {
    private String id;
    private String judul;
    private String konten;
    private String imageUrl; // 👈 Tambah variabel baru buat nampung link gambar

    // 👇 Update Constructor agar bisa menerima data imageUrl dari Volley
    public Tip(String id, String judul, String konten, String imageUrl) {
        this.id = id;
        this.judul = judul;
        this.konten = konten;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public String getJudul() {
        return judul;
    }

    public String getKonten() {
        return konten;
    }

    // 👇 Tambahkan Getter ini supaya bisa dipanggil dengan mulus oleh TipAdapter!
    public String getImageUrl() {
        return imageUrl;
    }
}