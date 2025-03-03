package com.san_pedrito.myapplication.export_metodo

import android.content.Context
import android.net.Uri
import android.util.Log
import com.opencsv.CSVWriterBuilder
import com.san_pedrito.myapplication.db_kt.Laptop
import java.io.IOException
import java.io.OutputStreamWriter

class ExcelExporter(private val context: Context) {

    fun exportToExcel(laptops: List<Laptop>, uri: Uri, onComplete: (Boolean, String) -> Unit) {
        Thread {
            try {
                Log.d("ExcelExporter", "Iniciando exportación de datos")

                if (laptops.isEmpty()) {
                    onComplete(false, "No hay datos para exportar")
                    return@Thread
                }

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    // Escribir BOM para compatibilidad con Excel
                    outputStream.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
                    
                    // Crear el escritor CSV con configuración para Excel
                    OutputStreamWriter(outputStream, "UTF-8").use { writer ->
                        CSVWriterBuilder(writer)
                            .withSeparator(';')  // Usar punto y coma para mejor compatibilidad con Excel
                            .withQuoteChar('"')
                            .build().use { csvWriter ->
                                
                                // Escribir encabezados
                                val headers = arrayOf(
                                    "Número de Serie",
                                    "Marca",
                                    "Modelo",
                                    "Estado",
                                    "Observaciones",
                                    "Fecha/Hora"
                                )
                                csvWriter.writeNext(headers)

                                // Escribir datos
                                laptops.forEach { laptop ->
                                    val row = arrayOf(
                                        laptop.numeroSerie?.ifBlank { "Sin datos" } ?: "Sin datos",
                                        laptop.marca?.ifBlank { "Sin datos" } ?: "Sin datos",
                                        laptop.modelo?.ifBlank { "Sin datos" } ?: "Sin datos",
                                        laptop.estado?.ifBlank { "Sin datos" } ?: "Sin datos",
                                        laptop.observaciones?.ifBlank { "Sin datos" } ?: "Sin datos",
                                        laptop.fechaHora?.ifBlank { "Sin datos" } ?: "Sin datos"
                                    )
                                    csvWriter.writeNext(row)
                                }
                            }
                    }
                    
                    onComplete(true, "Archivo Excel guardado exitosamente")
                } ?: throw IOException("No se pudo abrir el archivo para escritura")

            } catch (e: Exception) {
                Log.e("ExcelExporter", "Error: ${e.message}")
                e.printStackTrace()
                onComplete(false, "Error al exportar: ${e.localizedMessage}. Revisa permisos o formato.")
            }
        }.start()
    }
}
