import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

public class GestionVacunasVentana {

    static void abrir(int idMascota, Runnable alGuardar) {

        ArrayList<String[]> vacunas = GestorMascotas.obtenerVacunasConId(idMascota);

        if (vacunas.isEmpty()) {
            Alert aviso = new Alert(Alert.AlertType.INFORMATION, "Esta mascota no tiene vacunas registradas.");
            aviso.showAndWait();
            return;
        }

        Stage ventana = new Stage();
        ventana.setTitle("Gestionar vacunas");
        ventana.initModality(Modality.APPLICATION_MODAL);

        ListView<String> lista = new ListView<>();
        for (String[] v : vacunas) {
            // v = [id, nombre, fechaAplicacion, fechaProximaDosis, veterinario, lote]
            lista.getItems().add(v[1] + " (" + v[2] + ") - Próxima: " + v[3]);
        }

        TextField campoNombre = new TextField();
        DatePicker campoFechaAplicacion = new DatePicker();
        DatePicker campoFechaProxima = new DatePicker();
        TextField campoVeterinario = new TextField();
        TextField campoLote = new TextField();

        Button botonGuardar = new Button("Guardar cambios");
        botonGuardar.setDisable(true);

        Button botonBorrar = new Button("Borrar vacuna");
        botonBorrar.setDisable(true);

        lista.getSelectionModel().selectedItemProperty().addListener((observable, valorAntiguo, valorNuevo) -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice < 0) return;

            String[] v = vacunas.get(indice);
            campoNombre.setText(v[1]);
            campoFechaAplicacion.setValue(LocalDate.parse(v[2]));
            campoFechaProxima.setValue(LocalDate.parse(v[3]));
            campoVeterinario.setText(v[4]);
            campoLote.setText(v[5]);

            botonGuardar.setDisable(false);
            botonBorrar.setDisable(false);
        });

        botonGuardar.setOnAction(evento -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice < 0) return;

            if (campoNombre.getText().isBlank() || campoFechaAplicacion.getValue() == null
                    || campoFechaProxima.getValue() == null) {
                mostrarAviso("Nombre, fecha de aplicación y próxima dosis son obligatorios.");
                return;
            }

            int idVacuna = Integer.parseInt(vacunas.get(indice)[0]);

            botonGuardar.setDisable(true);
            new Thread(() -> {
                try {
                    GestorMascotas.actualizarVacuna(idVacuna, campoNombre.getText(),
                            campoFechaAplicacion.getValue(), campoFechaProxima.getValue(),
                            campoVeterinario.getText(), campoLote.getText());
                    javafx.application.Platform.runLater(() -> {
                        alGuardar.run();
                        ventana.close();
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> mostrarAviso("Error al actualizar: " + e.getMessage()));
                    botonGuardar.setDisable(false);
                }
            }, "actualizar-vacuna-thread").start();
        });

        botonBorrar.setOnAction(evento -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice < 0) return;

            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                    "¿Seguro que quieres borrar esta vacuna?",
                    ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> respuesta = confirmacion.showAndWait();

            if (respuesta.isPresent() && respuesta.get() == ButtonType.YES) {
                int idVacuna = Integer.parseInt(vacunas.get(indice)[0]);
                botonBorrar.setDisable(true);
                new Thread(() -> {
                    try {
                        GestorMascotas.borrarVacuna(idVacuna);
                        javafx.application.Platform.runLater(() -> {
                            alGuardar.run();
                            ventana.close();
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> mostrarAviso("Error al borrar: " + e.getMessage()));
                        botonBorrar.setDisable(false);
                    }
                }, "borrar-vacuna-thread").start();
            }
        });

        GridPane camposEdicion = new GridPane();
        camposEdicion.setHgap(10);
        camposEdicion.setVgap(8);
        camposEdicion.add(new Label("Nombre:"), 0, 0);
        camposEdicion.add(campoNombre, 1, 0);
        camposEdicion.add(new Label("Fecha aplicación:"), 0, 1);
        camposEdicion.add(campoFechaAplicacion, 1, 1);
        camposEdicion.add(new Label("Próxima dosis:"), 0, 2);
        camposEdicion.add(campoFechaProxima, 1, 2);
        camposEdicion.add(new Label("Veterinario:"), 0, 3);
        camposEdicion.add(campoVeterinario, 1, 3);
        camposEdicion.add(new Label("Lote:"), 0, 4);
        camposEdicion.add(campoLote, 1, 4);

        HBox botones = new HBox(10, botonGuardar, botonBorrar);

        VBox contenido = new VBox(10,
                new Label("Selecciona la vacuna a modificar:"),
                lista,
                new Label("Edita los datos y guarda:"),
                camposEdicion,
                botones
        );
        contenido.setPadding(new Insets(15));

        Scene escena = new Scene(contenido, 420, 560);
        ventana.setScene(escena);
        ventana.showAndWait();
    }

    private static void mostrarAviso(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensaje);
        alerta.showAndWait();
    }
}
