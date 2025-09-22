package org.example.app;

import org.example.dao.ProductoDAO;
import org.example.service.ProcesadorDocumento;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        String archivoPDF = "Docum-20250916144346.pdf";

        ProcesadorDocumento procesador = new ProcesadorDocumento(
                archivoPDF,
                "tessdata",   // Ruta de tessdata
                "spa",        // Idioma OCR
                600           // DPI para el renderizado
        );

        procesador.procesar();


//        ProductoDAO dao = new ProductoDAO();

//        dao.insertarProducto("83441", "ACEITU ALÍNAD CHUPADEDS LT(4,2K/2,5K", LocalDate.of(2029, 11, 5));
//        dao.insertarProducto("83198", "", LocalDate.of(2029, 11, 14));
//        dao.insertarProducto("83072", "ACEITU GORDAL PEPINIL 1/2G(2,35K/1K", LocalDate.of(2030, 8, 13));
//
//        System.out.println("Productos guardados:");
//        dao.listarProductos();
//
//        dao.borrarProducto("83441");
//        dao.borrarProducto("83198");
//        dao.borrarProducto("83072");
//
//        dao.listarProductos();
//
//        System.out.println("Después de borrar:");
//        dao.listarProductos();
    }
}
