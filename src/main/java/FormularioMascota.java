import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;

public class FormularioMascota {

    public static void abrir() {
        Navegador.navegarA("Nueva mascota", () -> crearVista(null, null));
    }

    public static void abrirEdicion(int idExistente, String[] datosActuales) {
        Navegador.navegarA("Editar mascota", () -> crearVista(idExistente, datosActuales));
    }

    public static Node crearVista(Integer idExistente, String[] datosActuales) {
        boolean esEdicion = (idExistente != null);

        TextField campoNombre = new TextField();
        TextField campoEspecie = new TextField();
        TextField campoRaza = new TextField();
        DatePicker campoFecha = new DatePicker();

        ComboBox<String> campoSexo = new ComboBox<>();
        campoSexo.getItems().addAll("Macho", "Hembra");

        TextField campoColor = new TextField();
        TextField campoMicrochip = new TextField();
        campoMicrochip.setPromptText("Opcional");

        if (esEdicion && datosActuales != null) {
            campoNombre.setText(datosActuales[0]);
            campoEspecie.setText(datosActuales[1]);
            campoRaza.setText(datosActuales[2]);
            campoFecha.setValue(LocalDate.parse(datosActuales[3]));
            campoSexo.setValue(datosActuales[4]);
            campoColor.setText(datosActuales[5]);
            campoMicrochip.setText(datosActuales[6].equals("(sin microchip)") ? "" : datosActuales[6]);
        }

        Button botonGuardar = new Button(esEdicion ? "Guardar cambios" : "Guardar");
        FontIcon iconoGuardar = new FontIcon(Feather.CHECK);
        iconoGuardar.setIconSize(16);
        botonGuardar.setGraphic(iconoGuardar);
        botonGuardar.getStyleClass().add("btn-primary");

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
                        if (esEdicion) {
                            Navegador.volverAtras();
                        } else {
                            Navegador.volverAlInicio();
                        }
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        String msg = e.getMessage();
                        if (msg != null && msg.contains("Ya existe una mascota")) {
                            mostrarError(msg);
                        } else {
                            mostrarError("Error al guardar: " + msg);
                        }
                        botonGuardar.setDisable(false);
                    });
                }
            }, "guardar-mascota-thread").start();
        });

        ScrollPane scroll = new ScrollPane(formulario);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return scroll;
    }

    static void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje);
        alerta.showAndWait();
    }
}
