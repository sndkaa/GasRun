package com.example.gasrun;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RunLogAdapter extends RecyclerView.Adapter<RunLogAdapter.RunLogViewHolder> {

    private List<RunLog> logList;
    private OnItemLongClickListener listener;

    // Interface jembatan untuk mendeteksi aksi tekan lama
    public interface OnItemLongClickListener {
        void onItemLongClick(RunLog log, int position);
    }

    // Constructor sekarang minta listener juga
    public RunLogAdapter(List<RunLog> logList, OnItemLongClickListener listener) {
        this.logList = logList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RunLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log, parent, false);
        return new RunLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RunLogViewHolder holder, int position) {
        RunLog log = logList.get(position);

        holder.tvLogKategori.setText(log.getNamaKategori());
        holder.tvLogTanggal.setText(log.getTanggalLari());
        holder.tvLogJarak.setText(log.getJarakKm() + " KM");
        holder.tvLogDurasi.setText(log.getDurasiMenit() + " Menit");

        // Aksi ketika kartu ditahan agak lama
        holder.itemView.setOnLongClickListener(v -> {
            listener.onItemLongClick(log, position);
            return true; // Beri tahu Android kalau klik tahan sudah diproses
        });
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    public static class RunLogViewHolder extends RecyclerView.ViewHolder {
        TextView tvLogKategori, tvLogTanggal, tvLogJarak, tvLogDurasi;

        public RunLogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLogKategori = itemView.findViewById(R.id.tvLogKategori);
            tvLogTanggal = itemView.findViewById(R.id.tvLogTanggal);
            tvLogJarak = itemView.findViewById(R.id.tvLogJarak);
            tvLogDurasi = itemView.findViewById(R.id.tvLogDurasi);
        }
    }
}