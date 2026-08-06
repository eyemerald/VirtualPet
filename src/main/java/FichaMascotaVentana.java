import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Optional;

public class FichaMascotaVentana {

    static void abrir(int idMascota) {

        String[] datos = GestorMascotas.obtenerDatosBasicos(idMascota);
        if (datos == null) {
            mostrarAviso(AlertType.ERROR, "No se encontró esa mascota.");
            return;
        }

        Stage ventana = new Stage();
        ventana.setTitle("Ficha de " + datos[0]);
        ventana.initModality(Modality.APPLICATION_MODAL);

        Label titulo = new Label(datos[0]);
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label infoBasica = new Label(
                "Especie: " + datos[1] + "   |   Raza: " + datos[2] + "\n"
                        + "Nacimiento: " + datos[3] + " (" + GestorMascotas.calcularEdad(datos[3]) + ")   |   Sexo: " + datos[4] + "\n"
                        + "Color: " + datos[5] + "   |   Microchip: " + datos[6]
        );

        // NUEVO: aviso visible si hay vacunas vencidas o próximas a caducar,
        // sin tener que leer todo el historial para darse cuenta
        Label avisoVacunas = new Label(construirAvisoVacunas(idMascota));
        avisoVacunas.setStyle("-fx-text-fill: #b00020; -fx-font-weight: bold;");
        avisoVacunas.setWrapText(true); // si el texto es largo, salta de línea en vez de cortarse

        TextArea areaHistorial = new TextArea();
        areaHistorial.setEditable(false);
        areaHistorial.setText(construirTextoHistorial(idMascota));

        // NUEVO: este Runnable se pasa a todos los formularios de "añadir".
        // Cuando terminan de guardar, llaman a alGuardar.run(), que aquí
        // simplemente vuelve a leer la base de datos y actualiza el texto
        // — así la ficha se refresca sola sin tener que cerrarla y abrirla.
        Runnable refrescarHistorial = () -> {
            areaHistorial.setText(construirTextoHistorial(idMascota));
            avisoVacunas.setText(construirAvisoVacunas(idMascota)); // también se actualiza el aviso de arriba
        };

        // --- Botones para AÑADIR datos nuevos ---
        Button botonVacuna = new Button("+ Vacuna");
        botonVacuna.setOnAction(e -> FormularioRegistro.abrirVacuna(idMascota, refrescarHistorial));

        Button botonRevision = new Button("+ Revisión");
        botonRevision.setOnAction(e -> FormularioRegistro.abrirRevision(idMascota, refrescarHistorial));

        Button botonTratamiento = new Button("+ Tratamiento");
        botonTratamiento.setOnAction(e -> FormularioRegistro.abrirTratamiento(idMascota, refrescarHistorial));

        Button botonPeso = new Button("+ Peso");
        botonPeso.setOnAction(e -> FormularioRegistro.abrirPeso(idMascota, refrescarHistorial));

        // NUEVO: gestionar (alargar/acortar/terminar) tratamientos ya existentes
        Button botonGestionarTratamientos = new Button("Gestionar tratamientos");
        botonGestionarTratamientos.setOnAction(e ->
                GestionTratamientosVentana.abrir(idMascota, refrescarHistorial));

        Button botonGestionarVacunas = new Button("Gestionar vacunas");
        botonGestionarVacunas.setOnAction(e ->
                GestionVacunasVentana.abrir(idMascota, refrescarHistorial));

        Button botonGestionarRevisiones = new Button("Gestionar revisiones");
        botonGestionarRevisiones.setOnAction(e ->
                GestionRevisionesVentana.abrir(idMascota, refrescarHistorial));

        Button botonGestionarPesos = new Button("Gestionar pesos");
        botonGestionarPesos.setOnAction(e ->
                GestionPesosVentana.abrir(idMascota, refrescarHistorial));

        // NUEVO: modificar los datos básicos de la mascota
        Button botonModificar = new Button("Modificar datos");
        botonModificar.setOnAction(e -> {
            FormularioMascota.abrirEdicion(idMascota, datos);
            // Tras editar, cerramos esta ficha y reabrimos una nueva con
            // los datos actualizados — más simple que actualizar cada
            // Label del título/cabecera uno a uno
            ventana.close();
            FichaMascotaVentana.abrir(idMascota);
        });

        Button botonExportar = new Button("Exportar a PDF");
        botonExportar.setOnAction(evento -> {
            botonExportar.setDisable(true);
            new Thread(() -> {
                try {
                    ExportadorMascota.exportarFicha(idMascota, true);
                    javafx.application.Platform.runLater(() -> {
                        mostrarAviso(AlertType.INFORMATION, "PDF generado en la carpeta del proyecto.");
                        botonExportar.setDisable(false);
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> mostrarAviso(AlertType.ERROR, "Error exportando PDF: " + e.getMessage()));
                    botonExportar.setDisable(false);
                }
            }, "exportar-pdf-thread").start();
        });

        Button botonBorrar = new Button("Borrar mascota");
        botonBorrar.setOnAction(evento -> {
            Alert confirmacion = new Alert(AlertType.CONFIRMATION,
                    "¿Seguro que quieres borrar a " + datos[0] + " y todos sus datos?",
                    ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> respuesta = confirmacion.showAndWait();

            if (respuesta.isPresent() && respuesta.get() == ButtonType.YES) {
                GestorMascotas.borrarMascota(idMascota);
                VirtuaPetApp.cargarListaMascotas();
                ventana.close();
            }
        });

        // FlowPane: como HBox, pero si no caben todos los botones en una
        // fila, sigue automáticamente en la siguiente — útil ahora que
        // tenemos bastantes más botones que al principio
        FlowPane filaBotonesAñadir = new FlowPane(8, 8,
                botonVacuna, botonRevision, botonTratamiento, botonPeso, 
                botonGestionarTratamientos, botonGestionarVacunas, botonGestionarRevisiones, botonGestionarPesos);

        FlowPane filaBotonesMascota = new FlowPane(8, 8, botonModificar, botonExportar, botonBorrar);

        VBox contenido = new VBox(12, titulo, infoBasica, avisoVacunas, areaHistorial, filaBotonesAñadir, filaBotonesMascota);
        contenido.setPadding(new Insets(15));

        Scene escena = new Scene(contenido, 480, 580);
        ventana.setScene(escena);
        ventana.showAndWait();
    }

    // NUEVO: recorre las vacunas ya formateadas (que ahora incluyen el
    // símbolo ⚠) y construye una sola línea-resumen para arriba de la ficha.
    // Si no hay ninguna con aviso, no muestra nada.
    static String construirAvisoVacunas(int idMascota) {
        ArrayList<String> vacunas = GestorMascotas.obtenerLineasVacunas(idMascota);
        int contador = 0;
        for (String linea : vacunas) {
            if (linea.contains("⚠")) contador++;
        }

        if (contador == 0) return "";
        return "⚠ " + contador + " vacuna(s) vencida(s) o próxima(s) — revisa el detalle abajo";
    }

    static String construirTextoHistorial(int idMascota) {
        StringBuilder texto = new StringBuilder();

        texto.append("VACUNAS:\n");
        agregarLineas(texto, GestorMascotas.obtenerLineasVacunas(idMascota));

        texto.append("\nREVISIONES:\n");
        agregarLineas(texto, GestorMascotas.obtenerLineasRevisiones(idMascota));

        texto.append("\nTRATAMIENTOS:\n");
        agregarLineas(texto, GestorMascotas.obtenerLineasTratamientos(idMascota, false));

        texto.append("\nHISTORIAL DE PESO:\n");
        agregarLineas(texto, GestorMascotas.obtenerLineasPesos(idMascota));

        return texto.toString();
    }

    static void agregarLineas(StringBuilder texto, ArrayList<String> lineas) {
        if (lineas.isEmpty()) {
            texto.append("(sin registros)\n");
        } else {
            for (String linea : lineas) {
                texto.append("- ").append(linea).append("\n");
            }
        }
    }

    static void mostrarAviso(AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.showAndWait();
    }
}
