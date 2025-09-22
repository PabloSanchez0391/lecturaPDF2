package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dao.ProductoDAO;
import org.example.model.Producto;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.example.service.FiltroProductos.parseFecha;

public class GestorProductos {
    private static final List<Producto> TODOS = new ArrayList<>();
    private static final ProductoDAO productoDAO = new ProductoDAO();

    public static void agregarProductos(List<Producto> productos) {
        TODOS.addAll(productos);
        for (Producto p : productos) {
            productoDAO.insertarProducto(p.getCodigo(), p.getDescripcion(), parseFecha(p.getFechaCaducidad()));
        }
    }

    public static void exportarJSON(String nombreArchivo) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(nombreArchivo), TODOS);
        System.out.println("JSON generado en: " + nombreArchivo);
    }
}

