package com.san_pedrito.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.san_pedrito.myapplication.db_kt.Laptop;
import java.util.List;
import com.bumptech.glide.Glide;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder> {
    private List<Laptop> laptops;
    private OnLaptopClickListener listener;

    public interface OnLaptopClickListener {
        void onLaptopClick(Laptop laptop);
    }

    public HistorialAdapter(List<Laptop> laptops, OnLaptopClickListener listener) {
        this.laptops = laptops;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historial, parent, false);
        return new HistorialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        Laptop laptop = laptops.get(position);
        holder.bind(laptop, listener);
    }

    @Override
    public int getItemCount() {
        return laptops != null ? laptops.size() : 0;
    }

    public void updateLaptops(List<Laptop> newLaptops) {
        this.laptops = newLaptops;
        notifyDataSetChanged();
    }

    static class HistorialViewHolder extends RecyclerView.ViewHolder {
        private final ImageView laptopImage;
        private final TextView fechaAccion;
        private final Chip numeroSerieChip;
        private final TextView marcaModelo;
        private final TextView numeroSerie;
        private final TextView detallesAccion;

        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            laptopImage = itemView.findViewById(R.id.laptopImage);
            fechaAccion = itemView.findViewById(R.id.fechaAccion);
            numeroSerieChip = itemView.findViewById(R.id.numeroSerieChip);
            marcaModelo = itemView.findViewById(R.id.marcaModelo);
            numeroSerie = itemView.findViewById(R.id.numeroSerie);
            detallesAccion = itemView.findViewById(R.id.detallesAccion);
        }

        public void bind(final Laptop laptop, final OnLaptopClickListener listener) {
            fechaAccion.setText(laptop.getFechaHora());
            numeroSerieChip.setText(laptop.getNumeroSerie());
            marcaModelo.setText(String.format("%s %s", laptop.getMarca(), laptop.getModelo()));
            numeroSerie.setText(laptop.getNumeroSerie());
            detallesAccion.setText(String.format("Estado: %s", laptop.getEstado()));

            // Cargar imagen usando Glide
            String imagePath = laptop.getRutaImagen();
            if (imagePath != null && !imagePath.isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(imagePath)
                    .placeholder(R.drawable.ic_laptop_placeholder)
                    .error(R.drawable.ic_laptop_placeholder)
                    .into(laptopImage);
            } else {
                laptopImage.setImageResource(R.drawable.ic_laptop_placeholder);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLaptopClick(laptop);
                }
            });
        }
    }
}