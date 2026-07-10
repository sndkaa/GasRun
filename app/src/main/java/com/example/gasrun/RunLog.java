package com.example.gasrun;

public class RunLog {
    private String idLog, idCategory, namaKategori, tanggalLari, jarakKm, durasiMenit;

    // Constructor sekarang ditambah idCategory
    public RunLog(String idLog, String idCategory, String namaKategori, String tanggalLari, String jarakKm, String durasiMenit) {
        this.idLog = idLog;
        this.idCategory = idCategory;
        this.namaKategori = namaKategori;
        this.tanggalLari = tanggalLari;
        this.jarakKm = jarakKm;
        this.durasiMenit = durasiMenit;
    }

    // Getter untuk mengambil data
    public String getIdLog() { return idLog; }
    public String getIdCategory() { return idCategory; }
    public String getNamaKategori() { return namaKategori; }
    public String getTanggalLari() { return tanggalLari; }
    public String getJarakKm() { return jarakKm; }
    public String getDurasiMenit() { return durasiMenit; }
}