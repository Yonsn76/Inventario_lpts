package com.san_pedrito.myapplication;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.san_pedrito.myapplication.db_kt.Laptop;
import com.san_pedrito.myapplication.db_kt.LaptopDatabaseHelper;
import com.san_pedrito.myapplication.HistorialAdapter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class HistorialFragment extends Fragment {
    private RecyclerView recyclerView;
    private HistorialAdapter adapter;
    private LaptopDatabaseHelper dbHelper;
    private ChipGroup filterChipGroup;
    private List<Laptop> allLaptops;
    private View exportPanel;
    private ExportPanelManager exportPanelManager;
    private boolean isPanelShowing = false;
    private Animation slideUpAnimation;
    private Animation slideDownAnimation;
    private static final int CREATE_EXCEL_FILE = 1;
    private static final int CREATE_PDF_FILE = 2;
    private List<Laptop> laptopsToExport;

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
        
        // Inicializar panel de exportación y animaciones
        setupExportPanel(view);
        
        // Cargar todos los registros de laptops
        loadAllLaptops();
        
        // Configurar listeners para los chips de filtro
        setupFilterListeners();

        return view;
    }
    
    private void setupExportPanel(View view) {
        exportPanel = view.findViewById(R.id.exportPanel);
        FloatingActionButton fabExport = view.findViewById(R.id.fabExport);
        
        // Inicializar ExportPanelManager
        exportPanelManager = new ExportPanelManager(exportPanel);

        // Configurar botón flotante
        fabExport.setOnClickListener(v -> exportPanelManager.togglePanel());

        // Configurar botones de exportación
        view.findViewById(R.id.btnExcel).setOnClickListener(v -> {
            exportToExcel();
            exportPanelManager.togglePanel();
        });

        view.findViewById(R.id.btnPdf).setOnClickListener(v -> {
            exportToPDF();
            exportPanelManager.togglePanel();
        });

        view.findViewById(R.id.btnCsv).setOnClickListener(v -> {
            exportToCSV();
            exportPanelManager.togglePanel();
        });
    }

    private void loadAllLaptops() {
        try {
            // Obtener todas las laptops de la base de datos
            allLaptops = dbHelper.obtenerTodasLaptops();
            Log.d("HistorialFragment", "Laptops cargadas: " + (allLaptops != null ? allLaptops.size() : 0));
            
            if (allLaptops != null && !allLaptops.isEmpty()) {
                // Log de la primera laptop para verificar datos
                Laptop primeraLaptop = allLaptops.get(0);
                Log.d("HistorialFragment", "Primera laptop - Serie: " + primeraLaptop.getNumeroSerie() 
                    + ", Marca: " + primeraLaptop.getMarca()
                    + ", Modelo: " + primeraLaptop.getModelo());
            }
            
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
            Log.e("HistorialFragment", "Error al cargar laptops: " + e.getMessage());
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

    private void exportToExcel() {
        if (allLaptops == null || allLaptops.isEmpty()) {
            Log.d("HistorialFragment", "No hay laptops para exportar");
            Toast.makeText(getContext(), "No hay datos para exportar", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("HistorialFragment", "Preparando exportación de " + allLaptops.size() + " laptops");
        
        // Verificar datos antes de la exportación
        for (int i = 0; i < Math.min(3, allLaptops.size()); i++) {
            Laptop laptop = allLaptops.get(i);
            Log.d("HistorialFragment", "Laptop " + i + " - Serie: " + laptop.getNumeroSerie() 
                + ", Marca: " + laptop.getMarca()
                + ", Modelo: " + laptop.getModelo());
        }

        laptopsToExport = new ArrayList<>(allLaptops); // Crear una copia de la lista
        Log.d("HistorialFragment", "Lista de exportación creada con " + laptopsToExport.size() + " laptops");
        
        // Crear intent para seleccionar ubicación
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.ms-excel");
        
        // Generar nombre de archivo sugerido
        String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                .format(new java.util.Date());
        String fileName = "Inventario_Laptops_" + timeStamp + ".xls";
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        
        Log.d("HistorialFragment", "Iniciando selección de archivo: " + fileName);
        startActivityForResult(intent, CREATE_EXCEL_FILE);
    }

    private void exportToPDF() {
        if (allLaptops == null || allLaptops.isEmpty()) {
            Log.d("HistorialFragment", "No hay laptops para exportar a PDF");
            Toast.makeText(getContext(), "No hay datos para exportar", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("HistorialFragment", "Preparando exportación a PDF de " + allLaptops.size() + " laptops");
        
        // Verificar datos antes de la exportación
        for (int i = 0; i < Math.min(3, allLaptops.size()); i++) {
            Laptop laptop = allLaptops.get(i);
            Log.d("HistorialFragment", "Laptop " + i + " - Serie: " + laptop.getNumeroSerie() 
                + ", Marca: " + laptop.getMarca()
                + ", Modelo: " + laptop.getModelo());
        }

        laptopsToExport = new ArrayList<>(allLaptops); // Crear una copia de la lista
        Log.d("HistorialFragment", "Lista de exportación PDF creada con " + laptopsToExport.size() + " laptops");
        
        // Crear intent para seleccionar ubicación
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        
        // Generar nombre de archivo sugerido
        String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                .format(new java.util.Date());
        String fileName = "Inventario_Laptops_" + timeStamp + ".pdf";
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        
        Log.d("HistorialFragment", "Iniciando selección de archivo PDF: " + fileName);
        startActivityForResult(intent, CREATE_PDF_FILE);
    }

    private void exportToCSV() {
        Toast.makeText(getContext(), "Exportación a CSV será implementada próximamente", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            if (laptopsToExport == null || laptopsToExport.isEmpty()) {
                Toast.makeText(getContext(), "No hay datos para exportar", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri uri = data.getData();
            int numRegistros = laptopsToExport.size();

            if (requestCode == CREATE_EXCEL_FILE) {
                Toast.makeText(getContext(), "Iniciando exportación de " + numRegistros + " registros a Excel...", Toast.LENGTH_SHORT).show();
                
                ExcelExporter exporter = new ExcelExporter(requireContext());
                exporter.exportToExcel(laptopsToExport, uri, (success, message) -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(getContext(), "Exportación exitosa: " + message, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getContext(), "Error en la exportación: " + message, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    return kotlin.Unit.INSTANCE;
                });
            } else if (requestCode == CREATE_PDF_FILE) {
                Toast.makeText(getContext(), "Iniciando exportación de " + numRegistros + " registros a PDF...", Toast.LENGTH_SHORT).show();
                
                PDFExporter exporter = new PDFExporter(requireContext());
                exporter.exportToPDF(laptopsToExport, uri, (success, message) -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(getContext(), "Exportación exitosa: " + message, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getContext(), "Error en la exportación: " + message, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    return kotlin.Unit.INSTANCE;
                });
            }
        }
    }
}
