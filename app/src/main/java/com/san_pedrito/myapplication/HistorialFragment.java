package com.san_pedrito.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HistorialFragment extends Fragment {

    public HistorialFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_historial, container, false);

        // Aquí implementaremos la lógica para mostrar el historial
        // TODO: Implementar RecyclerView y su adaptador
        // TODO: Cargar datos del historial
        // TODO: Implementar filtros

        return view;
    }
}