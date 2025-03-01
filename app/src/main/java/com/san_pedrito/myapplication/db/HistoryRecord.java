package com.san_pedrito.myapplication.db;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

/**
 * Entity representing a history record of laptop movements or status changes.
 */
@Entity(
    tableName = "historial",
    foreignKeys = {
        @ForeignKey(
            entity = Laptop.class,
            parentColumns = "id",
            childColumns = "laptop_id",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index("laptop_id")
    }
)
public class HistoryRecord {
    
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    private int id;
    
    @ColumnInfo(name = "laptop_id")
    private int laptopId;
    
    @ColumnInfo(name = "fecha")
    private Date fecha;
    
    @ColumnInfo(name = "accion")
    private String accion;
    
    @ColumnInfo(name = "ubicacion_anterior")
    private String ubicacionAnterior;
    
    @ColumnInfo(name = "ubicacion_nueva")
    private String ubicacionNueva;
    
    @ColumnInfo(name = "estado_anterior")
    private String estadoAnterior;
    
    @ColumnInfo(name = "estado_nuevo")
    private String estadoNuevo;
    
    @ColumnInfo(name = "usuario")
    private String usuario;
    
    @ColumnInfo(name = "notas")
    private String notas;
    
    // Constructor
    public HistoryRecord(int laptopId, Date fecha, String accion, String ubicacionAnterior, 
                        String ubicacionNueva, String estadoAnterior, String estadoNuevo, 
                        String usuario, String notas) {
        this.laptopId = laptopId;
        this.fecha = fecha;
        this.accion = accion;
        this.ubicacionAnterior = ubicacionAnterior;
        this.ubicacionNueva = ubicacionNueva;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.usuario = usuario;
        this.notas = notas;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getLaptopId() {
        return laptopId;
    }
    
    public void setLaptopId(int laptopId) {
        this.laptopId = laptopId;
    }
    
    public Date getFecha() {
        return fecha;
    }
    
    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
    
    public String getAccion() {
        return accion;
    }
    
    public void setAccion(String accion) {
        this.accion = accion;
    }
    
    public String getUbicacionAnterior() {
        return ubicacionAnterior;
    }
    
    public void setUbicacionAnterior(String ubicacionAnterior) {
        this.ubicacionAnterior = ubicacionAnterior;
    }
    
    public String getUbicacionNueva() {
        return ubicacionNueva;
    }
    
    public void setUbicacionNueva(String ubicacionNueva) {
        this.ubicacionNueva = ubicacionNueva;
    }
    
    public String getEstadoAnterior() {
        return estadoAnterior;
    }
    
    public void setEstadoAnterior(String estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }
    
    public String getEstadoNuevo() {
        return estadoNuevo;
    }
    
    public void setEstadoNuevo(String estadoNuevo) {
        this.estadoNuevo = estadoNuevo;
    }
    
    public String getUsuario() {
        return usuario;
    }
    
    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
    
    public String getNotas() {
        return notas;
    }
    
    public void setNotas(String notas) {
        this.notas = notas;
    }
}