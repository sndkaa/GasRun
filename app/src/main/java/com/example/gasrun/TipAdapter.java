package com.example.gasrun;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // 👈 Library penarik gambar dari internet

import java.util.List;

public class TipAdapter extends RecyclerView.Adapter<TipAdapter.TipViewHolder> {

    private List<Tip> tipList;

    public TipAdapter(List<Tip> tipList) {
        this.tipList = tipList;
    }

    @NonNull
    @Override
    public TipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tip, parent, false);
        return new TipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TipViewHolder holder, int position) {
        Tip tip = tipList.get(position);
        holder.tvJudulTip.setText(tip.getJudul());
        holder.tvKontenTip.setText(tip.getKonten());

        // 👇 Eksekusi penarikan gambar
        Glide.with(holder.itemView.getContext())
                .load(tip.getImageUrl())
                .placeholder(android.R.color.darker_gray)
                .error(android.R.color.holo_red_light)
                .into(holder.ivGambarTip);
    }

    @Override
    public int getItemCount() {
        return tipList.size();
    }

    public static class TipViewHolder extends RecyclerView.ViewHolder {
        TextView tvJudulTip, tvKontenTip;
        ImageView ivGambarTip;

        public TipViewHolder(@NonNull View itemView) {
            super(itemView);
            tvJudulTip = itemView.findViewById(R.id.tvJudulTip);
            tvKontenTip = itemView.findViewById(R.id.tvKontenTip);
            ivGambarTip = itemView.findViewById(R.id.ivGambarTip);
        }
    }
}