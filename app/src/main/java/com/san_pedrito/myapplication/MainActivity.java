package com.san_pedrito.myapplication;
import android.view.WindowManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView navegacionInferior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();  // Add this line
        setContentView(R.layout.activity_main);
    
        // Show InicioFragment by default
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.contenedorFragmentos, new InicioFragment())
                .commit();
        }

        navegacionInferior = findViewById(R.id.navegacionInferior);
        navegacionInferior.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menuInicio) {
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contenedorFragmentos, new InicioFragment())
                    .commit();
                return true;
            } else if (itemId == R.id.menuRegistro) {
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contenedorFragmentos, new RegistroFragment())
                    .commit();
                return true;
            } else if (itemId == R.id.menuHistorial) {
                // Manejar clic en Historial
                return true;
            }
            return false;
        });
    }
}