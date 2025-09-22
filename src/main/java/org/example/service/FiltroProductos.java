package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.example.dao.ProductoDAO;
import org.example.model.Producto;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FiltroProductos {

    private static final Pattern FECHA_PATTERN =
            Pattern.compile("Fecha\\s*Caducidad\\s*(\\d{2}/\\d{2}/\\d{4})", Pattern.CASE_INSENSITIVE);
    private static final Pattern CODIGO_PATTERN = Pattern.compile("(\\d{3,6})");

    // Lista global de todos los productos (todas las páginas)
    private static final List<Producto> TODOS_LOS_PRODUCTOS = new ArrayList<>();

    public static void filtrarProductos(File entrada, File salida) throws IOException {
        List<String> lines = Files.readAllLines(entrada.toPath(), StandardCharsets.UTF_8);

        int startIndex = 0;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).toUpperCase().contains("ARTICULO")) {
                startIndex = i + 1;
                break;
            }
        }

        List<Producto> productos = new ArrayList<>();
        ProductoDAO productoDAO = new ProductoDAO(); // DAO para la BD

        String pendingCodigo = null;
        String pendingDescripcion = null;

        for (int i = startIndex; i < lines.size(); i++) {
            String linea = lines.get(i).trim();
            if (linea.isEmpty()) continue;

            if (linea.toUpperCase().contains("RECIBÍ DE TOTAL CONFORMIDAD") ||
                    linea.toUpperCase().contains("COMPRADOR RENUNCIA")) {
                break;
            }

            Matcher fechaM = FECHA_PATTERN.matcher(linea);
            if (fechaM.find()) {
                String fecha = fechaM.group(1);
                if (pendingCodigo != null) {
                    Producto p = new Producto(pendingCodigo, pendingDescripcion, fecha);
                    productos.add(p);
                    TODOS_LOS_PRODUCTOS.add(p);

                    // Guardar en la BD
                    LocalDate fechaSql = parseFecha(fecha);
                    productoDAO.insertarProducto(p.getCodigo(), p.getDescripcion(), fechaSql);

                    pendingCodigo = null;
                    pendingDescripcion = null;
                }
                continue;
            }

            if (linea.toLowerCase().contains("lote") && !FECHA_PATTERN.matcher(linea).find()) {
                continue;
            }

            Matcher codigoM = CODIGO_PATTERN.matcher(linea);
            if (codigoM.find()) {
                String codigo = codigoM.group(1);

                if (linea.toLowerCase().startsWith("lote") || linea.toLowerCase().contains("fecha caducidad")) {
                    continue;
                }

                String afterCode = linea.substring(codigoM.end()).trim();
                String descripcion;

                if (afterCode.contains("|")) {
                    String[] parts = afterCode.split("\\|");
                    String candidate = null;
                    for (String p : parts) {
                        p = p.trim();
                        if (p.isEmpty()) continue;
                        if (p.matches("^\\d+$")) continue;
                        candidate = p;
                        break;
                    }
                    if (candidate == null) candidate = afterCode;
                    descripcion = cleanDescripcion(candidate);
                } else {
                    String candidate = afterCode;
                    int cutIndex = indexOfAnyIgnoreCase(candidate, new String[]{" lote", " fecha", " principal", " etp"});
                    if (cutIndex != -1) candidate = candidate.substring(0, cutIndex);
                    candidate = candidate.replaceAll("\\b\\d{1,3},\\d{2}\\b.*$", "").trim();
                    descripcion = cleanDescripcion(candidate);
                }

                pendingCodigo = codigo;
                pendingDescripcion = descripcion;
            }
        }

        if (pendingCodigo != null) {
            productos.add(new Producto(pendingCodigo, pendingDescripcion, ""));
        }

        // Añadir a la lista global
        TODOS_LOS_PRODUCTOS.addAll(productos);

        // Guardar txt filtrado por página
        List<String> salidaLines = productos.stream()
                .map(p -> p.getCodigo() + " | " + p.getDescripcion() +
                        (p.getFechaCaducidad().isEmpty() ? "" : " Fecha Caducidad " + p.getFechaCaducidad()))
                .collect(Collectors.toList());

        Files.write(salida.toPath(), salidaLines, StandardCharsets.UTF_8);

        System.out.println("Productos detectados en " + entrada.getName() + ": " + productos.size());
    }

    private static String cleanDescripcion(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        s = s.replaceFirst("^[^\\p{L}\\p{N}]+", "");
        s = s.replaceAll("(?i)\\bETP\\b", "");
        s = s.replaceAll("(?i)\\bPRINCIPAL\\b", "");
        s = s.replaceAll("\\b\\d{1,3},\\d{2}\\b.*$", "");
        s = s.replaceAll("\\s{2,}", " ").trim();
        s = s.replaceAll("[|\\p{Punct}]+$", "").trim();
        return s;
    }

    private static int indexOfAnyIgnoreCase(String text, String[] tokens) {
        String lower = text.toLowerCase();
        int best = -1;
        for (String t : tokens) {
            int pos = lower.indexOf(t.toLowerCase());
            if (pos != -1 && (best == -1 || pos < best)) best = pos;
        }
        return best;
    }

    /**
     * Guarda todos los productos recogidos hasta ahora en un único JSON.
     */
    public static void guardarJSON(File salidaJson) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        writer.writeValue(salidaJson, TODOS_LOS_PRODUCTOS);
        System.out.println("JSON global guardado en: " + salidaJson.getAbsolutePath());
    }

    static LocalDate parseFecha(String fecha) {
        if (fecha == null || fecha.isEmpty()) return null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(fecha, formatter);
        } catch (DateTimeParseException e) {
            System.err.println("No se pudo parsear la fecha: " + fecha);
            return null;
        }
    }
}
