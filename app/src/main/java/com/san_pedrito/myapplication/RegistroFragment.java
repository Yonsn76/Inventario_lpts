package com.san_pedrito.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import android.graphics.Bitmap;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.san_pedrito.myapplication.db_kt.LaptopDatabaseHelper;
import com.san_pedrito.myapplication.db_kt.Laptop;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistroFragment extends Fragment {
    private ImageView imagenLaptop;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private Uri selectedImageUri;
    private Bitmap capturedImage;
    private ExecutorService executorService;
    private LaptopDatabaseHelper databaseHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executorService = Executors.newSingleThreadExecutor();
        databaseHelper = new LaptopDatabaseHelper(requireContext());
        
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    selectedImageUri = result.getData().getData();
                    capturedImage = null; // Clear any previous captured image
                    imagenLaptop.setImageURI(selectedImageUri);
                }
            }
        );

        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Bundle extras = result.getData().getExtras();
                    capturedImage = (Bitmap) extras.get("data");
                    selectedImageUri = null; // Clear any previous selected image
                    imagenLaptop.setImageBitmap(capturedImage);
                } else {
                    Toast.makeText(getContext(), "Error al tomar foto", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registro, container, false);
        
        imagenLaptop = view.findViewById(R.id.imagenLaptop);
        MaterialButton btnSeleccionarImagen = view.findViewById(R.id.btnSeleccionarImagen);
        MaterialButton btnTomarFoto = view.findViewById(R.id.btnTomarFoto);
        MaterialButton btnRegistrar = view.findViewById(R.id.btnRegistrar);
        
        // Input fields
        TextInputEditText editMarca = view.findViewById(R.id.editMarca);
        TextInputEditText editModelo = view.findViewById(R.id.editModelo);
        TextInputEditText editSerialNumber = view.findViewById(R.id.editSerialNumber);
        TextInputEditText editEstado = view.findViewById(R.id.editEstado);
        TextInputEditText editObservaciones = view.findViewById(R.id.editObservaciones);
        
        btnSeleccionarImagen.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        btnTomarFoto.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                cameraLauncher.launch(takePictureIntent);
            } else {
                Toast.makeText(getContext(), "Camara no disponible", Toast.LENGTH_SHORT).show();
            }
        });
        
        btnRegistrar.setOnClickListener(v -> {
            // Validate input fields
            String marca = editMarca.getText().toString().trim();
            String modelo = editModelo.getText().toString().trim();
            String numeroSerie = editSerialNumber.getText().toString().trim();
            String estado = editEstado.getText().toString().trim();
            String observaciones = editObservaciones.getText().toString().trim();
            
            if (marca.isEmpty() || modelo.isEmpty() || numeroSerie.isEmpty() || estado.isEmpty()) {
                Toast.makeText(getContext(), "Por favor complete todos los campos obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (selectedImageUri == null && capturedImage == null) {
                Toast.makeText(getContext(), "Por favor seleccione o tome una foto", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Create image path (in a real app, you would save the image to storage and get the path)
            String imagePath = selectedImageUri != null ? selectedImageUri.toString() : "captured_image_" + System.currentTimeMillis();
            
            // Save to database in background thread
            executorService.execute(() -> {
                long id = databaseHelper.agregarLaptop(marca, modelo, numeroSerie, estado, observaciones, imagePath);
                
                // Update UI on main thread
                getActivity().runOnUiThread(() -> {
                    if (id > 0) {
                        Toast.makeText(getContext(), "Laptop registrada correctamente", Toast.LENGTH_SHORT).show();
                        // Clear form
                        editMarca.setText("");
                        editModelo.setText("");
                        editSerialNumber.setText("");
                        editEstado.setText("");
                        editObservaciones.setText("");
                        imagenLaptop.setImageResource(android.R.color.transparent);
                        selectedImageUri = null;
                        capturedImage = null;
                    } else {
                        Toast.makeText(getContext(), "Error al registrar laptop", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        return view;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}