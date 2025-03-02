package com.san_pedrito.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.san_pedrito.myapplication.db_kt.LaptopDatabaseHelper;
import com.san_pedrito.myapplication.db_kt.Laptop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.media.MediaScannerConnection;
import android.os.Environment;

public class RegistroFragment extends Fragment {
    private ImageView imagenLaptop;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private Uri selectedImageUri;
    private Bitmap capturedImage;
    private ExecutorService executorService;
    private LaptopDatabaseHelper databaseHelper;
    private String savedImagePath;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executorService = Executors.newSingleThreadExecutor();
        databaseHelper = new LaptopDatabaseHelper(requireContext());
        
        // Initialize permission launcher
        requestCameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    // Permission granted, launch camera
                    launchCamera();
                } else {
                    Toast.makeText(getContext(), "Se requiere permiso de cámara para esta función", Toast.LENGTH_SHORT).show();
                }
            });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registro, container, false);
        
        imagenLaptop = view.findViewById(R.id.imagenLaptop);
        MaterialButton btnSeleccionarImagen = view.findViewById(R.id.btnSeleccionarImagen);
        MaterialButton btnTomarFoto = view.findViewById(R.id.btnTomarFoto);
        MaterialButton btnRegistrar = view.findViewById(R.id.btnRegistrar);
        
        // Initialize launchers after imagenLaptop is available
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    selectedImageUri = result.getData().getData();
                    capturedImage = null; // Clear any previous captured image
                    saveImageFromUri(selectedImageUri);
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
                    saveImageFromBitmap(capturedImage);
                    imagenLaptop.setImageBitmap(capturedImage);
                } else {
                    Toast.makeText(getContext(), "Error al tomar foto", Toast.LENGTH_SHORT).show();
                }
            }
        );
        
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
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA);
            } else {
                launchCamera();
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
            
            String imagePath = savedImagePath;
            
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
    
    private void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                cameraLauncher.launch(takePictureIntent);
            } else {
                Toast.makeText(getContext(), "Camara no disponible", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al abrir la cámara", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void saveImageFromUri(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            saveImageFromBitmap(bitmap);
            if (inputStream != null) inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al guardar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageFromBitmap(Bitmap bitmap) {
        try {
            File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File appDir = new File(picturesDir, "Inventario_LPTS");
            if (!appDir.exists()) {
                appDir.mkdirs();
            }

            String fileName = "laptop_" + System.currentTimeMillis() + ".jpg";
            File file = new File(appDir, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

            // Add the image to the gallery
            MediaScannerConnection.scanFile(requireContext(),
                    new String[]{file.getAbsolutePath()}, null, null);

            savedImagePath = file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al guardar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}