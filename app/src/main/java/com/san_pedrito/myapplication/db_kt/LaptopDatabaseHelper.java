package com.san_pedrito.myapplication.db_kt;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class LaptopDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "laptops.db";
    private static final int DATABASE_VERSION = 1;

    public LaptopDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LAPTOPS_TABLE = "CREATE TABLE laptops (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "marca TEXT," +
                "modelo TEXT," +
                "numero_serie TEXT," +
                "estado TEXT," +
                "observaciones TEXT," +
                "ruta_imagen TEXT," +
                "fecha_hora DATETIME DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(CREATE_LAPTOPS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS laptops");
        onCreate(db);
    }

    public long agregarLaptop(String marca, String modelo, String numeroSerie, 
                            String estado, String observaciones, String rutaImagen) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("marca", marca);
        values.put("modelo", modelo);
        values.put("numero_serie", numeroSerie);
        values.put("estado", estado);
        values.put("observaciones", observaciones);
        values.put("ruta_imagen", rutaImagen);
        return db.insert("laptops", null, values);
    }

    public List<Laptop> obtenerTodasLaptops() {
        List<Laptop> laptops = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("laptops", null, null, null, null, null, "fecha_hora DESC");

        if (cursor.moveToFirst()) {
            do {
                Laptop laptop = new Laptop(
                    cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("marca")),
                    cursor.getString(cursor.getColumnIndexOrThrow("modelo")),
                    cursor.getString(cursor.getColumnIndexOrThrow("numero_serie")),
                    cursor.getString(cursor.getColumnIndexOrThrow("estado")),
                    cursor.getString(cursor.getColumnIndexOrThrow("observaciones")),
                    cursor.getString(cursor.getColumnIndexOrThrow("ruta_imagen")),
                    cursor.getString(cursor.getColumnIndexOrThrow("fecha_hora"))
                );
                laptops.add(laptop);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return laptops;
    }

    public Laptop obtenerLaptopPorNumeroSerie(String numeroSerie) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("laptops", null, "numero_serie = ?", 
            new String[]{numeroSerie}, null, null, null);

        Laptop laptop = null;
        if (cursor.moveToFirst()) {
            laptop = new Laptop(
                cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("marca")),
                cursor.getString(cursor.getColumnIndexOrThrow("modelo")),
                cursor.getString(cursor.getColumnIndexOrThrow("numero_serie")),
                cursor.getString(cursor.getColumnIndexOrThrow("estado")),
                cursor.getString(cursor.getColumnIndexOrThrow("observaciones")),
                cursor.getString(cursor.getColumnIndexOrThrow("ruta_imagen")),
                cursor.getString(cursor.getColumnIndexOrThrow("fecha_hora"))
            );
        }
        cursor.close();
        return laptop;
    }

    public int actualizarLaptop(long id, String marca, String modelo, String numeroSerie,
                               String estado, String observaciones, String rutaImagen) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("marca", marca);
        values.put("modelo", modelo);
        values.put("numero_serie", numeroSerie);
        values.put("estado", estado);
        values.put("observaciones", observaciones);
        values.put("ruta_imagen", rutaImagen);
        return db.update("laptops", values, "id = ?", new String[]{String.valueOf(id)});
    }

    public void eliminarLaptop(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("laptops", "id = ?", new String[]{String.valueOf(id)});
    }

    public boolean existeNumeroSerie(String numeroSerie) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
            "laptops",
            new String[]{"numero_serie"},
            "numero_serie = ?",
            new String[]{numeroSerie},
            null,
            null,
            null
        );
        boolean existe = cursor.getCount() > 0;
        cursor.close();
        return existe;
    }
} 