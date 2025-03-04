package com.san_pedrito.myapplication;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.san_pedrito.myapplication.db_kt.Laptop;
import java.util.List;
import com.bumptech.glide.Glide;
import android.content.res.ColorStateList;
import android.widget.ImageButton;
import com.san_pedrito.myapplication.interfaces.OnLaptopDeleteClickListener;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.ViewHolder> {
    private List<Laptop> laptops;
    private OnLaptopClickListener listener;
    private OnLaptopDeleteClickListener deleteListener;
    private final int[] cardColors;

    public interface OnLaptopClickListener {
        void onLaptopClick(Laptop laptop);
    }

    public void setOnLaptopClickListener(OnLaptopClickListener listener) {
        this.listener = listener;
    }

    public void setOnLaptopDeleteClickListener(OnLaptopDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    public HistorialAdapter(List<Laptop> laptops) {
        this.laptops = laptops;
        this.cardColors = new int[]{
            R.color.card_accent_1,
            R.color.card_accent_2,
            R.color.card_accent_3,
            R.color.card_accent_4,
            R.color.card_accent_5,
            R.color.card_accent_6,
            R.color.card_accent_7
        };
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historial, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Laptop laptop = laptops.get(position);
        // Obtener color basado en la posición
        int colorResId = cardColors[position % cardColors.length];
        holder.bind(laptop, listener, colorResId);
    }

    @Override
    public int getItemCount() {
        return laptops != null ? laptops.size() : 0;
    }

    public void updateLaptops(List<Laptop> newLaptops) {
        this.laptops = newLaptops;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView laptopImage;
        private final TextView tvNombre;
        private final TextView tvMarca;
        private final TextView tvModelo;
        private final Chip chipEstado;
        private final TextView tvFecha;
        private final View accentLine;
        private final ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            laptopImage = itemView.findViewById(R.id.laptopImage);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvMarca = itemView.findViewById(R.id.tvMarca);
            tvModelo = itemView.findViewById(R.id.tvModelo);
            chipEstado = itemView.findViewById(R.id.chipEstado);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            accentLine = itemView.findViewById(R.id.accentLine);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onLaptopClick(laptops.get(position));
                }
            });

            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && deleteListener != null) {
                    deleteListener.onDeleteClick(position);
                }
            });
        }

        public void bind(final Laptop laptop, final OnLaptopClickListener listener, int colorResId) {
            tvNombre.setText(laptop.getNumeroSerie());
            tvMarca.setText(laptop.getMarca());
            tvModelo.setText(laptop.getModelo());
            chipEstado.setText(laptop.getEstado());
            tvFecha.setText(laptop.getFechaHora());

            // Aplicar color al accentLine y al chip
            int color = ContextCompat.getColor(itemView.getContext(), colorResId);
            accentLine.setBackgroundColor(color);
            chipEstado.setChipBackgroundColor(ColorStateList.valueOf(adjustAlpha(color, 0.2f)));
            chipEstado.setTextColor(color);

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
        }

        // Método auxiliar para ajustar la transparencia del color
        private int adjustAlpha(int color, float factor) {
            int alpha = Math.round(Color.alpha(color) * factor);
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);
            return Color.argb(alpha, red, green, blue);
        }
    }

    public void removeLaptop(int position) {
        if (position >= 0 && position < laptops.size()) {
            laptops.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, laptops.size());
        }
    }
}