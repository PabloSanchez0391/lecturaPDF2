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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {

    // Configuración (podrían venir de variables de entorno)
    private static final String PDF_FILE = "Docum-20250916144346.pdf";
    private static final String TESSDATA_PATH = "tessdata";
    private static final String OCR_LANGUAGE = "spa";
    private static final int OCR_DPI = 600;

    public static void main(String[] args) {
        try {
            PDDocument document = cargarDocumento(PDF_FILE);
            String textoExtraido = procesarDocumento(document);
            document.close();

            String nombreSalida = generarNombreSalida(PDF_FILE);
            guardarResultado(nombreSalida, textoExtraido);

            System.out.println("Texto extraído guardado en: " + new File(nombreSalida).getAbsolutePath());

        } catch (IOException | TesseractException e) {
            e.printStackTrace();
        }
    }

    private static PDDocument cargarDocumento(String nombreArchivo) throws IOException {
        File pdfFile = new File(nombreArchivo);
        return PDDocument.load(pdfFile);
    }

    private static String procesarDocumento(PDDocument document) throws IOException, TesseractException {
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        ITesseract tesseract = configurarTesseract();

        StringBuilder textoExtraido = new StringBuilder();

        for (int page = 0; page < document.getNumberOfPages(); page++) {
            System.out.println("Procesando página " + (page + 1));
            BufferedImage image = pdfRenderer.renderImageWithDPI(page, OCR_DPI);
            String result = tesseract.doOCR(image);
            textoExtraido.append("\n--- Página ").append(page + 1).append(" ---\n");
            textoExtraido.append(result);
        }

        return textoExtraido.toString();
    }

    private static ITesseract configurarTesseract() {
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath(TESSDATA_PATH);
        tesseract.setLanguage(OCR_LANGUAGE);
        return tesseract;
    }

    private static String generarNombreSalida(String archivoEntrada) {
        String baseName = archivoEntrada.replaceFirst("[.][^.]+$", ""); // quita extensión
        String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm"));
        return baseName + "_" + fechaHora + ".txt";
    }

    private static void guardarResultado(String nombreArchivo, String contenido) throws IOException {
        File output = new File(nombreArchivo);
        Files.write(output.toPath(), contenido.getBytes());
    }
}
