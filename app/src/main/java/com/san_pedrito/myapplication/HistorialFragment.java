package com.san_pedrito.myapplication;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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

    private void exportToExcel() {
        // Mostrar mensaje de inicio de exportación
        Toast.makeText(getContext(), "Iniciando exportación a Excel...", Toast.LENGTH_SHORT).show();
        
        // Create a background thread for Excel export
        new Thread(() -> {
            // Use a try-with-resources to ensure proper resource cleanup
            Workbook workbook = null;
            OutputStream outputStream = null;
            
            try {
                // Verificar si hay datos para exportar
                if (allLaptops == null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "No hay datos para exportar", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                
                // Create a new workbook
                workbook = new HSSFWorkbook();
                Sheet sheet = workbook.createSheet("Inventario Laptops");

                // Create header row with a cell style for headers
                CellStyle headerStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                
                Row headerRow = sheet.createRow(0);
                String[] headers = {"Número de Serie", "Marca", "Modelo", "Estado", "Observaciones", "Fecha/Hora"};
                
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Add data rows
                if (!allLaptops.isEmpty()) {
                    int rowNum = 1;
                    for (Laptop laptop : allLaptops) {
                        Row row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue(laptop.getNumeroSerie() != null ? laptop.getNumeroSerie() : "");
                        row.createCell(1).setCellValue(laptop.getMarca() != null ? laptop.getMarca() : "");
                        row.createCell(2).setCellValue(laptop.getModelo() != null ? laptop.getModelo() : "");
                        row.createCell(3).setCellValue(laptop.getEstado() != null ? laptop.getEstado() : "");
                        row.createCell(4).setCellValue(laptop.getObservaciones() != null ? laptop.getObservaciones() : "");
                        row.createCell(5).setCellValue(laptop.getFechaHora() != null ? laptop.getFechaHora() : "");
                    }
                } else {
                    // Add a message if there's no data
                    Row row = sheet.createRow(1);
                    Cell cell = row.createCell(0);
                    cell.setCellValue("No hay datos disponibles");
                }

                // Auto-size columns
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                // Generate a unique filename
                String fileName = "Inventario_Laptops_" + System.currentTimeMillis() + ".xls";

                try {
                    // Create content values for the file
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.ms-excel");
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
    
                    // Get a URI for the file
                    Uri uri = requireContext().getContentResolver().insert(MediaStore.Files.getContentUri("external"), values);
                    
                    if (uri != null) {
                        // Open an output stream to write the workbook
                        outputStream = requireContext().getContentResolver().openOutputStream(uri);
                        
                        if (outputStream != null) {
                            workbook.write(outputStream);
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Archivo Excel guardado en Descargas: " + fileName, Toast.LENGTH_LONG).show();
                            });
                        } else {
                            throw new IOException("No se pudo abrir el archivo para escritura");
                        }
                    } else {
                        throw new IOException("No se pudo crear el archivo");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    final String errorMessage = e.getMessage();
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error de E/S: " + errorMessage, Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                final String errorMessage = e.getMessage() != null ? e.getMessage() : "Error desconocido";
                final String errorClass = e.getClass().getSimpleName();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error al exportar: " + errorClass + " - " + errorMessage, Toast.LENGTH_LONG).show();
                });
            } finally {
                // Close resources in finally block to ensure they're always closed
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (workbook != null) {
                        workbook.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    final String closeError = e.getMessage();
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error al cerrar recursos: " + closeError, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }).start();
    }

    private void exportToPDF() {
        Toast.makeText(getContext(), "Exportación a PDF será implementada próximamente", Toast.LENGTH_SHORT).show();
    }

    private void exportToCSV() {
        Toast.makeText(getContext(), "Exportación a CSV será implementada próximamente", Toast.LENGTH_SHORT).show();
    }
}
