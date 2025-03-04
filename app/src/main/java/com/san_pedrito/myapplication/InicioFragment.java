package com.san_pedrito.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import android.view.View;
import android.widget.Button;
import androidx.fragment.app.FragmentTransaction;
import android.widget.Toast;

public class InicioFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        Button btnNuevoRegistro = view.findViewById(R.id.btn_nuevo_registro);
        btnNuevoRegistro.setOnClickListener(v -> {
            // Crear y mostrar el fragmento de registro
            RegistroFragment registroFragment = new RegistroFragment();
            
            // Obtener referencia al BottomNavigationView
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = 
                requireActivity().findViewById(R.id.navegacionInferior);

            // Cambiar el ítem seleccionado
            bottomNav.setSelectedItemId(R.id.menuRegistro);

            // Realizar la transacción del fragmento
            getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorFragmentos, registroFragment)
                .addToBackStack(null)
                .commit();
        });

        return view;
    }
}