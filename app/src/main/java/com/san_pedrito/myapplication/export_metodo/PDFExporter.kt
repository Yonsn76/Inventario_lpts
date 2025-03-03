package com.san_pedrito.myapplication.export_metodo

import android.content.Context
import android.net.Uri
import android.util.Log
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.san_pedrito.myapplication.db_kt.Laptop
import java.io.IOException

class PDFExporter(private val context: Context) {

    fun exportToPDF(laptops: List<Laptop>, uri: Uri, onComplete: (Boolean, String) -> Unit) {
        Thread {
            try {
                if (laptops.isEmpty()) {
                    Log.e("PDFExporter", "Lista de laptops vacía")
                    onComplete(false, "No hay datos para exportar")
                    return@Thread
                }

                Log.d("PDFExporter", "Iniciando exportación con ${laptops.size} registros")

                // Abrir stream para escribir el PDF
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    // Crear documento PDF
                    val pdfWriter = PdfWriter(outputStream)
                    val pdf = PdfDocument(pdfWriter)
                    val document = Document(pdf)

                    // Agregar título
                    val title = Paragraph("Inventario de Laptops")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontSize(20f)
                        .setBold()
                    document.add(title)

                    // Crear tabla
                    val table = Table(UnitValue.createPercentArray(floatArrayOf(15f, 15f, 15f, 15f, 25f, 15f)))
                        .useAllAvailableWidth()

                    // Agregar encabezados
                    val headers = arrayOf("Número de Serie", "Marca", "Modelo", "Estado", "Observaciones", "Fecha/Hora")
                    headers.forEach { header ->
                        table.addHeaderCell(
                            Cell().add(Paragraph(header))
                                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setBold()
                        )
                    }

                    // Agregar datos
                    var rowsCreated = 0
                    laptops.forEach { laptop ->
                        table.addCell(Cell().add(Paragraph(laptop.numeroSerie)).setTextAlignment(TextAlignment.CENTER))
                        table.addCell(Cell().add(Paragraph(laptop.marca)).setTextAlignment(TextAlignment.CENTER))
                        table.addCell(Cell().add(Paragraph(laptop.modelo)).setTextAlignment(TextAlignment.CENTER))
                        table.addCell(Cell().add(Paragraph(laptop.estado)).setTextAlignment(TextAlignment.CENTER))
                        table.addCell(Cell().add(Paragraph(laptop.observaciones)).setTextAlignment(TextAlignment.LEFT))
                        table.addCell(Cell().add(Paragraph(laptop.fechaHora)).setTextAlignment(TextAlignment.CENTER))
                        rowsCreated++
                    }

                    Log.d("PDFExporter", "Filas de datos creadas: $rowsCreated")

                    // Agregar tabla al documento
                    document.add(table)

                    // Agregar pie de página con fecha de generación
                    val dateTime = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault())
                        .format(java.util.Date())
                    val footer = Paragraph("Documento generado el $dateTime")
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setFontSize(8f)
                        .setItalic()
                    document.add(footer)

                    // Cerrar documento
                    document.close()
                    Log.d("PDFExporter", "Documento PDF creado exitosamente")
                    onComplete(true, "Archivo PDF guardado exitosamente con ${laptops.size} registros")
                } ?: throw IOException("No se pudo abrir el archivo para escritura")

            } catch (e: Exception) {
                Log.e("PDFExporter", "Error durante la exportación: ${e.message}")
                e.printStackTrace()
                onComplete(false, "Error al exportar: ${e.message}")
            }
        }.start()
    }
} 