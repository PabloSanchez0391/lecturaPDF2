package org.example;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProcesadorDocumento {

    private final String archivoPDF;
    private final String tessdataPath;
    private final String idiomaOCR;
    private final int dpi;

    public ProcesadorDocumento(String archivoPDF, String tessdataPath, String idiomaOCR, int dpi) {
        this.archivoPDF = archivoPDF;
        this.tessdataPath = tessdataPath;
        this.idiomaOCR = idiomaOCR;
        this.dpi = dpi;
    }

    public void procesar() {
        try (PDDocument document = PDDocument.load(new File(archivoPDF))) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            ITesseract tesseract = configurarTesseract();

            for (int page = 0; page < document.getNumberOfPages(); page++) {
                System.out.println("Procesando página " + (page + 1));

                BufferedImage image = pdfRenderer.renderImageWithDPI(page, dpi);
                String textoPagina = tesseract.doOCR(image);

                // Guardar OCR completo
                String nombreCompleto = generarNombreSalida(page + 1, false);
                guardarResultado(nombreCompleto, textoPagina);

                // Guardar versión filtrada
                String nombreFiltrado = generarNombreSalida(page + 1, true);
                FiltroProductos.filtrarProductos(new File(nombreCompleto), new File(nombreFiltrado));


                System.out.println("Texto de la página " + (page + 1) +
                        " guardado en: " + new File(nombreCompleto).getAbsolutePath());
            }

            // Al finalizar todas las páginas, guardar JSON global
            File jsonFinal = new File(generarNombreJSON());
            FiltroProductos.guardarJSON(jsonFinal);

        } catch (IOException | TesseractException e) {
            e.printStackTrace();
        }
    }

    private ITesseract configurarTesseract() {
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath(tessdataPath);
        tesseract.setLanguage(idiomaOCR);
        return tesseract;
    }

    private String generarNombreSalida(int numeroPagina, boolean filtrado) {
        String baseName = archivoPDF.replaceFirst("[.][^.]+$", ""); // quita extensión
        String fechaHora = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm"));
        return baseName + "_pag" + numeroPagina + "_" + fechaHora +
                (filtrado ? "_filtrado.txt" : ".txt");
    }

    private String generarNombreJSON() {
        String baseName = archivoPDF.replaceFirst("[.][^.]+$", ""); // quita extensión
        String fechaHora = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm"));
        return baseName + "_productos_" + fechaHora + ".json";
    }

    private void guardarResultado(String nombreArchivo, String contenido) throws IOException {
        Path outputPath = Path.of(nombreArchivo);
        Files.write(outputPath, contenido.getBytes());
    }
}
