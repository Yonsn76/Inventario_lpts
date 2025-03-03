package com.san_pedrito.myapplication.export_metodo

import android.content.Context
import android.net.Uri
import android.util.Log
import com.san_pedrito.myapplication.db_kt.Laptop
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStreamWriter

class CSVExporter(private val context: Context) {

    fun exportToCSV(laptops: List<Laptop>, uri: Uri, onComplete: (Boolean, String) -> Unit) {
        Thread {
            var writer: BufferedWriter? = null
            try {
                if (laptops.isEmpty()) {
                    Log.e("CSVExporter", "Lista de laptops vacía")
                    onComplete(false, "No hay datos para exportar")
                    return@Thread
                }

                Log.d("CSVExporter", "Iniciando exportación con ${laptops.size} registros")

                context.contentResolver.openOutputStream(uri)?.let { outputStream ->
                    writer = BufferedWriter(OutputStreamWriter(outputStream, "UTF-8"))
                    
                    // Escribir BOM para Excel
                    writer?.write("\uFEFF")
                    
                    // Escribir encabezados
                    val headers = arrayOf("Número de Serie", "Marca", "Modelo", "Estado", "Observaciones", "Fecha/Hora")
                    writer?.write(headers.joinToString(",") { escapeCSV(it) })
                    writer?.newLine()
                    
                    Log.d("CSVExporter", "Encabezados escritos")

                    // Escribir datos
                    var rowsCreated = 0
                    laptops.forEach { laptop ->
                        val row = arrayOf(
                            laptop.numeroSerie,
                            laptop.marca,
                            laptop.modelo,
                            laptop.estado,
                            laptop.observaciones,
                            laptop.fechaHora
                        ).joinToString(",") { escapeCSV(it) }
                        
                        writer?.write(row)
                        writer?.newLine()
                        rowsCreated++
                    }

                    Log.d("CSVExporter", "Filas de datos creadas: $rowsCreated")
                    writer?.flush()
                    
                    onComplete(true, "Archivo CSV guardado exitosamente con ${laptops.size} registros")
                } ?: throw IOException("No se pudo abrir el archivo para escritura")

            } catch (e: Exception) {
                Log.e("CSVExporter", "Error durante la exportación: ${e.message}")
                e.printStackTrace()
                onComplete(false, "Error al exportar: ${e.message}")
            } finally {
                try {
                    writer?.close()
                    Log.d("CSVExporter", "Archivo cerrado correctamente")
                } catch (e: IOException) {
                    Log.e("CSVExporter", "Error al cerrar el archivo: ${e.message}")
                }
            }
        }.start()
    }

    private fun escapeCSV(value: String?): String {
        if (value == null) return "\"\""
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
} 