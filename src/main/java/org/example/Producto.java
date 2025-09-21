package org.example;

public class Producto {
    private final String codigo;
    private final String descripcion;
    private final String fechaCaducidad;

    public Producto(String codigo, String descripcion, String fechaCaducidad) {
        this.codigo = codigo != null ? codigo.trim() : "";
        this.descripcion = descripcion != null ? descripcion.trim() : "";
        this.fechaCaducidad = fechaCaducidad != null ? fechaCaducidad.trim() : "";
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getFechaCaducidad() {
        return fechaCaducidad;
    }

    @Override
    public String toString() {
        return codigo + " | " + descripcion + " Fecha Caducidad " + fechaCaducidad;
    }
}
