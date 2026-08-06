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

public class GestionPesosVentana {

    static void abrir(int idMascota, Runnable alGuardar) {

        ArrayList<String[]> pesos = GestorMascotas.obtenerPesosConId(idMascota);

        if (pesos.isEmpty()) {
            Alert aviso = new Alert(Alert.AlertType.INFORMATION, "Esta mascota no tiene registros de peso.");
            aviso.showAndWait();
            return;
        }

        Stage ventana = new Stage();
        ventana.setTitle("Gestionar pesos");
        ventana.initModality(Modality.APPLICATION_MODAL);

        ListView<String> lista = new ListView<>();
        for (String[] p : pesos) {
            // p = [id, fecha, peso, notas]
            lista.getItems().add(p[1] + ": " + p[2] + " kg");
        }

        DatePicker campoFecha = new DatePicker();
        TextField campoPeso = new TextField();
        TextArea campoNotas = new TextArea();
        campoNotas.setWrapText(true);
        campoNotas.setPrefRowCount(3);

        Button botonGuardar = new Button("Guardar cambios");
        botonGuardar.setDisable(true);

        Button botonBorrar = new Button("Borrar registro");
        botonBorrar.setDisable(true);

        lista.getSelectionModel().selectedItemProperty().addListener((observable, valorAntiguo, valorNuevo) -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice < 0) return;

            String[] p = pesos.get(indice);
            campoFecha.setValue(LocalDate.parse(p[1]));
            campoPeso.setText(p[2]);
            campoNotas.setText(p[3]);

            botonGuardar.setDisable(false);
            botonBorrar.setDisable(false);
        });

        botonGuardar.setOnAction(evento -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice < 0) return;

            if (campoFecha.getValue() == null || campoPeso.getText().isBlank()) {
                mostrarAviso("Fecha y peso son obligatorios.");
                return;
            }

            double peso;
            try {
                peso = Double.parseDouble(campoPeso.getText());
                if (peso <= 0) {
                    mostrarAviso("El peso debe ser un número positivo.");
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarAviso("El peso debe ser un número válido.");
                return;
            }

            int idPeso = Integer.parseInt(pesos.get(indice)[0]);

            botonGuardar.setDisable(true);
            new Thread(() -> {
                try {
                    GestorMascotas.actualizarPeso(idPeso, campoFecha.getValue(), peso, campoNotas.getText());
                    javafx.application.Platform.runLater(() -> {
                        alGuardar.run();
                        ventana.close();
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> mostrarAviso("Error al actualizar: " + e.getMessage()));
                    botonGuardar.setDisable(false);
                }
            }, "actualizar-peso-thread").start();
        });

        botonBorrar.setOnAction(evento -> {
            int indice = lista.getSelectionModel().getSelectedIndex();
            if (indice < 0) return;

            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                    "¿Seguro que quieres borrar este registro de peso?",
                    ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> respuesta = confirmacion.showAndWait();

            if (respuesta.isPresent() && respuesta.get() == ButtonType.YES) {
                int idPeso = Integer.parseInt(pesos.get(indice)[0]);
                botonBorrar.setDisable(true);
                new Thread(() -> {
                    try {
                        GestorMascotas.borrarPeso(idPeso);
                        javafx.application.Platform.runLater(() -> {
                            alGuardar.run();
                            ventana.close();
                        });
                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> mostrarAviso("Error al borrar: " + e.getMessage()));
                        botonBorrar.setDisable(false);
                    }
                }, "borrar-peso-thread").start();
            }
        });

        GridPane camposEdicion = new GridPane();
        camposEdicion.setHgap(10);
        camposEdicion.setVgap(8);
        camposEdicion.add(new Label("Fecha:"), 0, 0);
        camposEdicion.add(campoFecha, 1, 0);
        camposEdicion.add(new Label("Peso (kg):"), 0, 1);
        camposEdicion.add(campoPeso, 1, 1);
        camposEdicion.add(new Label("Notas:"), 0, 2);
        camposEdicion.add(campoNotas, 1, 2);

        HBox botones = new HBox(10, botonGuardar, botonBorrar);

        VBox contenido = new VBox(10,
                new Label("Selecciona el registro a modificar:"),
                lista,
                new Label("Edita los datos y guarda:"),
                camposEdicion,
                botones
        );
        contenido.setPadding(new Insets(15));

        Scene escena = new Scene(contenido, 420, 520);
        ventana.setScene(escena);
        ventana.showAndWait();
    }

    private static void mostrarAviso(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensaje);
        alerta.showAndWait();
    }
}
