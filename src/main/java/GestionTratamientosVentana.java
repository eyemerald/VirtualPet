import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;

public class GestionTratamientosVentana {

    static void abrir(int idMascota, Runnable alGuardar) {

        ArrayList<String[]> tratamientos = GestorMascotas.obtenerTratamientosConId(idMascota);

        if (tratamientos.isEmpty()) {
            Alert aviso = new Alert(Alert.AlertType.INFORMATION, "Esta mascota no tiene tratamientos registrados.");
            aviso.showAndWait();
            return;
        }

        Stage ventana = new Stage();
        ventana.setTitle("Gestionar tratamientos");
        ventana.initModality(Modality.APPLICATION_MODAL);

        ListView<String> lista = new ListView<>();
        for (String[] t : tratamientos) {
            // t = [id, nombreMedicamento, dosis, frecuencia, fechaInicio, fechaFin]
            String textoFin = t[5].isEmpty() ? "crónico/indefinido" : "hasta " + t[5];
            lista.getItems().add(t[1] + " (" + t[2] + ") - " + textoFin);
        }

        // Campos editables, vacíos hasta que se seleccione algo de la lista
        TextField campoMedicamento = new TextField();
        TextField campoDosis = new TextField();
        TextField campoFrecuencia = new TextField();
        DatePicker campoInicio = new DatePicker();
        DatePicker campoFin = new DatePicker();
        CheckBox marcarCronico = new CheckBox("Sin fecha fin (crónico)");
        marcarCronico.setOnAction(e -> campoFin.setDisable(marcarCronico.isSelected()));

        Button botonGuardar = new Button("Guardar cambios");
        botonGuardar.setDisable(true); // deshabilitado hasta que elijan un tratamiento de la lista

        // NUEVO: un "listener" — código que se ejecuta automáticamente
        // cada vez que cambia algo, sin que tú lo llames tú mismo.
        // selectedItemProperty() es el "valor actualmente seleccionado en
        // la lista"; addListener(...) le dice "avísame cada vez que cambie".
        // Aquí lo usamos para rellenar los campos en cuanto seleccionan
        // un tratamiento distinto en la lista.
        lista.getSelectionModel().selectedItemProperty().addListener((observable, valorAntiguo, valorNuevo) -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice < 0) return; // nada seleccionado, no hacemos nada

            String[] t = tratamientos.get(indice);
            campoMedicamento.setText(t[1]);
            campoDosis.setText(t[2]);
            campoFrecuencia.setText(t[3]);
            campoInicio.setValue(LocalDate.parse(t[4]));

            if (t[5].isEmpty()) {
                marcarCronico.setSelected(true);
                campoFin.setValue(null);
                campoFin.setDisable(true);
            } else {
                marcarCronico.setSelected(false);
                campoFin.setValue(LocalDate.parse(t[5]));
                campoFin.setDisable(false);
            }

            botonGuardar.setDisable(false); // ya hay algo seleccionado, se puede guardar
        });

        botonGuardar.setOnAction(evento -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice < 0) return;

            if (campoMedicamento.getText().isBlank() || campoInicio.getValue() == null) {
                mostrarAviso("Medicamento y fecha de inicio son obligatorios.");
                return;
            }
            // NUEVO: misma validación que al crear, también al editar
            if (!marcarCronico.isSelected() && campoFin.getValue() != null
                    && campoFin.getValue().isBefore(campoInicio.getValue())) {
                mostrarAviso("La fecha fin no puede ser anterior a la fecha de inicio.");
                return;
            }

            int idTratamiento = Integer.parseInt(tratamientos.get(indice)[0]);
            LocalDate fechaFin = marcarCronico.isSelected() ? null : campoFin.getValue();

            botonGuardar.setDisable(true);
            new Thread(() -> {
                try {
                    GestorMascotas.actualizarTratamiento(idTratamiento, campoMedicamento.getText(),
                            campoDosis.getText(), campoFrecuencia.getText(), campoInicio.getValue(), fechaFin);
                    javafx.application.Platform.runLater(() -> {
                        alGuardar.run();
                        ventana.close();
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> mostrarAviso("Error al actualizar: " + e.getMessage()));
                    botonGuardar.setDisable(false);
                }
            }, "actualizar-tratamiento-thread").start();
        });

        GridPane camposEdicion = new GridPane();
        camposEdicion.setHgap(10);
        camposEdicion.setVgap(8);
        camposEdicion.add(new Label("Medicamento:"), 0, 0);
        camposEdicion.add(campoMedicamento, 1, 0);
        camposEdicion.add(new Label("Dosis:"), 0, 1);
        camposEdicion.add(campoDosis, 1, 1);
        camposEdicion.add(new Label("Frecuencia:"), 0, 2);
        camposEdicion.add(campoFrecuencia, 1, 2);
        camposEdicion.add(new Label("Fecha inicio:"), 0, 3);
        camposEdicion.add(campoInicio, 1, 3);
        camposEdicion.add(new Label("Fecha fin:"), 0, 4);
        camposEdicion.add(campoFin, 1, 4);
        camposEdicion.add(marcarCronico, 1, 5);

        VBox contenido = new VBox(10,
                new Label("Selecciona el tratamiento a modificar:"),
                lista,
                new Label("Edita los datos y guarda:"),
                camposEdicion,
                botonGuardar
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
