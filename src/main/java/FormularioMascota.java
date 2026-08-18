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
import java.util.*;

public class FormularioMascota {

    // Mapas de razas por especie
    private static final Map<String, List<String>> RAZAS_POR_ESPECIE = new HashMap<>();
    private static final List<String> ESPECIES = Arrays.asList(
        "Perro", "Gato", "Ave", "Conejo", "Roedor", "Reptil", "Otro"
    );

    static {
        // Razas de perro
        RAZAS_POR_ESPECIE.put("Perro", Arrays.asList(
            "Labrador Retriever", "Pastor Alemán", "Bulldog Francés", "Golden Retriever",
            "Chihuahua", "Poodle", "Beagle", "Rottweiler", "Yorkshire Terrier",
            "Boxer", "Dachshund", "Pastor Belga", "Husky Siberiano", "Border Collie",
            "Bulldog Inglés", "Shih Tzu", "Pomerania", "Doberman", "Gran Danés",
            "Cocker Spaniel", "San Bernardo", "Basset Hound", "Mestizo", "Otra"
        ));

        // Razas de gato
        RAZAS_POR_ESPECIE.put("Gato", Arrays.asList(
            "Siamés", "Persa", "Común Europeo", "Maine Coon", "Bengalí",
            "Ragdoll", "Burmés", "Sphynx", "British Shorthair", "Scottish Fold",
            "Angora Turco", "Ruso Azul", "Savannah", "Mestizo", "Otra"
        ));

        // Razas genéricas para otras especies
        RAZAS_POR_ESPECIE.put("Ave", Arrays.asList("Canario", "Periquito", "Loro", "Agaporni", "Otra"));
        RAZAS_POR_ESPECIE.put("Conejo", Arrays.asList("Cabeza de León", "Holandés", "Mini Lop", "Otra"));
        RAZAS_POR_ESPECIE.put("Roedor", Arrays.asList("Hámster", "Cobaya", "Ratón", "Otra"));
        RAZAS_POR_ESPECIE.put("Reptil", Arrays.asList("Iguana", "Tortuga", "Serpiente", "Otra"));
        RAZAS_POR_ESPECIE.put("Otro", Collections.singletonList("Otra"));
    }

    public static void abrir() {
        Navegador.navegarA("Nueva mascota", () -> crearVista(null, null));
    }

    public static void abrirEdicion(int idExistente, String[] datosActuales) {
        Navegador.navegarA("Editar mascota", () -> crearVista(idExistente, datosActuales));
    }

    public static Node crearVista(Integer idExistente, String[] datosActuales) {
        boolean esEdicion = (idExistente != null);

        TextField campoNombre = new TextField();
        
        // ComboBox para Especie
        ComboBox<String> campoEspecie = new ComboBox<>();
        campoEspecie.getItems().addAll(ESPECIES);
        campoEspecie.setEditable(false);
        
        // ComboBox para Raza (dependiente de especie)
        ComboBox<String> campoRaza = new ComboBox<>();
        campoRaza.setEditable(false);
        
        // TextField para raza personalizada (cuando se selecciona "Otra")
        TextField campoRazaPersonalizada = new TextField();
        campoRazaPersonalizada.setPromptText("Escribe la raza exacta");
        campoRazaPersonalizada.setVisible(false);
        campoRazaPersonalizada.setManaged(false);
        
        // TextField para especie/raza cuando se selecciona "Otro" en especie
        TextField campoEspecieRazaOtro = new TextField();
        campoEspecieRazaOtro.setPromptText("Escribe la especie y raza");
        campoEspecieRazaOtro.setVisible(false);
        campoEspecieRazaOtro.setManaged(false);
        
        DatePicker campoFecha = new DatePicker();

        ComboBox<String> campoSexo = new ComboBox<>();
        campoSexo.getItems().addAll("Macho", "Hembra");

        TextField campoColor = new TextField();
        TextField campoMicrochip = new TextField();
        campoMicrochip.setPromptText("Opcional");

        // Listener para cambiar las opciones de raza según la especie
        campoEspecie.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                actualizarOpcionesRaza(campoRaza, campoRazaPersonalizada, campoEspecieRazaOtro, newVal);
            }
        });

        // Listener para mostrar/ocultar campo de raza personalizada
        campoRaza.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if ("Otra".equals(newVal)) {
                campoRazaPersonalizada.setVisible(true);
                campoRazaPersonalizada.setManaged(true);
            } else {
                campoRazaPersonalizada.setVisible(false);
                campoRazaPersonalizada.setManaged(false);
                campoRazaPersonalizada.setText("");
            }
        });

        if (esEdicion && datosActuales != null) {
            campoNombre.setText(datosActuales[0]);
            String especieGuardada = datosActuales[1];
            String razaGuardada = datosActuales[2];
            
            // Establecer especie (si no está en la lista, usar "Otro")
            if (ESPECIES.contains(especieGuardada)) {
                campoEspecie.setValue(especieGuardada);
            } else {
                campoEspecie.setValue("Otro");
                campoEspecieRazaOtro.setText(especieGuardada + (razaGuardada != null && !razaGuardada.isEmpty() ? " - " + razaGuardada : ""));
            }
            
            // Actualizar opciones de raza según la especie
            actualizarOpcionesRaza(campoRaza, campoRazaPersonalizada, campoEspecieRazaOtro, campoEspecie.getValue());
            
            // Establecer raza (si no está en la lista, usar "Otra" y mostrar en campo personalizado)
            if (!"Otro".equals(campoEspecie.getValue()) && razaGuardada != null && !razaGuardada.isEmpty()) {
                List<String> razas = RAZAS_POR_ESPECIE.get(campoEspecie.getValue());
                if (razas != null && razas.contains(razaGuardada)) {
                    campoRaza.setValue(razaGuardada);
                } else {
                    campoRaza.setValue("Otra");
                    campoRazaPersonalizada.setText(razaGuardada);
                }
            }
            
            campoFecha.setValue(LocalDate.parse(datosActuales[3]));
            campoSexo.setValue(datosActuales[4]);
            campoColor.setText(datosActuales[5]);
            campoMicrochip.setText(datosActuales[6].equals("(sin microchip)") ? "" : datosActuales[6]);
        } else {
            // Valores por defecto para nueva mascota
            campoEspecie.setValue("Perro");
            actualizarOpcionesRaza(campoRaza, campoRazaPersonalizada, campoEspecieRazaOtro, "Perro");
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
        formulario.add(campoEspecieRazaOtro, 1, 1);
        formulario.add(new Label("Raza:"), 0, 2);
        formulario.add(campoRaza, 1, 2);
        formulario.add(campoRazaPersonalizada, 1, 2);
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
            // Obtener valores de especie y raza según los controles visibles
            String especieFinal;
            String razaFinal;
            
            if ("Otro".equals(campoEspecie.getValue())) {
                especieFinal = campoEspecieRazaOtro.getText().trim();
                razaFinal = "";
                if (especieFinal.isBlank()) {
                    mostrarError("Cuando seleccionas 'Otro', debes escribir la especie y raza.");
                    return;
                }
            } else {
                especieFinal = campoEspecie.getValue();
                if ("Otra".equals(campoRaza.getValue())) {
                    razaFinal = campoRazaPersonalizada.getText().trim();
                    if (razaFinal.isBlank()) {
                        mostrarError("Cuando seleccionas 'Otra' raza, debes escribir la raza exacta.");
                        return;
                    }
                } else {
                    razaFinal = campoRaza.getValue();
                }
            }
            
            if (campoNombre.getText().isBlank() || especieFinal.isBlank()) {
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
                    especieFinal,
                    razaFinal,
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

    private static void actualizarOpcionesRaza(ComboBox<String> campoRaza, TextField campoRazaPersonalizada, 
                                                TextField campoEspecieRazaOtro, String especie) {
        campoRaza.getItems().clear();
        campoRazaPersonalizada.setVisible(false);
        campoRazaPersonalizada.setManaged(false);
        campoRazaPersonalizada.setText("");
        
        if ("Otro".equals(especie)) {
            // Mostrar campo para escribir especie y raza
            campoEspecieRazaOtro.setVisible(true);
            campoEspecieRazaOtro.setManaged(true);
            campoRaza.setVisible(false);
            campoRaza.setManaged(false);
        } else {
            // Ocultar campo de otro y mostrar ComboBox de raza
            campoEspecieRazaOtro.setVisible(false);
            campoEspecieRazaOtro.setManaged(false);
            campoEspecieRazaOtro.setText("");
            campoRaza.setVisible(true);
            campoRaza.setManaged(true);
            
            List<String> razas = RAZAS_POR_ESPECIE.get(especie);
            if (razas != null) {
                campoRaza.getItems().addAll(razas);
                if (!razas.isEmpty()) {
                    campoRaza.setValue(razas.get(0));
                }
            }
        }
    }
}
