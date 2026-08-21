import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UtilidadesFecha {

    private static final DateTimeFormatter FORMATO_ES = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Convierte una fecha en formato ISO (yyyy-MM-dd) a formato español (dd/MM/yyyy).
     * Si la fecha es null o vacía, devuelve la misma cadena.
     * Si hay error de parseo, devuelve la cadena original.
     */
    public static String formatearFechaES(String fechaISO) {
        if (fechaISO == null || fechaISO.isEmpty()) {
            return fechaISO;
        }

        try {
            LocalDate fecha = LocalDate.parse(fechaISO, FORMATO_ISO);
            return fecha.format(FORMATO_ES);
        } catch (Exception e) {
            return fechaISO;
        }
    }
}
