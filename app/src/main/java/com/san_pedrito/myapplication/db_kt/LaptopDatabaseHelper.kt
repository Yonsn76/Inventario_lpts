package com.san_pedrito.myapplication.db_kt

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Clase auxiliar para gestionar la base de datos de laptops.
 * Proporciona métodos para crear, leer, actualizar y eliminar registros de laptops.
 */
class LaptopDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "LaptopsDB"
        private const val TABLA_LAPTOPS = "laptops"
        private const val CLAVE_ID = "id"
        private const val CLAVE_MARCA = "marca"
        private const val CLAVE_MODELO = "modelo"
        private const val CLAVE_NUMERO_SERIE = "serial_number"
        private const val CLAVE_ESTADO = "estado"
        private const val CLAVE_OBSERVACIONES = "observaciones"
        private const val CLAVE_RUTA_IMAGEN = "image_path"
        private const val CLAVE_FECHA_HORA = "timestamp"
    }

    /**
     * Crea la tabla de laptops en la base de datos.
     */
    override fun onCreate(db: SQLiteDatabase) {
        val CREAR_TABLA_LAPTOPS = ("CREATE TABLE " + TABLA_LAPTOPS + "("
                + CLAVE_ID + " INTEGER PRIMARY KEY,"
                + CLAVE_MARCA + " TEXT,"
                + CLAVE_MODELO + " TEXT,"
                + CLAVE_NUMERO_SERIE + " TEXT,"
                + CLAVE_ESTADO + " TEXT,"
                + CLAVE_OBSERVACIONES + " TEXT,"
                + CLAVE_RUTA_IMAGEN + " TEXT,"
                + CLAVE_FECHA_HORA + " TEXT" + ")")
        db.execSQL(CREAR_TABLA_LAPTOPS)
    }

    /**
     * Actualiza la estructura de la base de datos cuando cambia la versión.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Añadir columna de fecha y hora a la tabla existente
            db.execSQL("ALTER TABLE $TABLA_LAPTOPS ADD COLUMN $CLAVE_FECHA_HORA TEXT")
        } else {
            // Recrear la tabla como alternativa
            db.execSQL("DROP TABLE IF EXISTS $TABLA_LAPTOPS")
            onCreate(db)
        }
    }

    /**
     * Añade una nueva laptop a la base de datos.
     * @return ID del registro insertado
     */
    fun agregarLaptop(marca: String, modelo: String, numeroSerie: String, estado: String, observaciones: String, rutaImagen: String?): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(CLAVE_MARCA, marca)
        values.put(CLAVE_MODELO, modelo)
        values.put(CLAVE_NUMERO_SERIE, numeroSerie)
        values.put(CLAVE_ESTADO, estado)
        values.put(CLAVE_OBSERVACIONES, observaciones)
        values.put(CLAVE_RUTA_IMAGEN, rutaImagen)
        
        // Añadir fecha y hora actual
        val fechaHora = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        values.put(CLAVE_FECHA_HORA, fechaHora)
        
        return db.insert(TABLA_LAPTOPS, null, values)
    }

    /**
     * Obtiene una laptop por su ID.
     * @return Objeto Laptop o null si no se encuentra
     */
    fun obtenerLaptop(id: Long): Laptop? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLA_LAPTOPS, arrayOf(CLAVE_ID, CLAVE_MARCA, CLAVE_MODELO, CLAVE_NUMERO_SERIE, CLAVE_ESTADO, CLAVE_OBSERVACIONES, CLAVE_RUTA_IMAGEN, CLAVE_FECHA_HORA),
            "$CLAVE_ID=?", arrayOf(id.toString()), null, null, null, null)
        return if (cursor.moveToFirst()) {
            val laptopId = cursor.getLong(cursor.getColumnIndexOrThrow(CLAVE_ID))
            val marca = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_MARCA))
            val modelo = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_MODELO))
            val numeroSerie = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_NUMERO_SERIE))
            val estado = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_ESTADO))
            val observaciones = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_OBSERVACIONES))
            val rutaImagen = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_RUTA_IMAGEN))
            val fechaHora = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_FECHA_HORA))
            cursor.close()
            Laptop(laptopId, marca, modelo, numeroSerie, estado, observaciones, rutaImagen, fechaHora)
        } else {
            cursor.close()
            null
        }
    }

    /**
     * Obtiene todas las laptops de la base de datos.
     * @return Lista de objetos Laptop
     */
    fun obtenerTodasLaptops(): List<Laptop> {
        val listaLaptops = mutableListOf<Laptop>()
        val consultaSeleccion = "SELECT * FROM $TABLA_LAPTOPS"
        val db = this.readableDatabase
        val cursor = db.rawQuery(consultaSeleccion, null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(CLAVE_ID))
                val marca = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_MARCA))
                val modelo = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_MODELO))
                val numeroSerie = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_NUMERO_SERIE))
                val estado = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_ESTADO))
                val observaciones = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_OBSERVACIONES))
                val rutaImagen = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_RUTA_IMAGEN))
                val fechaHora = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_FECHA_HORA))
                listaLaptops.add(Laptop(id, marca, modelo, numeroSerie, estado, observaciones, rutaImagen, fechaHora))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return listaLaptops
    }

    /**
     * Actualiza la información de una laptop existente.
     * @return Número de filas afectadas
     */
    fun actualizarLaptop(id: Long, marca: String, modelo: String, numeroSerie: String, estado: String, observaciones: String, rutaImagen: String?): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(CLAVE_MARCA, marca)
        values.put(CLAVE_MODELO, modelo)
        values.put(CLAVE_NUMERO_SERIE, numeroSerie)
        values.put(CLAVE_ESTADO, estado)
        values.put(CLAVE_OBSERVACIONES, observaciones)
        values.put(CLAVE_RUTA_IMAGEN, rutaImagen)
        
        // Añadir fecha y hora actual al actualizar
        val fechaHora = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        values.put(CLAVE_FECHA_HORA, fechaHora)
        
        return db.update(TABLA_LAPTOPS, values, "$CLAVE_ID=?", arrayOf(id.toString()))
    }

    /**
     * Elimina una laptop de la base de datos.
     * @return Número de filas afectadas
     */
    fun eliminarLaptop(id: Long): Int {
        val db = this.writableDatabase
        return db.delete(TABLA_LAPTOPS, "$CLAVE_ID=?", arrayOf(id.toString()))
    }

    /**
     * Elimina todas las laptops de la base de datos.
     * @return Número de filas afectadas
     */
    fun eliminarTodasLaptops(): Int {
        val db = this.writableDatabase
        return db.delete(TABLA_LAPTOPS, null, null)
    }

    /**
     * Busca laptops que coincidan con la consulta en marca, modelo o número de serie.
     * @return Lista de objetos Laptop que coinciden con la búsqueda
     */
    fun buscarLaptops(consulta: String): List<Laptop> {
        val listaLaptops = mutableListOf<Laptop>()
        val consultaSeleccion = "SELECT * FROM $TABLA_LAPTOPS WHERE $CLAVE_MARCA LIKE '%$consulta%' OR $CLAVE_MODELO LIKE '%$consulta%' OR $CLAVE_NUMERO_SERIE LIKE '%$consulta%'"
        val db = this.readableDatabase
        val cursor = db.rawQuery(consultaSeleccion, null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(CLAVE_ID))
                val marca = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_MARCA))
                val modelo = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_MODELO))
                val numeroSerie = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_NUMERO_SERIE))
                val estado = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_ESTADO))
                val observaciones = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_OBSERVACIONES))
                val rutaImagen = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_RUTA_IMAGEN))
                val fechaHora = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_FECHA_HORA))
                listaLaptops.add(Laptop(id, marca, modelo, numeroSerie, estado, observaciones, rutaImagen, fechaHora))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return listaLaptops
    }

    /**
     * Obtiene una laptop por su número de serie.
     * @return Objeto Laptop o null si no se encuentra
     */
    fun obtenerLaptopPorNumeroSerie(numeroSerie: String): Laptop? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLA_LAPTOPS, arrayOf(CLAVE_ID, CLAVE_MARCA, CLAVE_MODELO, CLAVE_NUMERO_SERIE, CLAVE_ESTADO, CLAVE_OBSERVACIONES, CLAVE_RUTA_IMAGEN, CLAVE_FECHA_HORA),
            "$CLAVE_NUMERO_SERIE=?", arrayOf(numeroSerie), null, null, null, null)
        return if (cursor.moveToFirst()) {
            val laptopId = cursor.getLong(cursor.getColumnIndexOrThrow(CLAVE_ID))
            val marca = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_MARCA))
            val modelo = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_MODELO))
            val serie = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_NUMERO_SERIE))
            val estado = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_ESTADO))
            val observaciones = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_OBSERVACIONES))
            val rutaImagen = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_RUTA_IMAGEN))
            val fechaHora = cursor.getString(cursor.getColumnIndexOrThrow(CLAVE_FECHA_HORA))
            cursor.close()
            Laptop(laptopId, marca, modelo, serie, estado, observaciones, rutaImagen, fechaHora)
        } else {
            cursor.close()
            null
        }
    }
}