import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.util.ArrayList;

public class ExportadorMascota {

    // Método de ayuda: escribe UNA línea de texto en la posición (x, y) indicada.
    // Cada línea es su propio bloque beginText/endText, así (x, y) es siempre
    // una posición absoluta en la página, sin arrastrar la posición anterior.
    static void escribirLinea(PDPageContentStream contenido, String texto,
                              float x, float y, float tamaño) throws IOException {
        contenido.beginText();
        contenido.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), tamaño);
        contenido.newLineAtOffset(x, y);
        contenido.showText(texto);
        contenido.endText();
    }

    // soloTratamientosActuales: si es true, no se incluyen en el PDF los
    // tratamientos cuya fechaFin ya haya pasado (ver GestorMascotas.obtenerLineasTratamientos)
    static void exportarFicha(String microchip, boolean soloTratamientosActuales) {

        String[] datos = GestorMascotas.obtenerDatosBasicos(microchip);
        if (datos == null) {
            System.out.println("No se encontró ninguna mascota con ese microchip.");
            return;
        }

        float margen = 50;
        float y = 750; // empezamos cerca de la parte de arriba de la página

        try (PDDocument documento = new PDDocument()) {
            PDPage pagina = new PDPage();
            documento.addPage(pagina);

            try (PDPageContentStream contenido = new PDPageContentStream(documento, pagina)) {

                // --- Cabecera con los datos básicos ---
                escribirLinea(contenido, "Ficha de " + datos[0], margen, y, 16);
                y -= 25;
                escribirLinea(contenido, "Especie: " + datos[1] + "   |   Raza: " + datos[2], margen, y, 11);
                y -= 15;
                escribirLinea(contenido, "Fecha de nacimiento: " + datos[3], margen, y, 11);
                y -= 15;
                escribirLinea(contenido, "Sexo: " + datos[4] + "   |   Color: " + datos[5], margen, y, 11);
                y -= 15;
                escribirLinea(contenido, "Microchip: " + datos[6], margen, y, 11);
                y -= 30;

                // --- Vacunas: siempre todas ---
                y = escribirSeccion(contenido, "Vacunas", GestorMascotas.obtenerLineasVacunas(microchip), margen, y);

                // --- Revisiones: siempre todas ---
                y = escribirSeccion(contenido, "Revisiones", GestorMascotas.obtenerLineasRevisiones(microchip), margen, y);

                // --- Tratamientos: filtrados según el parámetro ---
                String tituloTratamientos = soloTratamientosActuales ? "Tratamientos actuales" : "Tratamientos (todos)";
                ArrayList<String> tratamientos = GestorMascotas.obtenerLineasTratamientos(microchip, soloTratamientosActuales);
                y = escribirSeccion(contenido, tituloTratamientos, tratamientos, margen, y);

                // --- Historial de peso: siempre todo ---
                escribirSeccion(contenido, "Historial de peso", GestorMascotas.obtenerLineasPesos(microchip), margen, y);
            }

            String nombreArchivo = datos[0] + "_ficha.pdf";
            documento.save(nombreArchivo);
            System.out.println("PDF generado correctamente: " + nombreArchivo);

        } catch (IOException e) {
            System.out.println("Error al generar el PDF: " + e.getMessage());
        }
    }

    // Escribe un título de sección y su lista de líneas, y devuelve la nueva
    // posición "y" (más abajo), para que el siguiente bloque sepa dónde seguir.
    // Nota: por simplicidad, esta primera versión asume que todo cabe en una
    // sola página. Si una mascota tuviera muchísimos registros, el texto
    // podría salirse de la página — eso se resolvería más adelante añadiendo
    // una segunda página cuando "y" baje demasiado.
    static float escribirSeccion(PDPageContentStream contenido, String titulo,
                                 ArrayList<String> lineas, float margen, float y) throws IOException {

        escribirLinea(contenido, titulo + ":", margen, y, 13);
        y -= 18;

        if (lineas.isEmpty()) {
            escribirLinea(contenido, "- (sin registros)", margen, y, 10);
            y -= 15;
        } else {
            for (String linea : lineas) {
                escribirLinea(contenido, "- " + linea, margen, y, 10);
                y -= 15;
            }
        }

        return y - 15; // margen extra antes de la siguiente sección
    }
}