import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

public class GestionRevisionesVentana {

    static void abrir(int idMascota, Runnable alGuardar) {

        ArrayList<String[]> revisiones = GestorMascotas.obtenerRevisionesConId(idMascota);

        if (revisiones.isEmpty()) {
            Alert aviso = new Alert(Alert.AlertType.INFORMATION, "Esta mascota no tiene revisiones registradas.");
            aviso.showAndWait();
            return;
        }

        Stage ventana = new Stage();
        ventana.setTitle("Gestionar revisiones");
        ventana.initModality(Modality.APPLICATION_MODAL);

        ListView<String> lista = new ListView<>();
        for (String[] r : revisiones) {
            // r = [id, fechaRevision, motivoRevision, diagnostico, notas, veterinario]
            lista.getItems().add(r[1] + " - " + r[2]);
        }

        DatePicker campoFecha = new DatePicker();
        TextField campoMotivo = new TextField();
        TextArea campoDiagnostico = new TextArea();
        campoDiagnostico.setWrapText(true);
        campoDiagnostico.setPrefRowCount(3);
        TextArea campoNotas = new TextArea();
        campoNotas.setWrapText(true);
        campoNotas.setPrefRowCount(2);
        TextField campoVeterinario = new TextField();

        Button botonGuardar = new Button("Guardar cambios");
        botonGuardar.setDisable(true);

        Button botonBorrar = new Button("Borrar revisión");
        botonBorrar.setDisable(true);

        lista.getSelectionModel().selectedItemProperty().addListener((observable, valorAntiguo, valorNuevo) -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice < 0) return;

            String[] r = revisiones.get(indice);
            campoFecha.setValue(LocalDate.parse(r[1]));
            campoMotivo.setText(r[2]);
            campoDiagnostico.setText(r[3]);
            campoNotas.setText(r[4]);
            campoVeterinario.setText(r[5]);

            botonGuardar.setDisable(false);
            botonBorrar.setDisable(false);
        });

        botonGuardar.setOnAction(evento -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice < 0) return;

            if (campoFecha.getValue() == null || campoMotivo.getText().isBlank()) {
                mostrarAviso("Fecha y motivo de revisión son obligatorios.");
                return;
            }

            int idRevision = Integer.parseInt(revisiones.get(indice)[0]);

            botonGuardar.setDisable(true);
            new Thread(() -> {
                try {
                    GestorMascotas.actualizarRevision(idRevision, campoFecha.getValue(),
                            campoMotivo.getText(), campoDiagnostico.getText(),
                            campoNotas.getText(), campoVeterinario.getText());
                    javafx.application.Platform.runLater(() -> {
                        alGuardar.run();
                        ventana.close();
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> mostrarAviso("Error al actualizar: " + e.getMessage()));
                    botonGuardar.setDisable(false);
                }
            }, "actualizar-revision-thread").start();
        });

        botonBorrar.setOnAction(evento -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice < 0) return;

            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                    "¿Seguro que quieres borrar esta revisión?",
                    ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> respuesta = confirmacion.showAndWait();

            if (respuesta.isPresent() && respuesta.get() == ButtonType.YES) {
                int idRevision = Integer.parseInt(revisiones.get(indice)[0]);
                botonBorrar.setDisable(true);
                new Thread(() -> {
                    try {
                        GestorMascotas.borrarRevision(idRevision);
                        javafx.application.Platform.runLater(() -> {
                            alGuardar.run();
                            ventana.close();
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> mostrarAviso("Error al borrar: " + e.getMessage()));
                        botonBorrar.setDisable(false);
                    }
                }, "borrar-revision-thread").start();
            }
        });

        GridPane camposEdicion = new GridPane();
        camposEdicion.setHgap(10);
        camposEdicion.setVgap(8);
        camposEdicion.add(new Label("Fecha:"), 0, 0);
        camposEdicion.add(campoFecha, 1, 0);
        camposEdicion.add(new Label("Motivo:"), 0, 1);
        camposEdicion.add(campoMotivo, 1, 1);
        camposEdicion.add(new Label("Diagnóstico:"), 0, 2);
        camposEdicion.add(campoDiagnostico, 1, 2);
        camposEdicion.add(new Label("Notas:"), 0, 3);
        camposEdicion.add(campoNotas, 1, 3);
        camposEdicion.add(new Label("Veterinario:"), 0, 4);
        camposEdicion.add(campoVeterinario, 1, 4);

        HBox botones = new HBox(10, botonGuardar, botonBorrar);

        VBox contenido = new VBox(10,
                new Label("Selecciona la revisión a modificar:"),
                lista,
                new Label("Edita los datos y guarda:"),
                camposEdicion,
                botones
        );
        contenido.setPadding(new Insets(15));

        Scene escena = new Scene(contenido, 480, 640);
        ventana.setScene(escena);
        ventana.showAndWait();
    }

    private static void mostrarAviso(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensaje);
        alerta.showAndWait();
    }
}
