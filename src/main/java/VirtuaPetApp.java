import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.ArrayList;

public class VirtuaPetApp extends Application {

    // ListView<String> es un componente de JavaFX que muestra una lista
    // vertical de elementos de texto. La declaramos aquí, como atributo
    // de la clase (no dentro de un método), para que tanto start() como
    // el método que refresca la lista puedan acceder a ella.
    static ListView<String> listaMascotas = new ListView<>();

    // NUEVO: guarda los ids reales, en el mismo orden que se muestran en
    // la lista visual. Igual que hacíamos en consola con la lista de
    // microchips/ids para poder elegir por número, aquí lo necesitamos
    // para saber a qué mascota corresponde la fila en la que hicen doble clic.
    static ArrayList<Integer> idsMascotas = new ArrayList<>();

    @Override
    public void start(Stage escenario) {

        CrearTablas.crearTablas(); // igual que en el Main de consola: asegura las tablas

        Button botonAñadir = new Button("+ Añadir mascota");
        botonAñadir.setOnAction(evento -> abrirFormularioNuevaMascota());

        // BorderPane organiza la pantalla en 5 zonas: arriba, abajo,
        // izquierda, derecha y centro. Aquí solo usamos "top" (el botón)
        // y "center" (la lista), el resto quedan vacías por ahora.
        BorderPane raiz = new BorderPane();
        raiz.setTop(botonAñadir);
        raiz.setCenter(listaMascotas);
        BorderPane.setMargin(botonAñadir, new Insets(10)); // un poco de aire alrededor del botón

        cargarListaMascotas(); // rellena la lista con lo que ya haya en la base de datos

        // NUEVO: al hacer doble clic en un elemento de la lista, se abre
        // su ficha completa. getClickCount() == 2 distingue un doble clic
        // de un simple clic (que solo selecciona, sin abrir nada).
        listaMascotas.setOnMouseClicked(evento -> {
            if (evento.getClickCount() == 2) {
                int indiceSeleccionado = listaMascotas.getSelectionModel().getSelectedIndex();
                if (indiceSeleccionado >= 0) { // -1 significa "nada seleccionado"
                    int idMascota = idsMascotas.get(indiceSeleccionado);
                    FichaMascotaVentana.abrir(idMascota);
                }
            }
        });

        Scene escena = new Scene(raiz, 400, 500);
        escenario.setTitle("VirtuaPet");
        escenario.setScene(escena);
        escenario.show();
    }

    // Vacía la lista visual y la vuelve a rellenar leyendo la base de datos.
    // La llamamos al arrancar, y también cada vez que se guarda una mascota
    // nueva, para que la ventana principal se actualice sola.
    static void cargarListaMascotas() {
        listaMascotas.getItems().clear();
        idsMascotas.clear();

        // Ejecutar en background para no bloquear la UI
        new Thread(() -> {
            try {
                ArrayList<String[]> mascotas = GestorMascotas.obtenerListaMascotas();
                Platform.runLater(() -> {
                    listaMascotas.getItems().clear();
                    idsMascotas.clear();
                    for (String[] datos : mascotas) {
                        listaMascotas.getItems().add(datos[1] + "  (" + datos[2] + ")");
                        idsMascotas.add(Integer.parseInt(datos[0]));
                    }
                });
            } catch (Exception e) {
                AppLogger.logSevere("Error cargando lista de mascotas: " + e.getMessage());
                Platform.runLater(() -> System.err.println("Error al cargar mascotas: " + e.getMessage()));
            }
        }, "cargar-lista-thread").start();
    }

    static void abrirFormularioNuevaMascota() {
        FormularioMascota.abrir();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
