import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GestorMascotas {

    // La URL ya no es fija con ruta relativa: se calcula apuntando a la
    // carpeta de usuario (ver RutasApp.java) — coherente con CrearTablas.
    static String getUrl() {
        return RutasApp.getUrlBaseDatos();
    }

    static Connection abrirConexion() throws SQLException {
        Connection conexion = DriverManager.getConnection(getUrl());
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
            AppLogger.logSevere("Error al leer las mascotas: " + e.getMessage());
        }

        return lista;
    }

    // ================== GUARDAR ==================

    static int guardarMascota(Mascota m) throws Exception {
        // Comprobación de nombre único (case-insensitive)
        String sqlCheck = "SELECT id FROM mascotas WHERE LOWER(nombre) = LOWER(?)";
        try (Connection conexion = abrirConexion();
             PreparedStatement chk = conexion.prepareStatement(sqlCheck)) {
            chk.setString(1, m.getNombre());
            try (ResultSet rs = chk.executeQuery()) {
                if (rs.next()) {
                    throw new Exception("Ya existe una mascota con ese nombre");
                }
            }
        } catch (SQLException e) {
            AppLogger.logSevere("Error comprobando nombre: " + e.getMessage());
            throw new Exception("Error comprobando nombre: " + e.getMessage());
        }

        String sql = "INSERT INTO mascotas (nombre, especie, raza, fechaNacimiento, sexo, color, microchip) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, m.getNombre());
            sentencia.setString(2, m.getEspecie());
            sentencia.setString(3, m.getRaza());
            sentencia.setString(4, m.getFechaNacimiento().toString());
            sentencia.setString(5, m.getSexo());
            sentencia.setString(6, m.getColor());
            sentencia.setString(7, m.getMicrochip());

            sentencia.executeUpdate();

            // Obtener id generado con last_insert_rowid() (compatible con SQLite JDBC)
            try (Statement s2 = conexion.createStatement();
                 ResultSet rs = s2.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    int idNuevo = rs.getInt(1);
                    AppLogger.logInfo("Mascota guardada: " + m.getNombre() + " (id " + idNuevo + ")");
                    // Crear carpeta física para esa mascota
                    try {
                        GestorArchivos.crearCarpetaMascota(idNuevo, m.getNombre());
                    } catch (Exception ex) {
                        AppLogger.logSevere("Aviso: no se pudo crear carpeta de la mascota: " + ex.getMessage());
                    }
                    return idNuevo;
                }
            }

        } catch (SQLException e) {
            AppLogger.logSevere("Error al guardar la mascota: " + e.getMessage());
            throw new Exception("Error al guardar la mascota: " + e.getMessage());
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
            AppLogger.logInfo("Vacuna guardada.");

        } catch (SQLException e) {
            AppLogger.logSevere("Error al guardar la vacuna: " + e.getMessage());
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
            AppLogger.logInfo("Revisión guardada.");

        } catch (SQLException e) {
            AppLogger.logSevere("Error al guardar la revisión: " + e.getMessage());
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
            AppLogger.logInfo("Tratamiento guardado.");

        } catch (SQLException e) {
            AppLogger.logSevere("Error al guardar tratamiento: " + e.getMessage());
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
            AppLogger.logInfo("Registro de peso guardado.");

        } catch (SQLException e) {
            AppLogger.logSevere("Error al guardar el peso: " + e.getMessage());
        }
    }

    // ================== INFORMES (documentos adjuntos) ================

    static void guardarInforme(int idMascota, String tipo, String descripcion, String fecha, File archivoOrigen) {
        // Obtén nombre de la mascota para construir la ruta
        String[] datos = obtenerDatosBasicos(idMascota);
        if (datos == null) {
            AppLogger.logSevere("No se encontró la mascota para guardar el informe.");
            return;
        }
        String nombreMascota = datos[0];

        try {
            // Aseguramos que la carpeta exista
            GestorArchivos.crearCarpetaMascota(idMascota, nombreMascota);

            String safeNombreArchivo = archivoOrigen.getName().replaceAll("[^a-zA-Z0-9-_.]", "_");
            if (safeNombreArchivo.length() > 100) safeNombreArchivo = safeNombreArchivo.substring(0, 100);

            // Si existe, añadimos timestamp para evitar sobrescribir.
            // La carpeta de mascotas ahora vive en la ruta fija de usuario
            // (RutasApp), no en una ruta relativa al directorio de trabajo.
            Path carpeta = RutasApp.getCarpetaMascotas().resolve(idMascota + "_" + GestorArchivos.safeName(nombreMascota));
            if (!Files.exists(carpeta)) Files.createDirectories(carpeta);

            String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
            String nombreDestino = timestamp + "_" + safeNombreArchivo;
            Path destino = carpeta.resolve(nombreDestino);

            Files.copy(archivoOrigen.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);

            // Guardar referencia en BD
            String sql = "INSERT INTO informes (mascota_id, tipo, descripcion, fecha, nombreArchivo, rutaArchivo) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conexion = abrirConexion();
                 PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setInt(1, idMascota);
                ps.setString(2, tipo);
                ps.setString(3, descripcion);
                ps.setString(4, fecha);
                ps.setString(5, nombreDestino);
                ps.setString(6, destino.toString());
                ps.executeUpdate();
            }

            AppLogger.logInfo("Informe guardado: " + destino.toString());

        } catch (IOException | SQLException e) {
            AppLogger.logSevere("Error al guardar el informe: " + e.getMessage());
        }
    }

    static ArrayList<String[]> obtenerInformesConId(int idMascota) {
        ArrayList<String[]> lista = new ArrayList<>();
        String sql = "SELECT id, tipo, descripcion, fecha, nombreArchivo, rutaArchivo FROM informes WHERE mascota_id = ?";

        try (Connection conexion = abrirConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idMascota);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new String[]{
                            rs.getString("id"),
                            rs.getString("tipo"),
                            rs.getString("descripcion"),
                            rs.getString("fecha"),
                            rs.getString("nombreArchivo"),
                            rs.getString("rutaArchivo")
                    });
                }
            }
        } catch (SQLException e) {
            AppLogger.logSevere("Error al leer informes: " + e.getMessage());
        }

        return lista;
    }

    static void borrarInforme(int idInforme) {
        String sqlSelect = "SELECT rutaArchivo FROM informes WHERE id = ?";
        String sqlDelete = "DELETE FROM informes WHERE id = ?";

        try (Connection conexion = abrirConexion();
             PreparedStatement psSelect = conexion.prepareStatement(sqlSelect)) {
            psSelect.setInt(1, idInforme);
            try (ResultSet rs = psSelect.executeQuery()) {
                if (rs.next()) {
                    String ruta = rs.getString("rutaArchivo");
                    // borrar fichero físico si existe
                    if (ruta != null && !ruta.isEmpty()) {
                        try {
                            Path p = Path.of(ruta);
                            if (Files.exists(p)) Files.delete(p);
                        } catch (IOException ex) {
                            AppLogger.logSevere("No se pudo borrar el archivo físico: " + ex.getMessage());
                        }
                    }
                }
            }

            try (PreparedStatement psDelete = conexion.prepareStatement(sqlDelete)) {
                psDelete.setInt(1, idInforme);
                int filas = psDelete.executeUpdate();
                if (filas > 0) AppLogger.logInfo("Informe borrado de la base de datos.");
            }

        } catch (SQLException e) {
            AppLogger.logSevere("Error al borrar informe: " + e.getMessage());
        }
    }

    // ================== ACTUALIZAR ==================

    static void actualizarMascota(int id, Mascota datosNuevos) throws Exception {
        // Comprobación de nombre único (case-insensitive), permitiendo mantener el propio id
        String sqlCheck = "SELECT id FROM mascotas WHERE LOWER(nombre) = LOWER(?)";
        try (Connection conexion = abrirConexion();
             PreparedStatement chk = conexion.prepareStatement(sqlCheck)) {
            chk.setString(1, datosNuevos.getNombre());
            try (ResultSet rs = chk.executeQuery()) {
                if (rs.next()) {
                    int idExistente = rs.getInt("id");
                    if (idExistente != id) {
                        throw new Exception("Ya existe una mascota con ese nombre");
                    }
                }
            }
        } catch (SQLException e) {
            AppLogger.logSevere("Error comprobando nombre: " + e.getMessage());
            throw new Exception("Error comprobando nombre: " + e.getMessage());
        }

        String sql = "UPDATE mascotas SET nombre = ?, especie = ?, raza = ?, "
                + "fechaNacimiento = ?, sexo = ?, color = ?, microchip = ? WHERE id = ?";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            // Obtener nombre antiguo para renombrar carpeta si hace falta
            String[] datosAntiguos = obtenerDatosBasicos(id);
            String nombreAntiguo = (datosAntiguos == null) ? null : datosAntiguos[0];

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
                AppLogger.logInfo("Mascota actualizada correctamente.");
                // Si cambió el nombre, renombrar carpeta
                if (nombreAntiguo != null && !GestorArchivos.safeName(nombreAntiguo).equals(GestorArchivos.safeName(datosNuevos.getNombre()))) {
                    try {
                        GestorArchivos.renombrarCarpetaMascota(id, nombreAntiguo, datosNuevos.getNombre());
                    } catch (Exception ex) {
                        AppLogger.logSevere("Aviso: no se pudo renombrar la carpeta de la mascota: " + ex.getMessage());
                    }
                }
            } else {
                AppLogger.logSevere("No se encontró ninguna mascota con ese id.");
            }

        } catch (SQLException e) {
            AppLogger.logSevere("Error al actualizar la mascota: " + e.getMessage());
            throw new Exception("Error al actualizar la mascota: " + e.getMessage());
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
                AppLogger.logInfo("Mascota y todos sus datos asociados han sido borrados.");
            } else {
                AppLogger.logSevere("No se encontró ninguna mascota con ese id.");
            }

        } catch (SQLException e) {
            AppLogger.logSevere("Error al borrar la mascota: " + e.getMessage());
        }
    }

    static void borrarVacuna(int id) {
        String sql = "DELETE FROM vacunas WHERE id = ?";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, id);
            int filasBorradas = sentencia.executeUpdate();

            if (filasBorradas > 0) {
                AppLogger.logInfo("Vacuna borrada.");
            } else {
                AppLogger.logSevere("No se encontró ninguna vacuna con ese id.");
            }

        } catch (SQLException e) {
            AppLogger.logSevere("Error al borrar la vacuna: " + e.getMessage());
        }
    }

    static void borrarRevision(int id) {
        String sql = "DELETE FROM revision WHERE id = ?";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, id);
            int filasBorradas = sentencia.executeUpdate();

            if (filasBorradas > 0) {
                AppLogger.logInfo("Revisión borrada.");
            } else {
                AppLogger.logSevere("No se encontró ninguna revisión con ese id.");
            }

        } catch (SQLException e) {
            AppLogger.logSevere("Error al borrar la revisión: " + e.getMessage());
        }
    }

    static void borrarPeso(int id) {
        String sql = "DELETE FROM pesos WHERE id = ?";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, id);
            int filasBorradas = sentencia.executeUpdate();

            if (filasBorradas > 0) {
                AppLogger.logInfo("Registro de peso borrado.");
            } else {
                AppLogger.logSevere("No se encontró ningún registro de peso con ese id.");
            }

        } catch (SQLException e) {
            AppLogger.logSevere("Error al borrar el registro de peso: " + e.getMessage());
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
            AppLogger.logSevere("Error al leer los datos de la mascota: " + e.getMessage());
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
            AppLogger.logSevere("Error al leer las vacunas: " + e.getMessage());
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
            AppLogger.logSevere("Error al leer las revisiones: " + e.getMessage());
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
            AppLogger.logSevere("Error al leer los tratamientos: " + e.getMessage());
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
            AppLogger.logSevere("Error al leer las vacunas: " + e.getMessage());
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
            AppLogger.logSevere("Error al leer las revisiones: " + e.getMessage());
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
            AppLogger.logSevere("Error al leer los pesos: " + e.getMessage());
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
            AppLogger.logInfo("Tratamiento actualizado.");

        } catch (SQLException e) {
            AppLogger.logSevere("Error al actualizar el tratamiento: " + e.getMessage());
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
            AppLogger.logInfo("Vacuna actualizada.");

        } catch (SQLException e) {
            AppLogger.logSevere("Error al actualizar la vacuna: " + e.getMessage());
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
            AppLogger.logInfo("Revisión actualizada.");

        } catch (SQLException e) {
            AppLogger.logSevere("Error al actualizar la revisión: " + e.getMessage());
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
            AppLogger.logInfo("Registro de peso actualizado.");

        } catch (SQLException e) {
            AppLogger.logSevere("Error al actualizar el peso: " + e.getMessage());
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
            AppLogger.logInfo("Fecha fin del tratamiento actualizada.");

        } catch (SQLException e) {
            AppLogger.logSevere("Error al actualizar el tratamiento: " + e.getMessage());
        }
    }

    // ================== NOTAS "A TENER EN CUENTA" ==================

    static ArrayList<String[]> obtenerNotasConId(int idMascota) {
        ArrayList<String[]> lista = new ArrayList<>();
        String sql = "SELECT id, texto FROM notas_importantes WHERE mascota_id = ? ORDER BY id DESC";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idMascota);

            try (ResultSet filas = sentencia.executeQuery()) {
                while (filas.next()) {
                    lista.add(new String[]{
                            filas.getString("id"),
                            filas.getString("texto")
                    });
                }
            }

        } catch (SQLException e) {
            AppLogger.logSevere("Error al leer las notas: " + e.getMessage());
        }

        return lista;
    }

    static void guardarNota(int idMascota, String texto) {
        String sql = "INSERT INTO notas_importantes (mascota_id, texto) VALUES (?, ?)";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idMascota);
            sentencia.setString(2, texto);

            sentencia.executeUpdate();
            AppLogger.logInfo("Nota guardada.");

        } catch (SQLException e) {
            AppLogger.logSevere("Error al guardar la nota: " + e.getMessage());
        }
    }

    static void actualizarNota(int idNota, String texto) {
        String sql = "UPDATE notas_importantes SET texto = ? WHERE id = ?";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, texto);
            sentencia.setInt(2, idNota);

            sentencia.executeUpdate();
            AppLogger.logInfo("Nota actualizada.");

        } catch (SQLException e) {
            AppLogger.logSevere("Error al actualizar la nota: " + e.getMessage());
        }
    }

    static void borrarNota(int idNota) {
        String sql = "DELETE FROM notas_importantes WHERE id = ?";

        try (Connection conexion = abrirConexion();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idNota);
            sentencia.executeUpdate();
            AppLogger.logInfo("Nota borrada.");

        } catch (SQLException e) {
            AppLogger.logSevere("Error al borrar la nota: " + e.getMessage());
        }
    }

    // Texto plano para incluir en el PDF exportado
    static ArrayList<String> obtenerLineasNotas(int idMascota) {
        ArrayList<String> lineas = new ArrayList<>();
        for (String[] n : obtenerNotasConId(idMascota)) {
            lineas.add(n[1]);
        }
        return lineas;
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
            AppLogger.logSevere("Error al obtener tratamientos: " + e.getMessage());
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
            AppLogger.logSevere("Error al leer los pesos: " + e.getMessage());
        }

        return lineas;
    }

}