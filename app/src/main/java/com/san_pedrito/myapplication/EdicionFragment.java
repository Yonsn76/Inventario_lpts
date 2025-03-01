package com.san_pedrito.myapplication;

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

public class EdicionFragment extends Fragment {
    private ImageView imagenLaptop;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Uri imageUri = result.getData().getData();
                    imagenLaptop.setImageURI(imageUri);
                }
            }
        );

        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Bundle extras = result.getData().getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    imagenLaptop.setImageBitmap(imageBitmap);
                }
            }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edicion, container, false);
        
        imagenLaptop = view.findViewById(R.id.imagenLaptop);
        FloatingActionButton fabCamera = view.findViewById(R.id.fabCamera);
        FloatingActionButton fabDelete = view.findViewById(R.id.fabDelete);
        FloatingActionButton fabSave = view.findViewById(R.id.fabSave);

        fabCamera.setOnClickListener(v -> {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                cameraLauncher.launch(takePictureIntent);
            } else {
                Toast.makeText(getContext(), "Cámara no disponible", Toast.LENGTH_SHORT).show();
            }
        });

        fabDelete.setOnClickListener(v -> {
            // Add delete confirmation dialog
            new AlertDialog.Builder(getContext())
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que quieres eliminar este registro?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Add your delete logic here
                    Toast.makeText(getContext(), "Registro eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
        });

        fabSave.setOnClickListener(v -> {
            // Add your save logic here
            Toast.makeText(getContext(), "Guardando cambios...", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}