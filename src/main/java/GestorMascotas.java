import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class GestorMascotas {

    static final String URL = "jdbc:sqlite:virtuapet.db";

    static Connection abrirConexion() throws SQLException {
        Connection conexion = DriverManager.getConnection(URL);
        try (Statement pragma = conexion.createStatement()) {
            pragma.execute("PRAGMA foreign_keys = ON");
        }
        return conexion;
    }

    // NUEVO: pensado para JavaFX, no para consola. Devuelve cada mascota
    // como un array de texto [id, nombre, especie, raza], sin imprimir nada
    // por consola (a diferencia de mostrarMascotasParaSeleccion, que sí
    // imprime, porque esa es para el menú de texto).
    static ArrayList<String[]> obtenerListaMascotas() {
        ArrayList<String[]> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, especie, raza FROM mascotas";

        try (Connection conexion = abrirConexion();
             Statement sentencia = conexion.createStatement();
             ResultSet filas = sentencia.executeQuery(sql)) {

            while (filas.next()) {
                lista.add(new String[]{
                        filas.getString("id"),
                        filas.getString("nombre"),
                        filas.getString("especie"),
                        filas.getString("raza")
                });
            }

        } catch (SQLException e) {
            System.out.println("Error al leer las mascotas: " + e.getMessage());
        }

        return lista;
    }

    // ================== SELECCIÓN ==================

    static ArrayList<Integer> mostrarMascotasParaSeleccion() {
        ArrayList<Integer> ids = new ArrayList<>();
        String sql = "SELECT id, nombre, especie FROM mascotas";

        try (Connection conexion = abrirConexion();
             Statement sentencia = conexion.createStatement();
             ResultSet filas = sentencia.executeQuery(sql)) {

            int numero = 1;
            while (filas.next()) {
                System.out.println(numero + ". " + filas.getString("nombre")
                        + " (" + filas.getString("especie") + ")");
                ids.add(filas.getInt("id"));
                numero++;
            }

        } catch (SQLException e) {
            System.out.println("Error al leer las mascotas: " + e.getMessage());
        }

        return ids;
    }

    // ================== GUARDAR ==================

    static int guardarMascota(Mascota m) {
        String sql = "INSERT INTO mascotas (nombre, especie, raza, fechaNacimiento, sexo, color, microchip) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            sentencia.setString(1, m.getNombre());
            sentencia.setString(2, m.getEspecie());
            sentencia.setString(3, m.getRaza());
            sentencia.setString(4, m.getFechaNacimiento().toString());
            sentencia.setString(5, m.getSexo());
            sentencia.setString(6, m.getColor());
            sentencia.setString(7, m.getMicrochip());

            sentencia.executeUpdate();

            try (ResultSet claveGenerada = sentencia.getGeneratedKeys()) {
                if (claveGenerada.next()) {
                    int idNuevo = claveGenerada.getInt(1);
                    System.out.println("Mascota guardada: " + m.getNombre() + " (id " + idNuevo + ")");
                    return idNuevo;
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al guardar la mascota: " + e.getMessage());
        }

        return -1;
    }

    static void guardarVacuna(int idMascota, Vacuna v) {
        String sql = "INSERT INTO vacunas (mascota_id, nombre, fechaAplicacion, fechaProximaDosis, veterinario, lote) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idMascota);
            sentencia.setString(2, v.getNombre());
            sentencia.setString(3, v.getFechaAplicacion().toString());
            sentencia.setString(4, v.getFechaProximaDosis().toString());
            sentencia.setString(5, v.getVeterinario());
            sentencia.setString(6, v.getLote());

            sentencia.executeUpdate();
            System.out.println("Vacuna guardada.");

        } catch (SQLException e) {
            System.out.println("Error al guardar la vacuna: " + e.getMessage());
        }
    }

    static void guardarRevision(int idMascota, Revision r) {
        String sql = "INSERT INTO revision (mascota_id, fechaRevision, motivoRevision, diagnostico, notas, veterinario) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idMascota);
            sentencia.setString(2, r.getFecha().toString());
            sentencia.setString(3, r.getMotivo());
            sentencia.setString(4, r.getDiagnostico());
            sentencia.setString(5, r.getNotas());
            sentencia.setString(6, r.getVeterinario());

            sentencia.executeUpdate();
            System.out.println("Revisión guardada.");

        } catch (SQLException e) {
            System.out.println("Error al guardar la revisión: " + e.getMessage());
        }
    }

    // CORREGIDO: los nombres de columna deben coincidir EXACTAMENTE con
    // los que se declararon en CrearTablas (mascota_id, nombreMedicamento,
    // fechaInicio, fechaFin) — antes usaba nombres distintos que no existían
    // en ninguna tabla real, y eso es lo que daba el error.
    static void guardarTratamiento(int idMascota, Tratamiento t) {
        String sql = "INSERT INTO tratamientos (mascota_id, nombreMedicamento, dosis, frecuencia, fechaInicio, fechaFin) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idMascota);
            sentencia.setString(2, t.getNombreMedicamento());
            sentencia.setString(3, t.getDosis());
            sentencia.setString(4, t.getFrecuencia());
            sentencia.setString(5, t.getFechaInicio().toString());

            // Si fechaFin es null (tratamiento crónico/sin fin), guardamos NULL en la BD
            if (t.getFechaFin() != null) {
                sentencia.setString(6, t.getFechaFin().toString());
            } else {
                sentencia.setNull(6, java.sql.Types.VARCHAR);
            }

            sentencia.executeUpdate();
            System.out.println("Tratamiento guardado.");

        } catch (SQLException e) {
            System.out.println("Error al guardar tratamiento: " + e.getMessage());
        }
    }

    static void guardarPeso(int idMascota, RegistroPeso p) {
        String sql = "INSERT INTO pesos (mascota_id, fecha, peso, notas) VALUES (?, ?, ?, ?)";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idMascota);
            sentencia.setString(2, p.getFecha().toString());
            sentencia.setDouble(3, p.getPeso());
            sentencia.setString(4, p.getNotas());

            sentencia.executeUpdate();
            System.out.println("Registro de peso guardado.");

        } catch (SQLException e) {
            System.out.println("Error al guardar el peso: " + e.getMessage());
        }
    }

    // ================== ACTUALIZAR ==================

    static void actualizarMascota(int id, Mascota datosNuevos) {
        String sql = "UPDATE mascotas SET nombre = ?, especie = ?, raza = ?, "
                + "fechaNacimiento = ?, sexo = ?, color = ?, microchip = ? WHERE id = ?";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, datosNuevos.getNombre());
            sentencia.setString(2, datosNuevos.getEspecie());
            sentencia.setString(3, datosNuevos.getRaza());
            sentencia.setString(4, datosNuevos.getFechaNacimiento().toString());
            sentencia.setString(5, datosNuevos.getSexo());
            sentencia.setString(6, datosNuevos.getColor());
            sentencia.setString(7, datosNuevos.getMicrochip());
            sentencia.setInt(8, id);

            int filasActualizadas = sentencia.executeUpdate();
            if (filasActualizadas > 0) {
                System.out.println("Mascota actualizada correctamente.");
            } else {
                System.out.println("No se encontró ninguna mascota con ese id.");
            }

        } catch (SQLException e) {
            System.out.println("Error al actualizar la mascota: " + e.getMessage());
        }
    }

    // ================== BORRAR ==================

    static void borrarMascota(int id) {
        String sql = "DELETE FROM mascotas WHERE id = ?";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, id);
            int filasBorradas = sentencia.executeUpdate();

            if (filasBorradas > 0) {
                System.out.println("Mascota y todos sus datos asociados han sido borrados.");
            } else {
                System.out.println("No se encontró ninguna mascota con ese id.");
            }

        } catch (SQLException e) {
            System.out.println("Error al borrar la mascota: " + e.getMessage());
        }
    }

    static void borrarVacuna(int id) {
        String sql = "DELETE FROM vacunas WHERE id = ?";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, id);
            int filasBorradas = sentencia.executeUpdate();

            if (filasBorradas > 0) {
                System.out.println("Vacuna borrada.");
            } else {
                System.out.println("No se encontró ninguna vacuna con ese id.");
            }

        } catch (SQLException e) {
            System.out.println("Error al borrar la vacuna: " + e.getMessage());
        }
    }

    static void borrarRevision(int id) {
        String sql = "DELETE FROM revision WHERE id = ?";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, id);
            int filasBorradas = sentencia.executeUpdate();

            if (filasBorradas > 0) {
                System.out.println("Revisión borrada.");
            } else {
                System.out.println("No se encontró ninguna revisión con ese id.");
            }

        } catch (SQLException e) {
            System.out.println("Error al borrar la revisión: " + e.getMessage());
        }
    }

    static void borrarPeso(int id) {
        String sql = "DELETE FROM pesos WHERE id = ?";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, id);
            int filasBorradas = sentencia.executeUpdate();

            if (filasBorradas > 0) {
                System.out.println("Registro de peso borrado.");
            } else {
                System.out.println("No se encontró ningún registro de peso con ese id.");
            }

        } catch (SQLException e) {
            System.out.println("Error al borrar el registro de peso: " + e.getMessage());
        }
    }

    // ================== DATOS PARA EL PDF ==================

    static String[] obtenerDatosBasicos(int id) {
        String sql = "SELECT nombre, especie, raza, fechaNacimiento, sexo, color, microchip "
                + "FROM mascotas WHERE id = ?";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, id);

            try (ResultSet fila = sentencia.executeQuery()) {
                if (fila.next()) {
                    String microchip = fila.getString("microchip");
                    return new String[]{
                            fila.getString("nombre"),
                            fila.getString("especie"),
                            fila.getString("raza"),
                            fila.getString("fechaNacimiento"),
                            fila.getString("sexo"),
                            fila.getString("color"),
                            (microchip == null || microchip.isEmpty()) ? "(sin microchip)" : microchip
                    };
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al leer los datos de la mascota: " + e.getMessage());
        }

        return null;
    }

    // NUEVO: ahora cada línea de vacuna incluye un aviso según la fecha
    // de la próxima dosis, comparándola con hoy.
    static ArrayList<String> obtenerLineasVacunas(int idMascota) {
        ArrayList<String> lineas = new ArrayList<>();
        String sql = "SELECT * FROM vacunas WHERE mascota_id = ?";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idMascota);

            try (ResultSet filas = sentencia.executeQuery()) {
                while (filas.next()) {
                    String nombre = filas.getString("nombre");
                    String fechaAplicacion = filas.getString("fechaAplicacion");
                    String fechaProximaTexto = filas.getString("fechaProximaDosis");

                    String aviso = "";
                    if (fechaProximaTexto != null && !fechaProximaTexto.isEmpty()) {
                        LocalDate fechaProxima = LocalDate.parse(fechaProximaTexto);
                        long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), fechaProxima);

                        if (diasRestantes < 0) {
                            aviso = " ⚠ VENCIDA hace " + Math.abs(diasRestantes) + " días";
                        } else if (diasRestantes <= 30) {
                            aviso = " ⚠ próxima en " + diasRestantes + " días";
                        }
                    }

                    lineas.add(nombre + " (" + fechaAplicacion + ", próxima: " + fechaProximaTexto + ")" + aviso);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al leer las vacunas: " + e.getMessage());
        }

        return lineas;
    }

    // NUEVO: calcula la edad en años y meses a partir del texto de fecha
    // que devuelve la base de datos (formato ISO, yyyy-MM-dd)
    static String calcularEdad(String fechaNacimientoTexto) {
        LocalDate fechaNacimiento = LocalDate.parse(fechaNacimientoTexto);
        java.time.Period edad = java.time.Period.between(fechaNacimiento, LocalDate.now());
        return edad.getYears() + " años y " + edad.getMonths() + " meses";
    }

    static ArrayList<String> obtenerLineasRevisiones(int idMascota) {
        ArrayList<String> lineas = new ArrayList<>();
        String sql = "SELECT * FROM revision WHERE mascota_id = ?";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idMascota);

            try (ResultSet filas = sentencia.executeQuery()) {
                while (filas.next()) {
                    lineas.add(filas.getString("fechaRevision") + " - " + filas.getString("motivoRevision")
                            + " -> " + filas.getString("diagnostico"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al leer las revisiones: " + e.getMessage());
        }

        return lineas;
    }

    // CORREGIDO: mismos nombres de columna que en CrearTablas. Se mantiene
    // el filtro por SQL directamente: si soloActuales es true, solo trae
    // filas con fechaFin todavía no pasada, o sin fecha fin (crónicos).
    // NUEVO: como obtenerLineasTratamientos, pero devuelve también el "id"
    // de cada tratamiento (no solo el texto ya formateado). Lo necesitamos
    // para poder editar UNO en concreto más tarde — sin el id no sabríamos
    // cuál actualizar en la base de datos.
    static ArrayList<String[]> obtenerTratamientosConId(int idMascota) {
        ArrayList<String[]> lista = new ArrayList<>();
        String sql = "SELECT id, nombreMedicamento, dosis, frecuencia, fechaInicio, fechaFin "
                + "FROM tratamientos WHERE mascota_id = ?";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idMascota);

            try (ResultSet filas = sentencia.executeQuery()) {
                while (filas.next()) {
                    String fechaFin = filas.getString("fechaFin");
                    lista.add(new String[]{
                            filas.getString("id"),
                            filas.getString("nombreMedicamento"),
                            filas.getString("dosis"),
                            filas.getString("frecuencia"),
                            filas.getString("fechaInicio"),
                            (fechaFin == null) ? "" : fechaFin
                    });
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al leer los tratamientos: " + e.getMessage());
        }

        return lista;
    }

    // ================== OBTENER CON ID (para edición) ==================

    static ArrayList<String[]> obtenerVacunasConId(int idMascota) {
        ArrayList<String[]> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, fechaAplicacion, fechaProximaDosis, veterinario, lote "
                + "FROM vacunas WHERE mascota_id = ?";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idMascota);

            try (ResultSet filas = sentencia.executeQuery()) {
                while (filas.next()) {
                    lista.add(new String[]{
                            filas.getString("id"),
                            filas.getString("nombre"),
                            filas.getString("fechaAplicacion"),
                            filas.getString("fechaProximaDosis"),
                            filas.getString("veterinario"),
                            filas.getString("lote")
                    });
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al leer las vacunas: " + e.getMessage());
        }

        return lista;
    }

    static ArrayList<String[]> obtenerRevisionesConId(int idMascota) {
        ArrayList<String[]> lista = new ArrayList<>();
        String sql = "SELECT id, fechaRevision, motivoRevision, diagnostico, notas, veterinario "
                + "FROM revision WHERE mascota_id = ?";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idMascota);

            try (ResultSet filas = sentencia.executeQuery()) {
                while (filas.next()) {
                    lista.add(new String[]{
                            filas.getString("id"),
                            filas.getString("fechaRevision"),
                            filas.getString("motivoRevision"),
                            filas.getString("diagnostico"),
                            filas.getString("notas"),
                            filas.getString("veterinario")
                    });
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al leer las revisiones: " + e.getMessage());
        }

        return lista;
    }

    static ArrayList<String[]> obtenerPesosConId(int idMascota) {
        ArrayList<String[]> lista = new ArrayList<>();
        String sql = "SELECT id, fecha, peso, notas FROM pesos WHERE mascota_id = ?";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idMascota);

            try (ResultSet filas = sentencia.executeQuery()) {
                while (filas.next()) {
                    lista.add(new String[]{
                            filas.getString("id"),
                            filas.getString("fecha"),
                            filas.getString("peso"),
                            filas.getString("notas")
                    });
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al leer los pesos: " + e.getMessage());
        }

        return lista;
    }

    // NUEVO: cambia SOLO la fecha fin de un tratamiento ya existente.
    // nuevaFechaFin puede ser null (para dejarlo como crónico/indefinido).
    // NUEVO: actualiza TODOS los campos de un tratamiento existente
    // (medicamento, dosis, frecuencia, ambas fechas). fechaFin puede
    // venir null si se deja como crónico/indefinido.
    static void actualizarTratamiento(int idTratamiento, String nombreMedicamento, String dosis,
                                        String frecuencia, java.time.LocalDate fechaInicio,
                                        java.time.LocalDate fechaFin) {
        String sql = "UPDATE tratamientos SET nombreMedicamento = ?, dosis = ?, frecuencia = ?, "
                + "fechaInicio = ?, fechaFin = ? WHERE id = ?";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, nombreMedicamento);
            sentencia.setString(2, dosis);
            sentencia.setString(3, frecuencia);
            sentencia.setString(4, fechaInicio.toString());

            if (fechaFin != null) {
                sentencia.setString(5, fechaFin.toString());
            } else {
                sentencia.setNull(5, java.sql.Types.VARCHAR);
            }

            sentencia.setInt(6, idTratamiento);

            sentencia.executeUpdate();
            System.out.println("Tratamiento actualizado.");

        } catch (SQLException e) {
            System.out.println("Error al actualizar el tratamiento: " + e.getMessage());
        }
    }

    static void actualizarVacuna(int idVacuna, String nombre, java.time.LocalDate fechaAplicacion,
                                  java.time.LocalDate fechaProximaDosis, String veterinario, String lote) {
        String sql = "UPDATE vacunas SET nombre = ?, fechaAplicacion = ?, fechaProximaDosis = ?, "
                + "veterinario = ?, lote = ? WHERE id = ?";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, nombre);
            sentencia.setString(2, fechaAplicacion.toString());
            sentencia.setString(3, fechaProximaDosis.toString());
            sentencia.setString(4, veterinario);
            sentencia.setString(5, lote);
            sentencia.setInt(6, idVacuna);

            sentencia.executeUpdate();
            System.out.println("Vacuna actualizada.");

        } catch (SQLException e) {
            System.out.println("Error al actualizar la vacuna: " + e.getMessage());
        }
    }

    static void actualizarRevision(int idRevision, java.time.LocalDate fecha, String motivo,
                                    String diagnostico, String notas, String veterinario) {
        String sql = "UPDATE revision SET fechaRevision = ?, motivoRevision = ?, diagnostico = ?, "
                + "notas = ?, veterinario = ? WHERE id = ?";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, fecha.toString());
            sentencia.setString(2, motivo);
            sentencia.setString(3, diagnostico);
            sentencia.setString(4, notas);
            sentencia.setString(5, veterinario);
            sentencia.setInt(6, idRevision);

            sentencia.executeUpdate();
            System.out.println("Revisión actualizada.");

        } catch (SQLException e) {
            System.out.println("Error al actualizar la revisión: " + e.getMessage());
        }
    }

    static void actualizarPeso(int idPeso, java.time.LocalDate fecha, double peso, String notas) {
        String sql = "UPDATE pesos SET fecha = ?, peso = ?, notas = ? WHERE id = ?";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, fecha.toString());
            sentencia.setDouble(2, peso);
            sentencia.setString(3, notas);
            sentencia.setInt(4, idPeso);

            sentencia.executeUpdate();
            System.out.println("Registro de peso actualizado.");

        } catch (SQLException e) {
            System.out.println("Error al actualizar el peso: " + e.getMessage());
        }
    }

    static void actualizarFechaFinTratamiento(int idTratamiento, java.time.LocalDate nuevaFechaFin) {
        String sql = "UPDATE tratamientos SET fechaFin = ? WHERE id = ?";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            if (nuevaFechaFin != null) {
                sentencia.setString(1, nuevaFechaFin.toString());
            } else {
                sentencia.setNull(1, java.sql.Types.VARCHAR);
            }
            sentencia.setInt(2, idTratamiento);

            sentencia.executeUpdate();
            System.out.println("Fecha fin del tratamiento actualizada.");

        } catch (SQLException e) {
            System.out.println("Error al actualizar el tratamiento: " + e.getMessage());
        }
    }

    static ArrayList<String> obtenerLineasTratamientos(int idMascota, boolean soloActuales) {
        ArrayList<String> lista = new ArrayList<>();

        String sql = "SELECT nombreMedicamento, dosis, frecuencia, fechaInicio, fechaFin "
                + "FROM tratamientos WHERE mascota_id = ?";
        if (soloActuales) {
            sql += " AND (fechaFin IS NULL OR fechaFin >= DATE('now'))";
        }

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idMascota);

            try (ResultSet rs = sentencia.executeQuery()) {
                while (rs.next()) {
                    String med = rs.getString("nombreMedicamento");
                    String dosis = rs.getString("dosis");
                    String freq = rs.getString("frecuencia");
                    String fInicio = rs.getString("fechaInicio");
                    String fFin = rs.getString("fechaFin");

                    String textoFin = (fFin == null || fFin.isEmpty()) ? "Indefinido/Crónico" : fFin;

                    lista.add(med + " (" + dosis + ", " + freq + ") - Desde: " + fInicio + " Hasta: " + textoFin);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al obtener tratamientos: " + e.getMessage());
        }

        return lista;
    }

    static ArrayList<String> obtenerLineasPesos(int idMascota) {
        ArrayList<String> lineas = new ArrayList<>();
        String sql = "SELECT * FROM pesos WHERE mascota_id = ?";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idMascota);

            try (ResultSet filas = sentencia.executeQuery()) {
                while (filas.next()) {
                    lineas.add(filas.getString("fecha") + ": " + filas.getDouble("peso")
                            + " kg (" + filas.getString("notas") + ")");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al leer los pesos: " + e.getMessage());
        }

        return lineas;
    }

    // ================== LEER (para consola) ==================

    static void mostrarTodasLasMascotas() {
        String sql = "SELECT * FROM mascotas";

        try (Connection conexion = abrirConexion();
             Statement sentencia = conexion.createStatement();
             ResultSet filas = sentencia.executeQuery(sql)) {

            boolean hayMascotas = false;

            while (filas.next()) {
                hayMascotas = true;

                int id = filas.getInt("id");
                String nombre = filas.getString("nombre");
                String especie = filas.getString("especie");
                String raza = filas.getString("raza");
                String microchip = filas.getString("microchip");

                System.out.println("\n===== FICHA DE " + nombre.toUpperCase() + " =====");
                System.out.println("Especie: " + especie + " | Raza: " + raza);
                System.out.println("Microchip: " + (microchip == null || microchip.isEmpty() ? "(sin microchip)" : microchip));

                mostrarVacunasDe(id);
                mostrarRevisionesDe(id);
                mostrarTratamientosDe(id);
                mostrarPesosDe(id);
            }

            if (!hayMascotas) {
                System.out.println("Todavía no hay ninguna mascota guardada.");
            }

        } catch (SQLException e) {
            System.out.println("Error al leer las mascotas: " + e.getMessage());
        }
    }

    static void mostrarVacunasDe(int idMascota) {
        System.out.println("Vacunas:");
        ArrayList<String> lineas = obtenerLineasVacunas(idMascota);
        if (lineas.isEmpty()) {
            System.out.println("- (sin vacunas registradas)");
        } else {
            for (String linea : lineas) System.out.println("- " + linea);
        }
    }

    static void mostrarRevisionesDe(int idMascota) {
        System.out.println("Revisiones:");
        ArrayList<String> lineas = obtenerLineasRevisiones(idMascota);
        if (lineas.isEmpty()) {
            System.out.println("- (sin revisiones registradas)");
        } else {
            for (String linea : lineas) System.out.println("- " + linea);
        }
    }

    static void mostrarTratamientosDe(int idMascota) {
        System.out.println("Tratamientos:");
        ArrayList<String> lineas = obtenerLineasTratamientos(idMascota, false);
        if (lineas.isEmpty()) {
            System.out.println("- (sin tratamientos registrados)");
        } else {
            for (String linea : lineas) System.out.println("- " + linea);
        }
    }

    static void mostrarPesosDe(int idMascota) {
        System.out.println("Historial de peso:");
        ArrayList<String> lineas = obtenerLineasPesos(idMascota);
        if (lineas.isEmpty()) {
            System.out.println("- (sin registros de peso)");
        } else {
            for (String linea : lineas) System.out.println("- " + linea);
        }
    }
}