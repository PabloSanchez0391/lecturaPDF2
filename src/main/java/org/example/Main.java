package org.example;

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
    }
}
