package com.san_pedrito.myapplication

import android.content.Context
import android.net.Uri
import android.util.Log
import com.san_pedrito.myapplication.db_kt.Laptop
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Font
import java.io.IOException
import java.io.OutputStream

class ExcelExporter(private val context: Context) {

    fun exportToExcel(laptops: List<Laptop>, uri: Uri, onComplete: (Boolean, String) -> Unit) {
        Thread {
            var workbook: HSSFWorkbook? = null
            var outputStream: OutputStream? = null
            
            try {
                if (laptops.isEmpty()) {
                    Log.e("ExcelExporter", "Lista de laptops vacía")
                    onComplete(false, "No hay datos para exportar")
                    return@Thread
                }

                Log.d("ExcelExporter", "Iniciando exportación con ${laptops.size} registros")
                // Verificar los primeros registros
                laptops.take(3).forEachIndexed { index, laptop ->
                    Log.d("ExcelExporter", "Verificando laptop $index: " +
                            "Serie=${laptop.numeroSerie}, " +
                            "Marca=${laptop.marca}, " +
                            "Modelo=${laptop.modelo}, " +
                            "Estado=${laptop.estado}")
                }

                workbook = HSSFWorkbook()
                val sheet = workbook.createSheet("Inventario Laptops")
                Log.d("ExcelExporter", "Hoja de cálculo creada")

                // Crear estilo para los encabezados
                val headerStyle = workbook.createCellStyle()
                val font = workbook.createFont()
                font.bold = true
                headerStyle.setFont(font)

                // Crear fila de encabezados
                val headerRow = sheet.createRow(0)
                val headers = arrayOf("Número de Serie", "Marca", "Modelo", "Estado", "Observaciones", "Fecha/Hora")
                headers.forEachIndexed { index, header ->
                    val cell = headerRow.createCell(index)
                    cell.setCellValue(header)
                    cell.setCellStyle(headerStyle)
                }
                Log.d("ExcelExporter", "Encabezados creados")

                // Agregar datos
                var rowsCreated = 0
                laptops.forEachIndexed { index, laptop ->
                    val row = sheet.createRow(index + 1)
                    createCell(row, 0, laptop.numeroSerie)
                    createCell(row, 1, laptop.marca)
                    createCell(row, 2, laptop.modelo)
                    createCell(row, 3, laptop.estado)
                    createCell(row, 4, laptop.observaciones)
                    createCell(row, 5, laptop.fechaHora)
                    rowsCreated++
                }
                Log.d("ExcelExporter", "Filas de datos creadas: $rowsCreated")

                // Ajustar ancho de columnas
                headers.indices.forEach { sheet.autoSizeColumn(it) }
                Log.d("ExcelExporter", "Columnas ajustadas")

                // Escribir el archivo usando el URI proporcionado
                outputStream = context.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    Log.d("ExcelExporter", "Stream abierto, escribiendo workbook...")
                    workbook.write(outputStream)
                    outputStream.flush()
                    Log.d("ExcelExporter", "Archivo Excel escrito exitosamente")
                    onComplete(true, "Archivo Excel guardado exitosamente con ${laptops.size} registros")
                } else {
                    throw IOException("No se pudo abrir el archivo para escritura")
                }

            } catch (e: Exception) {
                Log.e("ExcelExporter", "Error durante la exportación: ${e.message}")
                e.printStackTrace()
                onComplete(false, "Error al exportar: ${e.message}")
            } finally {
                try {
                    Log.d("ExcelExporter", "Cerrando recursos...")
                    outputStream?.flush()
                    outputStream?.close()
                    workbook?.close()
                    Log.d("ExcelExporter", "Recursos cerrados correctamente")
                } catch (e: IOException) {
                    Log.e("ExcelExporter", "Error al cerrar recursos: ${e.message}")
                }
            }
        }.start()
    }

    private fun createCell(row: org.apache.poi.ss.usermodel.Row, index: Int, value: String?) {
        try {
            val cell = row.createCell(index)
            cell.setCellValue(value ?: "")
        } catch (e: Exception) {
            Log.e("ExcelExporter", "Error al crear celda $index: ${e.message}")
        }
    }
} 