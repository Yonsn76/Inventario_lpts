package com.san_pedrito.myapplication.db_kt

/**
 * Clase de datos que representa una entidad de laptop en el sistema de inventario.
 * @property id Identificador único para la laptop
 * @property marca Marca de la laptop
 * @property modelo Modelo de la laptop
 * @property numeroSerie Número de serie de la laptop
 * @property estado Estado/condición actual de la laptop
 * @property observaciones Observaciones adicionales o notas sobre la laptop
 * @property rutaImagen Ruta a la imagen almacenada de la laptop
 * @property fechaHora Fecha y hora cuando la laptop fue registrada en el sistema
 */
data class Laptop(
    val id: Long,
    val marca: String,
    val modelo: String,
    val numeroSerie: String,
    val estado: String,
    val observaciones: String,
    val rutaImagen: String?,
    val fechaHora: String
)