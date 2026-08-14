import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;

public class VirtuaPetApp extends Application {

    static FlowPane contenedorMascotas = new FlowPane();
    static ArrayList<Integer> idsMascotas = new ArrayList<>();

    @Override
    public void start(Stage escenario) {

        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        // Quitamos la barra de título nativa de Windows: la sustituimos por
        // una propia, más abajo, con su propio estilo, botones de minimizar
        // y cerrar, y capacidad de arrastrar la ventana. Es lo que hace que
        // la app deje de "gritar Java de escritorio clásico" a simple vista.
        escenario.initStyle(StageStyle.UNDECORATED);

        CrearTablas.crearTablas();

        Button botonAtras = new Button();
        FontIcon iconoAtras = new FontIcon(Feather.ARROW_LEFT);
        iconoAtras.setIconSize(18);
        botonAtras.setGraphic(iconoAtras);
        botonAtras.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4 8 4 8;");
        botonAtras.setOnAction(evento -> Navegador.volverAtras());

        Label tituloVentana = new Label("VirtuaPet");
        tituloVentana.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Zona "vacía" a la derecha del título: es la parte de la barra
        // que se puede arrastrar para mover la ventana (los botones no,
        // porque tienen su propia acción).
        Region espacioArrastrable = new Region();
        HBox.setHgrow(espacioArrastrable, Priority.ALWAYS);

        Button botonMinimizar = new Button();
        FontIcon iconoMinimizar = new FontIcon(Feather.MINUS);
        iconoMinimizar.setIconSize(16);
        botonMinimizar.setGraphic(iconoMinimizar);
        botonMinimizar.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 6 10 6 10;");
        botonMinimizar.setOnAction(evento -> escenario.setIconified(true));

        // Maximizar/restaurar: usa la propiedad nativa del Stage, funciona
        // igual con UNDECORATED — un toggle simple entre pantalla completa
        // (respetando la barra de tareas) y el tamaño normal.
        Button botonMaximizar = new Button();
        FontIcon iconoMaximizar = new FontIcon(Feather.MAXIMIZE_2);
        iconoMaximizar.setIconSize(15);
        botonMaximizar.setGraphic(iconoMaximizar);
        botonMaximizar.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 6 10 6 10;");
        botonMaximizar.setOnAction(evento -> escenario.setMaximized(!escenario.isMaximized()));

        Button botonCerrar = new Button();
        FontIcon iconoCerrar = new FontIcon(Feather.X);
        iconoCerrar.setIconSize(16);
        botonCerrar.setGraphic(iconoCerrar);
        botonCerrar.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 6 10 6 10;");
        botonCerrar.setOnAction(evento -> Platform.exit());
        // Al pasar el ratón por encima, se pone en rojo — igual que en
        // cualquier app real, para que quede claro que es "cerrar"
        botonCerrar.setOnMouseEntered(e -> botonCerrar.setStyle(
                "-fx-background-color: #E85D5D; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 10 6 10;"));
        botonCerrar.setOnMouseExited(e -> botonCerrar.setStyle(
                "-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 6 10 6 10;"));

        HBox topBar = new HBox(10, botonAtras, tituloVentana, espacioArrastrable, botonMinimizar, botonMaximizar, botonCerrar);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 10, 10, 14));
        topBar.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0;");

        // Arrastrar la ventana: guardamos dónde se hizo click dentro de la
        // barra, y al mover el ratón desplazamos la ventana esa diferencia.
        // Solo en la zona "vacía" (el espaciador), no sobre los botones.
        double[] offsetX = new double[1];
        double[] offsetY = new double[1];
        espacioArrastrable.setOnMousePressed(evento -> {
            offsetX[0] = evento.getSceneX();
            offsetY[0] = evento.getSceneY();
        });
        espacioArrastrable.setOnMouseDragged(evento -> {
            escenario.setX(evento.getScreenX() - offsetX[0]);
            escenario.setY(evento.getScreenY() - offsetY[0]);
        });
        // El título también sirve para arrastrar, es la zona más natural
        // donde la gente intenta mover una ventana
        tituloVentana.setOnMousePressed(evento -> {
            offsetX[0] = evento.getSceneX();
            offsetY[0] = evento.getSceneY();
        });
        tituloVentana.setOnMouseDragged(evento -> {
            escenario.setX(evento.getScreenX() - offsetX[0]);
            escenario.setY(evento.getScreenY() - offsetY[0]);
        });

        BorderPane raiz = new BorderPane();
        raiz.setTop(topBar);

        Navegador.inicializar(escenario, raiz, botonAtras, tituloVentana);
        Navegador.navegarA("VirtuaPet", VirtuaPetApp::crearVistaInicio);

        // Limitamos el tamaño al espacio real disponible en la pantalla
        // (dejando margen para la barra de tareas), en vez de forzar
        // siempre 680x680 aunque la pantalla sea más pequeña.
        double anchoDisponible = Screen.getPrimary().getVisualBounds().getWidth() - 100;
        double altoDisponible = Screen.getPrimary().getVisualBounds().getHeight() - 100;
        double ancho = Math.min(760, anchoDisponible);
        double alto = Math.min(760, altoDisponible);

        Scene escena = new Scene(raiz, ancho, alto);
        
        URL cssUrl = VirtuaPetApp.class.getResource("/styles.css");
        if (cssUrl != null) {
            escena.getStylesheets().add(cssUrl.toExternalForm());
        }

        escenario.setTitle("VirtuaPet");
        escenario.setScene(escena);
        escenario.centerOnScreen();
        escenario.show();
    }

    public static Node crearVistaInicio() {
        Button botonAñadir = new Button("Añadir mascota");
        FontIcon iconoAñadir = new FontIcon(Feather.PLUS_CIRCLE);
        iconoAñadir.setIconSize(16);
        botonAñadir.setGraphic(iconoAñadir);
        botonAñadir.getStyleClass().add("btn-primary");
        botonAñadir.setOnAction(evento -> abrirFormularioNuevaMascota());

        // Copia de seguridad manual: un único .zip con la base de datos y
        // todos los documentos de todas las mascotas. Es el mismo formato
        // que, el día de mañana, la app Android podría leer para importar
        // los datos vía WiFi — por eso vale la pena tenerlo ya ahora.
        Button botonExportar = new Button("Exportar todos los datos");
        FontIcon iconoExportar = new FontIcon(Feather.DOWNLOAD);
        iconoExportar.setIconSize(16);
        botonExportar.setGraphic(iconoExportar);
        botonExportar.setOnAction(evento -> exportarTodosLosDatos(botonExportar));

        HBox filaBotones = new HBox(10, botonAñadir, botonExportar);

        contenedorMascotas.setHgap(12);
        contenedorMascotas.setVgap(12);
        contenedorMascotas.setPadding(new Insets(12));
        contenedorMascotas.setStyle("-fx-background-color: #F5F5F5;");

        VBox contenido = new VBox(12, filaBotones, contenedorMascotas);
        contenido.setPadding(new Insets(12));
        contenido.setStyle("-fx-background-color: #F5F5F5;");

        cargarListaMascotas();

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        return scroll;
    }

    private static void exportarTodosLosDatos(Button botonExportar) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar copia de seguridad");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivo ZIP", "*.zip"));
        chooser.setInitialFileName("virtuapet_backup_" + LocalDate.now() + ".zip");

        File destino = chooser.showSaveDialog(Navegador.getEscenario());
        if (destino == null) return; // el usuario canceló el diálogo

        botonExportar.setDisable(true);
        new Thread(() -> {
            try {
                GestorArchivos.exportarTodo(destino);
                Platform.runLater(() -> {
                    Alert exito = new Alert(Alert.AlertType.INFORMATION,
                            "Copia de seguridad generada correctamente:\n" + destino.getAbsolutePath());
                    exito.showAndWait();
                    botonExportar.setDisable(false);
                });
            } catch (Exception e) {
                AppLogger.logSevere("Error exportando copia de seguridad: " + e.getMessage());
                Platform.runLater(() -> {
                    Alert error = new Alert(Alert.AlertType.ERROR,
                            "No se pudo generar la copia de seguridad: " + e.getMessage());
                    error.showAndWait();
                    botonExportar.setDisable(false);
                });
            }
        }, "exportar-backup-thread").start();
    }

    static void cargarListaMascotas() {
        contenedorMascotas.getChildren().clear();
        idsMascotas.clear();

        new Thread(() -> {
            try {
                ArrayList<String[]> mascotas = GestorMascotas.obtenerListaMascotas();
                Platform.runLater(() -> {
                    contenedorMascotas.getChildren().clear();
                    idsMascotas.clear();
                    for (String[] datos : mascotas) {
                        int idMascota = Integer.parseInt(datos[0]);
                        String nombre = datos[1];
                        
                        VBox tarjeta = crearTarjetaMascota(nombre, idMascota);
                        contenedorMascotas.getChildren().add(tarjeta);
                        idsMascotas.add(idMascota);
                    }
                });
            } catch (Exception e) {
                AppLogger.logSevere("Error cargando lista de mascotas: " + e.getMessage());
                Platform.runLater(() -> System.err.println("Error al cargar mascotas: " + e.getMessage()));
            }
        }, "cargar-lista-thread").start();
    }

    private static VBox crearTarjetaMascota(String nombre, int idMascota) {
        Label avatar = new Label(nombre.isEmpty() ? "?" : nombre.substring(0, 1).toUpperCase());
        avatar.getStyleClass().add("avatar-circle");
        avatar.setAlignment(Pos.CENTER);

        Label nombreLabel = new Label(nombre);
        nombreLabel.getStyleClass().add("pet-card-name");
        nombreLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-alignment: center;");
        nombreLabel.setMaxWidth(80);
        nombreLabel.setWrapText(true);

        VBox tarjeta = new VBox(8, avatar, nombreLabel);
        tarjeta.getStyleClass().add("card");
        tarjeta.getStyleClass().add("clickable-card");
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setPadding(new Insets(12));
        tarjeta.setPrefWidth(110);
        tarjeta.setMinHeight(120);

        tarjeta.setOnMouseClicked(evento -> {
            if (evento.getButton() == MouseButton.PRIMARY && evento.getClickCount() == 1) {
                FichaMascotaVentana.abrir(idMascota);
            }
        });

        return tarjeta;
    }

    static void abrirFormularioNuevaMascota() {
        FormularioMascota.abrir();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
