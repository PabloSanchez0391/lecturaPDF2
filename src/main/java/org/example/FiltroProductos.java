package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

public class FiltroProductos {

    public static void filtrarProductos(File entrada, File salida) throws IOException {
        List<String> lineas = Files.readAllLines(entrada.toPath());

        boolean dentroDeProductos = false;
        StringBuilder productos = new StringBuilder();

        for (String linea : lineas) {
            // Detectar inicio de la tabla
            if (linea.contains("ARTICULO")) {
                dentroDeProductos = true;
                productos.append(linea).append("\n");
                continue;
            }

            // Detectar fin (cuando llega el texto legal)
            if (dentroDeProductos && linea.contains("Recibí de total conformidad")) {
                break;
            }

            if (dentroDeProductos) {
                productos.append(linea).append("\n");
            }
        }

        // Guardar solo la parte de productos
        Files.write(salida.toPath(), productos.toString().getBytes());
        System.out.println("Productos filtrados guardados en: " + salida.getAbsolutePath());
    }

    public static void main(String[] args) throws IOException {
        File entrada = new File("resultado.txt");   // el OCR completo
        File salida = new File("productos.txt");    // solo productos
        filtrarProductos(entrada, salida);
    }
}
