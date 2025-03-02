package com.san_pedrito.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.san_pedrito.myapplication.db_kt.Laptop;
import com.san_pedrito.myapplication.db_kt.LaptopDatabaseHelper;
import com.san_pedrito.myapplication.HistorialAdapter;

import java.util.List;

public class HistorialFragment extends Fragment {
    private RecyclerView recyclerView;
    private HistorialAdapter adapter;
    private LaptopDatabaseHelper dbHelper;
    private ChipGroup filterChipGroup;
    private List<Laptop> allLaptops;

    public HistorialFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new LaptopDatabaseHelper(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_historial, container, false);

        // Inicializar RecyclerView
        recyclerView = view.findViewById(R.id.historialRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Inicializar ChipGroup para filtros
        filterChipGroup = view.findViewById(R.id.filterChipGroup);
        
        // Cargar todos los registros de laptops
        loadAllLaptops();
        
        // Configurar listeners para los chips de filtro
        setupFilterListeners();

        return view;
    }
    
    private void loadAllLaptops() {
        try {
            // Obtener todas las laptops de la base de datos
            allLaptops = dbHelper.obtenerTodasLaptops();
            
            // Configurar el adaptador
            adapter = new HistorialAdapter(allLaptops, laptop -> {
                // Al hacer clic en un item, navegar al fragmento de edición
                EdicionFragment edicionFragment = new EdicionFragment();
                Bundle args = new Bundle();
                args.putString("serial_number", laptop.getNumeroSerie());
                edicionFragment.setArguments(args);
                
                getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedorFragmentos, edicionFragment)
                    .addToBackStack(null)
                    .commit();
            });
            
            recyclerView.setAdapter(adapter);
            
            if (allLaptops.isEmpty()) {
                Toast.makeText(getContext(), "No hay registros disponibles", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error al cargar los registros: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    private void setupFilterListeners() {
        // Configurar listener para el grupo de chips
        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipTodos) {
                // Mostrar todos los registros
                if (adapter != null && allLaptops != null) {
                    adapter.updateLaptops(allLaptops);
                }
            } else {
                // En una versión futura se pueden implementar filtros adicionales
                // Por ahora solo mostramos todos los registros
                if (adapter != null && allLaptops != null) {
                    adapter.updateLaptops(allLaptops);
                }
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Recargar datos cuando el fragmento vuelve a ser visible
        // para mostrar cambios realizados en otros fragmentos
        loadAllLaptops();
    }
}