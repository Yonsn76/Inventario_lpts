package com.san_pedrito.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.san_pedrito.myapplication.db_kt.Laptop;
import com.san_pedrito.myapplication.db_kt.LaptopDatabaseHelper;
import com.san_pedrito.myapplication.export_metodo.CSVExporter;
import com.san_pedrito.myapplication.export_metodo.ExcelExporter;
import com.san_pedrito.myapplication.export_metodo.PDFExporter;
import android.app.AlertDialog;
import com.san_pedrito.myapplication.interfaces.OnLaptopDeleteClickListener;
import com.san_pedrito.myapplication.interfaces.OnLaptopEditClickListener;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.ListPopupWindow;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class HistorialFragment extends Fragment {
    private RecyclerView recyclerView;
    private HistorialAdapter adapter;
    private LaptopDatabaseHelper dbHelper;
    private ChipGroup filterChipGroup;
    private List<Laptop> allLaptops = new ArrayList<>();
    private View exportPanel;
    private ExportPanelManager exportPanelManager;
    private boolean isPanelShowing = false;
    private Animation slideUpAnimation;
    private Animation slideDownAnimation;
    private static final int CREATE_EXCEL_FILE = 1;
    private static final int CREATE_PDF_FILE = 2;
    private static final int CREATE_CSV_FILE = 3;
    private List<Laptop> laptopsToExport;
    private TextInputEditText searchEditText;
    private ListPopupWindow listPopupWindow;

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
        View view = inflater.inflate(R.layout.fragment_historial, container, false);

        recyclerView = view.findViewById(R.id.historialRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializar el adaptador con una lista vacía
        adapter = new HistorialAdapter(new ArrayList<>());
        adapter.setOnLaptopClickListener(laptop -> {
            PerfilLaptopFragment perfilFragment = PerfilLaptopFragment.newInstance(laptop);
            getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorFragmentos, perfilFragment)
                .addToBackStack(null)
                .commit();
        });
        adapter.setOnLaptopDeleteClickListener(position -> {
            Laptop laptopToDelete = allLaptops.get(position);
            
            new AlertDialog.Builder(requireContext())
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar esta laptop?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    try {
                        // Eliminar de la base de datos
                        dbHelper.eliminarLaptop(laptopToDelete.getId());
                        
                        // Eliminar de la lista y actualizar el adaptador
                        allLaptops.remove(position);
                        adapter.removeLaptop(position);
                        
                        // Recargar la lista completa para asegurar sincronización
                        loadAllLaptops();
                        
                        Toast.makeText(getContext(), "Laptop eliminada exitosamente", 
                            Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(getContext(), 
                            "Error al eliminar: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
        });
        adapter.setOnLaptopEditClickListener(laptop -> {
            EdicionFragment edicionFragment = new EdicionFragment();
            
            Bundle args = new Bundle();
            args.putString("numeroSerie", laptop.getNumeroSerie());
            edicionFragment.setArguments(args);
            
            // Obtener referencia al BottomNavigationView y cambiar a la pestaña de edición
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = 
                requireActivity().findViewById(R.id.navegacionInferior);
            bottomNav.setSelectedItemId(R.id.menuEdicion);
            
            // Cambiar al fragmento de edición
            getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedorFragmentos, edicionFragment)
                .addToBackStack(null)
                .commit();
        });
        recyclerView.setAdapter(adapter);

        // Cargar datos inmediatamente
        loadAllLaptops();
        
        // Configurar búsqueda
        TextInputLayout searchLayout = view.findViewById(R.id.searchLayout);
        searchEditText = view.findViewById(R.id.searchEditText);
        
        // Configurar el TextWatcher
        initializeSearchView();
        
        searchLayout.setEndIconOnClickListener(v -> {
            String query = searchEditText.getText().toString();
            filterLaptops(query);
        });

        setupExportPanel(view);

        return view;
    }
    
    private void setupExportPanel(View view) {
        exportPanel = view.findViewById(R.id.exportPanel);
        FloatingActionButton fabExport = view.findViewById(R.id.fabExport);
        
        // Inicializar ExportPanelManager
        exportPanelManager = new ExportPanelManager(exportPanel);

        // Configurar botón flotante
        fabExport.setOnClickListener(v -> {
            exportPanelManager.togglePanel();
            // Asegurarse de que el panel no interfiera con los clicks cuando está oculto
            exportPanel.setClickable(exportPanelManager.isPanelShowing());
            exportPanel.setEnabled(exportPanelManager.isPanelShowing());
        });

        // Configurar botones de exportación con manejo explícito del estado del panel
        View.OnClickListener exportButtonClickListener = v -> {
            if (v.getId() == R.id.btnExcel) {
                exportToExcel();
            } else if (v.getId() == R.id.btnPdf) {
                exportToPDF();
            } else if (v.getId() == R.id.btnCsv) {
                exportToCSV();
            }
            // Ocultar el panel después de hacer clic en cualquier botón
            exportPanelManager.hidePanel();
            exportPanel.setClickable(false);
            exportPanel.setEnabled(false);
        };

        view.findViewById(R.id.btnExcel).setOnClickListener(exportButtonClickListener);
        view.findViewById(R.id.btnPdf).setOnClickListener(exportButtonClickListener);
        view.findViewById(R.id.btnCsv).setOnClickListener(exportButtonClickListener);
    }

    private void loadAllLaptops() {
        try {
            List<Laptop> newLaptops = dbHelper.obtenerTodasLaptops();
            
            if (newLaptops != null && !newLaptops.isEmpty()) {
                // Actualizar la lista local
                allLaptops.clear();
                allLaptops.addAll(newLaptops);
                
                // Actualizar el adaptador
                adapter.updateLaptops(new ArrayList<>(allLaptops));
            } else {
                allLaptops.clear();
                adapter.updateLaptops(new ArrayList<>());
                Toast.makeText(getContext(), "No hay registros disponibles", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("HistorialFragment", "Error al cargar laptops: " + e.getMessage());
            Toast.makeText(getContext(), "Error al cargar los registros", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar datos cuando el fragmento vuelve a ser visible
        // para mostrar cambios realizados en otros fragmentos
        loadAllLaptops();
    }

    private void initializeSearchView() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                showSerialSuggestions(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Inicializar ListPopupWindow una sola vez
        listPopupWindow = new ListPopupWindow(requireContext());
        listPopupWindow.setAnchorView(searchEditText);
        listPopupWindow.setModal(false); // Cambiar a false para permitir interacción con el EditText
        listPopupWindow.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NEEDED);
        listPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    private void filterLaptops(String query) {
        List<Laptop> filteredList = new ArrayList<>();
        for (Laptop laptop : allLaptops) {
            if (matchesSearch(laptop, query.toLowerCase())) {
                filteredList.add(laptop);
            }
        }
        adapter.updateLaptops(filteredList);
    }

    private boolean matchesSearch(Laptop laptop, String query) {
        return laptop.getNumeroSerie().toLowerCase().contains(query) ||
               laptop.getMarca().toLowerCase().contains(query) ||
               laptop.getModelo().toLowerCase().contains(query) ||
               laptop.getEstado().toLowerCase().contains(query);
    }

    private void showSerialSuggestions(String query) {
        if (query.length() < 2) {
            if (listPopupWindow.isShowing()) {
                listPopupWindow.dismiss();
            }
            return;
        }

        List<String> suggestions = new ArrayList<>();
        for (Laptop laptop : allLaptops) {
            String serial = laptop.getNumeroSerie().toLowerCase();
            if (serial.contains(query.toLowerCase()) && !serial.equals(query.toLowerCase())) {
                suggestions.add(laptop.getNumeroSerie());
                if (suggestions.size() >= 5) break;
            }
        }

        if (!suggestions.isEmpty()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                R.layout.item_search_suggestion,
                R.id.text_suggestion,
                suggestions
            );
            
            listPopupWindow.setAdapter(adapter);
            listPopupWindow.setWidth(searchEditText.getWidth());
            listPopupWindow.setHeight(ListPopupWindow.WRAP_CONTENT);
            listPopupWindow.setBackgroundDrawable(getContext().getDrawable(R.drawable.popup_background));
            listPopupWindow.setVerticalOffset(8);
            
            listPopupWindow.setOnItemClickListener((parent, view, position, id) -> {
                searchEditText.setText(suggestions.get(position));
                searchEditText.setSelection(suggestions.get(position).length());
                filterLaptops(suggestions.get(position));
                listPopupWindow.dismiss();
            });
            
            if (!listPopupWindow.isShowing()) {
                listPopupWindow.show();
            }
        } else {
            if (listPopupWindow.isShowing()) {
                listPopupWindow.dismiss();
            }
        }
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
            Log.d("HistorialFragment", String.format("Laptop %d - Serie: %s, Marca: %s, Modelo: %s",
                i, laptop.getNumeroSerie(), laptop.getMarca(), laptop.getModelo()));
        }

        try {
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
            
        } catch (Exception e) {
            Log.e("HistorialFragment", "Error al preparar la exportación: " + e.getMessage());
            Toast.makeText(getContext(), "Error al preparar la exportación: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
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
        if (allLaptops == null || allLaptops.isEmpty()) {
            Log.d("HistorialFragment", "No hay laptops para exportar a CSV");
            Toast.makeText(getContext(), "No hay datos para exportar", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("HistorialFragment", "Preparando exportación a CSV de " + allLaptops.size() + " laptops");
        
        // Verificar datos antes de la exportación
        for (int i = 0; i < Math.min(3, allLaptops.size()); i++) {
            Laptop laptop = allLaptops.get(i);
            Log.d("HistorialFragment", "Laptop " + i + " - Serie: " + laptop.getNumeroSerie() 
                + ", Marca: " + laptop.getMarca()
                + ", Modelo: " + laptop.getModelo());
        }

        laptopsToExport = new ArrayList<>(allLaptops); // Crear una copia de la lista
        Log.d("HistorialFragment", "Lista de exportación CSV creada con " + laptopsToExport.size() + " laptops");
        
        // Crear intent para seleccionar ubicación
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        
        // Generar nombre de archivo sugerido
        String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                .format(new java.util.Date());
        String fileName = "Inventario_Laptops_" + timeStamp + ".csv";
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        
        Log.d("HistorialFragment", "Iniciando selección de archivo CSV: " + fileName);
        startActivityForResult(intent, CREATE_CSV_FILE);
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
                try {
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
                } catch (Exception e) {
                    Log.e("HistorialFragment", "Error durante la exportación a Excel: " + e.getMessage());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Error durante la exportación: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }
                }
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
            } else if (requestCode == CREATE_CSV_FILE) {
                Toast.makeText(getContext(), "Iniciando exportación de " + numRegistros + " registros a CSV...", Toast.LENGTH_SHORT).show();
                
                CSVExporter exporter = new CSVExporter(requireContext());
                exporter.exportToCSV(laptopsToExport, uri, (success, message) -> {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listPopupWindow != null) {
            listPopupWindow.dismiss();
        }
    }
}
