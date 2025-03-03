package com.san_pedrito.myapplication;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

public class MainActivity extends AppCompatActivity {
    private SoundPool soundPool;
    private int soundId = 0;
    private boolean soundLoaded = false;
    private int currentSelectedItemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        // Inicializar SoundPool
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();

        soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
            if (status == 0) {
                soundLoaded = true;
            } else {
                Toast.makeText(this, "Error al cargar el sonido", Toast.LENGTH_SHORT).show();
            }
        });

        soundId = soundPool.load(this, R.raw.tap_sound, 1);

        BottomNavigationView navView = findViewById(R.id.navegacionInferior);
        setupCustomNavigation(navView);

        // Seleccionar inicio por defecto sin reproducir sonido
        currentSelectedItemId = R.id.menuInicio;
        applySelectedStyle(navView, navView.getMenu().findItem(R.id.menuInicio), R.color.nav_inicio);
        navView.setSelectedItemId(R.id.menuInicio);

        // Mostrar InicioFragment por defecto
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contenedorFragmentos, new InicioFragment())
                    .commit();
        }
    }

    private void setupCustomNavigation(BottomNavigationView navView) {
        navView.setItemIconTintList(null);
        navView.setItemTextColor(null);

        navView.setOnItemSelectedListener(item -> {
            if (item.getItemId() != currentSelectedItemId) {
                playSound();
                currentSelectedItemId = item.getItemId();
            }

            resetAllItems(navView);

            int itemId = item.getItemId();
            if (itemId == R.id.menuInicio) {
                applySelectedStyle(navView, item, R.color.nav_inicio);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.contenedorFragmentos, new InicioFragment())
                        .commit();
                return true;
            } else if (itemId == R.id.menuRegistro) {
                applySelectedStyle(navView, item, R.color.nav_registro);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.contenedorFragmentos, new RegistroFragment())
                        .commit();
                return true;
            } else if (itemId == R.id.menuHistorial) {
                applySelectedStyle(navView, item, R.color.nav_historial);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.contenedorFragmentos, new HistorialFragment())
                        .commit();
                return true;
            } else if (itemId == R.id.menuEdicion) {
                applySelectedStyle(navView, item, R.color.nav_edicion);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.contenedorFragmentos, new EdicionFragment())
                        .commit();
                return true;
            }
            return false;
        });
    }

    private void playSound() {
        try {
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.tap_sound);
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void resetAllItems(BottomNavigationView navView) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navView.getChildAt(0);
        for (int i = 0; i < menuView.getChildCount(); i++) {
            BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(i);
            itemView.setBackground(null);
            itemView.setPadding(0, 0, 0, 0);
            
            ColorStateList grayColor = ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.nav_unselected));
            itemView.setIconTintList(grayColor);
            itemView.setTextColor(grayColor);
        }
    }

    private void applySelectedStyle(BottomNavigationView navView, MenuItem item, int colorResId) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navView.getChildAt(0);
        int index = getMenuItemIndex(navView, item.getItemId());

        if (index >= 0) {
            BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(index);

            ShapeAppearanceModel shapeAppearanceModel = ShapeAppearanceModel.builder()
                    .setAllCornerSizes(getResources().getDimension(R.dimen.nav_item_corner_radius))
                    .build();

            int color = ContextCompat.getColor(this, colorResId);
            MaterialShapeDrawable shapeDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
            shapeDrawable.setFillColor(ColorStateList.valueOf(Color.TRANSPARENT));
            shapeDrawable.setStrokeColor(ColorStateList.valueOf(color));
            shapeDrawable.setStrokeWidth(getResources().getDimension(R.dimen.nav_item_stroke_width));

            itemView.setBackground(shapeDrawable);

            int padding = getResources().getDimensionPixelSize(R.dimen.nav_item_padding);
            itemView.setPadding(padding, padding / 2, padding, padding / 2);

            ColorStateList colorStateList = ColorStateList.valueOf(color);
            itemView.setIconTintList(colorStateList);
            itemView.setTextColor(colorStateList);
        }
    }

    private int getMenuItemIndex(BottomNavigationView navView, int itemId) {
        for (int i = 0; i < navView.getMenu().size(); i++) {
            if (navView.getMenu().getItem(i).getItemId() == itemId) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.release();
    }
}