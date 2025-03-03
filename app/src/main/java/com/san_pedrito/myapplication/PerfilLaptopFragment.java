package com.san_pedrito.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;
import com.san_pedrito.myapplication.db_kt.Laptop;
import com.bumptech.glide.Glide;
import java.io.File;

public class PerfilLaptopFragment extends Fragment {
    private static final String ARG_LAPTOP = "laptop";
    private Laptop laptop;

    public static PerfilLaptopFragment newInstance(Laptop laptop) {
        PerfilLaptopFragment fragment = new PerfilLaptopFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_LAPTOP, laptop);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            laptop = getArguments().getParcelable(ARG_LAPTOP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.perfil_laptop, container, false);

        if (laptop != null) {
            // Cargar imagen
            ShapeableImageView imagenLaptop = view.findViewById(R.id.imagenPerfilLaptop);
            if (laptop.getRutaImagen() != null) {
                Glide.with(this)
                    .load(new File(laptop.getRutaImagen()))
                    .into(imagenLaptop);
            }

            // Establecer textos
            ((TextView) view.findViewById(R.id.tvNumeroSeriePerfil)).setText(laptop.getNumeroSerie());
            ((TextView) view.findViewById(R.id.tvMarcaPerfil)).setText(laptop.getMarca());
            ((TextView) view.findViewById(R.id.tvModeloPerfil)).setText(laptop.getModelo());
            ((TextView) view.findViewById(R.id.tvObservacionesPerfil)).setText(laptop.getObservaciones());
            ((TextView) view.findViewById(R.id.tvFechaPerfil)).setText(laptop.getFechaHora());

            // Configurar chip de estado
            Chip chipEstado = view.findViewById(R.id.chipEstadoPerfil);
            chipEstado.setText(laptop.getEstado());
            // Configurar color según el estado
            switch (laptop.getEstado().toLowerCase()) {
                case "bueno":
                    chipEstado.setChipBackgroundColorResource(R.color.estado_bueno);
                    break;
                case "regular":
                    chipEstado.setChipBackgroundColorResource(R.color.estado_regular);
                    break;
                case "malo":
                    chipEstado.setChipBackgroundColorResource(R.color.estado_malo);
                    break;
            }
        }

        // Configurar el botón de cierre
        view.findViewById(R.id.btnCerrar).setOnClickListener(v -> {
            // Cerrar el fragmento y volver al anterior
            getParentFragmentManager().popBackStack();
        });

        return view;
    }
}