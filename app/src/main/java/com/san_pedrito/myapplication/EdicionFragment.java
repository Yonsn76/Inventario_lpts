package com.san_pedrito.myapplication;

/**
 * Fragment para la edición de registros de laptops en la base de datos.
 * Permite buscar laptops por número de serie, modificar sus datos y guardar los cambios.
 * También ofrece funcionalidad para eliminar registros y capturar imágenes.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.san_pedrito.myapplication.db_kt.Laptop;
import com.san_pedrito.myapplication.db_kt.LaptopDatabaseHelper;

public class EdicionFragment extends Fragment {
    // Elementos de la interfaz de usuario
    private ImageView imagenLaptop;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private TextInputEditText editSearchSerialNumber, editMarca, editModelo, editNroSerie, editEstado, editObservacion;
    private LaptopDatabaseHelper dbHelper;
    private Laptop currentLaptop;
    private String currentImagePath;

    /**
     * Inicializa el fragmento, la conexión a la base de datos y los launchers para
     * la selección de imágenes y la cámara.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new LaptopDatabaseHelper(requireContext());
        
        // Launcher para seleccionar imágenes de la galería
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Uri imageUri = result.getData().getData();
                    imagenLaptop.setImageURI(imageUri);
                    currentImagePath = imageUri.toString();
                }
            }
        );

        // Launcher para capturar imágenes con la cámara
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Bundle extras = result.getData().getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    imagenLaptop.setImageBitmap(imageBitmap);
                    // TODO: Guardar la imagen en un archivo y obtener la ruta
                }
            }
        );
    }

    /**
     * Crea y configura la vista del fragmento, inicializa los elementos de la UI
     * y configura los listeners para los botones y campos de búsqueda.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edicion, container, false);
        
        // Inicialización de los elementos de la interfaz de usuario
        imagenLaptop = view.findViewById(R.id.imagenLaptop);
        editSearchSerialNumber = view.findViewById(R.id.editSearchSerialNumber);
        editMarca = view.findViewById(R.id.editMarca);
        editModelo = view.findViewById(R.id.editModelo);
        editNroSerie = view.findViewById(R.id.editNroSerie);
        editEstado = view.findViewById(R.id.editEstado);
        editObservacion = view.findViewById(R.id.editObservacion);
        
        FloatingActionButton fabCamera = view.findViewById(R.id.fabCamera);
        FloatingActionButton fabDelete = view.findViewById(R.id.fabDelete);
        FloatingActionButton fabSave = view.findViewById(R.id.fabSave);

        // Configuración de la funcionalidad de búsqueda
        ((com.google.android.material.textfield.TextInputLayout) view.findViewById(R.id.searchLayout)).setEndIconOnClickListener(v -> searchLaptop());
        editSearchSerialNumber.setOnEditorActionListener((v, actionId, event) -> {
            searchLaptop();
            return true;
        });

        // Botón para tomar una foto con la cámara
        fabCamera.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                cameraLauncher.launch(takePictureIntent);
            } else {
                Toast.makeText(getContext(), "Cámara no disponible", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón para eliminar el registro actual
        fabDelete.setOnClickListener(v -> {
            if (currentLaptop != null) {
                new AlertDialog.Builder(getContext())
                    .setTitle("Confirmar eliminación")
                    .setMessage("¿Estás seguro de que quieres eliminar este registro?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        dbHelper.eliminarLaptop(currentLaptop.getId());
                        clearFields();
                        Toast.makeText(getContext(), "Registro eliminado", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
            }
        });

        // Botón para guardar los cambios realizados
        fabSave.setOnClickListener(v -> {
            if (validateFields()) {
                saveLaptop();
            }
        });

        return view;
    }

    /**
     * Busca una laptop en la base de datos por su número de serie
     * y muestra sus datos si se encuentra.
     */
    private void searchLaptop() {
        String serialNumber = editSearchSerialNumber.getText().toString().trim();
        if (!serialNumber.isEmpty()) {
            Laptop laptop = dbHelper.obtenerLaptopPorNumeroSerie(serialNumber);
            if (laptop != null) {
                displayLaptopData(laptop);
                Toast.makeText(getContext(), "Laptop encontrada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "No se encontró la laptop", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Muestra los datos de la laptop encontrada en los campos de edición.
     * @param laptop El objeto Laptop cuyos datos se mostrarán
     */
    private void displayLaptopData(Laptop laptop) {
        currentLaptop = laptop;
        editMarca.setText(laptop.getMarca());
        editModelo.setText(laptop.getModelo());
        editNroSerie.setText(laptop.getNumeroSerie());
        editEstado.setText(laptop.getEstado());
        editObservacion.setText(laptop.getObservaciones());
        currentImagePath = laptop.getRutaImagen();
        if (currentImagePath != null) {
            imagenLaptop.setImageURI(Uri.parse(currentImagePath));
        }
    }

    /**
     * Guarda los cambios realizados a la laptop actual en la base de datos.
     * Recoge los datos de los campos de edición y los envía al helper de la base de datos.
     */
    private void saveLaptop() {
        String marca = editMarca.getText().toString().trim();
        String modelo = editModelo.getText().toString().trim();
        String nroSerie = editNroSerie.getText().toString().trim();
        String estado = editEstado.getText().toString().trim();
        String observacion = editObservacion.getText().toString().trim();

        if (currentLaptop != null) {
            int result = dbHelper.actualizarLaptop(
                currentLaptop.getId(),
                marca,
                modelo,
                nroSerie,
                estado,
                observacion,
                currentImagePath
            );
            if (result > 0) {
                Toast.makeText(getContext(), "Cambios guardados exitosamente", Toast.LENGTH_SHORT).show();
                clearFields();
            } else {
                Toast.makeText(getContext(), "Error al guardar los cambios", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Valida que todos los campos obligatorios estén completos antes de guardar.
     * @return true si todos los campos obligatorios tienen datos, false en caso contrario
     */
    private boolean validateFields() {
        if (currentLaptop == null) {
            Toast.makeText(getContext(), "Primero busque una laptop para editar", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (editMarca.getText().toString().trim().isEmpty() ||
            editModelo.getText().toString().trim().isEmpty() ||
            editNroSerie.getText().toString().trim().isEmpty() ||
            editEstado.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "Por favor complete todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Limpia todos los campos de edición y reinicia el estado del fragmento
     * después de guardar o eliminar un registro.
     */
    private void clearFields() {
        currentLaptop = null;
        editSearchSerialNumber.setText("");
        editMarca.setText("");
        editModelo.setText("");
        editNroSerie.setText("");
        editEstado.setText("");
        editObservacion.setText("");
        imagenLaptop.setImageResource(android.R.color.darker_gray);
        currentImagePath = null;
    }
}