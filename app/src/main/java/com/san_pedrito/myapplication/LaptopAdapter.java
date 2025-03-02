package com.san_pedrito.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.san_pedrito.myapplication.db_kt.Laptop;
import android.net.Uri;
import java.util.List;
import com.bumptech.glide.Glide;

public class LaptopAdapter extends RecyclerView.Adapter<LaptopAdapter.LaptopViewHolder> {
    private List<Laptop> laptops;
    private OnLaptopClickListener listener;

    public interface OnLaptopClickListener {
        void onLaptopClick(Laptop laptop);
    }

    public LaptopAdapter(List<Laptop> laptops, OnLaptopClickListener listener) {
        this.laptops = laptops;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LaptopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_laptop, parent, false);
        return new LaptopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LaptopViewHolder holder, int position) {
        Laptop laptop = laptops.get(position);
        holder.bind(laptop, listener);
    }

    @Override
    public int getItemCount() {
        return laptops.size();
    }

    public void updateLaptops(List<Laptop> newLaptops) {
        this.laptops = newLaptops;
        notifyDataSetChanged();
    }

    static class LaptopViewHolder extends RecyclerView.ViewHolder {
        private final ImageView laptopImage;
        private final TextView txtMarca;
        private final TextView txtModelo;
        private final TextView txtSerialNumber;
        private final TextView txtEstado;
        private final TextView txtFecha;

        public LaptopViewHolder(@NonNull View itemView) {
            super(itemView);
            laptopImage = itemView.findViewById(R.id.laptopImage);
            txtMarca = itemView.findViewById(R.id.txtMarca);
            txtModelo = itemView.findViewById(R.id.txtModelo);
            txtSerialNumber = itemView.findViewById(R.id.txtSerialNumber);
            txtEstado = itemView.findViewById(R.id.txtEstado);
            txtFecha = itemView.findViewById(R.id.txtFecha);
        }

        public void bind(final Laptop laptop, final OnLaptopClickListener listener) {
            txtMarca.setText(laptop.getMarca());
            txtModelo.setText(laptop.getModelo());
            txtSerialNumber.setText(laptop.getNumeroSerie());
            txtEstado.setText(laptop.getEstado());
            txtFecha.setText(laptop.getFechaHora());

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