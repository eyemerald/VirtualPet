import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FormularioMascota {

    // Para AÑADIR una mascota nueva (como ya tenías)
    static void abrir() {
        construirVentana(null, null);
    }

    // NUEVO: para EDITAR una mascota que ya existe. Recibe el id (para
    // saber a quién actualizar) y sus datos actuales (para rellenar
    // los campos, en vez de dejarlos vacíos).
    static void abrirEdicion(int idExistente, String[] datosActuales) {
        construirVentana(idExistente, datosActuales);
    }

    // Método privado compartido: construye la MISMA ventana tanto para
    // crear como para editar. idExistente == null significa "es nueva".
    // datosActuales viene en el mismo orden que obtenerDatosBasicos():
    // [nombre, especie, raza, fechaNacimiento, sexo, color, microchip]
    private static void construirVentana(Integer idExistente, String[] datosActuales) {

        boolean esEdicion = (idExistente != null);

        Stage ventana = new Stage();
        ventana.setTitle(esEdicion ? "Editar mascota" : "Nueva mascota");
        ventana.initModality(Modality.APPLICATION_MODAL);

        TextField campoNombre = new TextField();
        TextField campoEspecie = new TextField();
        TextField campoRaza = new TextField();
        DatePicker campoFecha = new DatePicker();

        ComboBox<String> campoSexo = new ComboBox<>();
        campoSexo.getItems().addAll("Macho", "Hembra");

        TextField campoColor = new TextField();
        TextField campoMicrochip = new TextField();
        campoMicrochip.setPromptText("Opcional");

        // Si estamos editando, precargamos los campos con los datos actuales
        if (esEdicion) {
            campoNombre.setText(datosActuales[0]);
            campoEspecie.setText(datosActuales[1]);
            campoRaza.setText(datosActuales[2]);
            campoFecha.setValue(LocalDate.parse(datosActuales[3])); // viene en formato ISO (yyyy-MM-dd) de la base de datos
            campoSexo.setValue(datosActuales[4]);
            campoColor.setText(datosActuales[5]);
            // Si no tenía microchip, obtenerDatosBasicos devuelve "(sin microchip)" — no lo precargamos tal cual
            campoMicrochip.setText(datosActuales[6].equals("(sin microchip)") ? "" : datosActuales[6]);
        }

        Button botonGuardar = new Button(esEdicion ? "Guardar cambios" : "Guardar");

        GridPane formulario = new GridPane();
        formulario.setPadding(new Insets(15));
        formulario.setHgap(10);
        formulario.setVgap(10);

        formulario.add(new Label("Nombre:"), 0, 0);
        formulario.add(campoNombre, 1, 0);
        formulario.add(new Label("Especie:"), 0, 1);
        formulario.add(campoEspecie, 1, 1);
        formulario.add(new Label("Raza:"), 0, 2);
        formulario.add(campoRaza, 1, 2);
        formulario.add(new Label("Fecha nacimiento:"), 0, 3);
        formulario.add(campoFecha, 1, 3);
        formulario.add(new Label("Sexo:"), 0, 4);
        formulario.add(campoSexo, 1, 4);
        formulario.add(new Label("Color:"), 0, 5);
        formulario.add(campoColor, 1, 5);
        formulario.add(new Label("Microchip:"), 0, 6);
        formulario.add(campoMicrochip, 1, 6);
        formulario.add(botonGuardar, 1, 7);

        botonGuardar.setOnAction(evento -> {

            if (campoNombre.getText().isBlank() || campoEspecie.getText().isBlank()) {
                mostrarError("El nombre y la especie son obligatorios.");
                return;
            }
            if (campoFecha.getValue() == null) {
                mostrarError("Elige una fecha de nacimiento.");
                return;
            }
            // NUEVO: una mascota no puede haber nacido "en el futuro"
            if (campoFecha.getValue().isAfter(LocalDate.now())) {
                mostrarError("La fecha de nacimiento no puede ser posterior a hoy.");
                return;
            }

            Mascota datos = new Mascota(
                    campoNombre.getText(),
                    campoEspecie.getText(),
                    campoRaza.getText(),
                    campoFecha.getValue(),
                    campoSexo.getValue() == null ? "" : campoSexo.getValue(),
                    campoColor.getText(),
                    campoMicrochip.getText()
            );

            // Ejecutar guardado/actualización en background para no bloquear la UI
            botonGuardar.setDisable(true);
            new Thread(() -> {
                try {
                    if (esEdicion) {
                        GestorMascotas.actualizarMascota(idExistente, datos);
                    } else {
                        GestorMascotas.guardarMascota(datos);
                    }
                    javafx.application.Platform.runLater(() -> {
                        VirtuaPetApp.cargarListaMascotas();
                        ventana.close();
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> mostrarError("Error al guardar: " + e.getMessage()));
                    botonGuardar.setDisable(false);
                }
            }, "guardar-mascota-thread").start();
        });

        Scene escena = new Scene(formulario, 350, 320);
        ventana.setScene(escena);
        ventana.showAndWait();
    }

    static void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje);
        alerta.showAndWait();
    }
}
