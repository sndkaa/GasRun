package com.example.gasrun;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AdminTipAdapter extends RecyclerView.Adapter<AdminTipAdapter.ViewHolder> {

    private List<Tip> tipList;
    private OnTipActionListener listener;

    public interface OnTipActionListener {
        void onEdit(Tip tip);
        void onDelete(String idTip);
    }

    public AdminTipAdapter(List<Tip> tipList, OnTipActionListener listener) {
        this.tipList = tipList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_tip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tip tip = tipList.get(position);
        holder.tvJudul.setText(tip.getJudul());
        holder.tvKonten.setText(tip.getKonten());

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(tip));
        holder.btnHapus.setOnClickListener(v -> listener.onDelete(tip.getId()));
    }

    @Override
    public int getItemCount() {
        return tipList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvJudul, tvKonten;
        Button btnEdit, btnHapus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJudul = itemView.findViewById(R.id.tvAdminJudulTip);
            tvKonten = itemView.findViewById(R.id.tvAdminKontenTip);
            btnEdit = itemView.findViewById(R.id.btnActionEdit);
            btnHapus = itemView.findViewById(R.id.btnActionHapus);
        }
    }
}