package com.san_pedrito.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import android.view.View;
import android.widget.Button;
import androidx.fragment.app.FragmentTransaction;

public class InicioFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        Button btnNuevoRegistro = view.findViewById(R.id.btn_nuevo_registro);
        btnNuevoRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to RegistroFragment
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.contenedorFragmentos, new RegistroFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return view;
    }
}