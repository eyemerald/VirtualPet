# VirtuaPet 🐾

Aplicación de consola en Java para llevar la cartilla veterinaria digital de una mascota: datos básicos, vacunas, revisiones, tratamientos e historial de peso, con exportación a PDF. Proyecto personal de aprendizaje, construido paso a paso como vehículo para aprender Java y bases de datos relacionales, con vistas a una futura versión de escritorio (JavaFX) y Android.

## Tecnologías

- **Java 21** (Eclipse Temurin JDK), IntelliJ IDEA
- **SQLite** (vía driver JDBC `sqlite-jdbc`) como base de datos persistente
- **Apache PDFBox** para la generación de fichas en PDF
- SQL con `PreparedStatement` (consultas parametrizadas, evitando inyección SQL)

## Estado actual

Funcional en consola, con menú interactivo:

- ✅ Alta de mascotas
- ✅ Alta de vacunas, revisiones, tratamientos y registros de peso, cada uno asociado a una mascota
- ✅ Listado de todas las mascotas guardadas, con su historial completo
- ✅ Cálculo automático de edad a partir de la fecha de nacimiento
- ✅ Selección de mascota por lista numerada (sin necesidad de recordar el microchip)
- ✅ Exportación de la ficha completa a PDF, con filtro configurable: los tratamientos ya finalizados pueden excluirse o incluirse a elección del usuario
- 🔲 Catálogos de razas y vacunas con opción de añadir nuevas (pendiente de interfaz gráfica)
- 🔲 Interfaz gráfica (JavaFX) / versión Android
- 🔲 Exportación e importación manual de copia de seguridad (offline-first, sin sincronización en la nube)

## Estructura del proyecto

```
src/
├── Main.java                Menú interactivo (punto de entrada)
├── CrearTablas.java          Crea las tablas de la base de datos si no existen
├── GestorMascotas.java       Toda la lógica de acceso a datos (INSERT / SELECT)
├── ExportadorMascota.java    Genera la ficha de una mascota en PDF
├── Mascota.java               Clase de dominio: datos de una mascota
├── Vacuna.java                 Clase de dominio: una vacuna
├── Revision.java               Clase de dominio: una revisión veterinaria
├── Tratamiento.java            Clase de dominio: un tratamiento médico
└── RegistroPeso.java           Clase de dominio: un registro de peso
```

### Cómo se relacionan las piezas

El proyecto sigue una separación de responsabilidades en capas:

1. **`Main`** — interactúa con el usuario (menú, `Scanner`). No sabe nada de SQL.
2. **Clases de dominio** (`Mascota`, `Vacuna`...) — solo describen los datos, sin lógica de negocio ni de persistencia.
3. **`GestorMascotas`** — es el único punto del programa que habla SQL. Traduce objetos Java a filas de base de datos y viceversa, tanto para mostrar por consola como para alimentar el PDF.
4. **`ExportadorMascota`** — coge los datos ya formateados que le da `GestorMascotas` y construye el documento PDF con PDFBox. No ejecuta ninguna consulta SQL directamente.

Cada capa solo se comunica con la de al lado.

### Modelo de datos

La base de datos (`virtuapet.db`, se genera automáticamente al ejecutar el programa) tiene cinco tablas:

- **`mascotas`** — datos básicos de cada mascota. El `microchip` es único por mascota.
- **`vacunas`**, **`revision`**, **`tratamientos`**, **`pesos`** — cada una relacionada con su mascota mediante la columna `mascota_microchip` (relación uno-a-muchos).

### Lógica destacada: filtro de tratamientos en el PDF

Al exportar, las vacunas y revisiones se incluyen siempre completas. Los tratamientos, en cambio, se filtran por defecto para no mostrar medicación ya finalizada (comparando `fechaFin` con la fecha actual mediante `LocalDate.isBefore()`), aunque el usuario puede elegir incluirlos igualmente.

## Cómo ejecutarlo

1. Clonar el repositorio
2. Abrir con IntelliJ IDEA
3. Añadir como librerías del proyecto (`File → Project Structure → Libraries`) los `.jar` de `sqlite-jdbc` y `pdfbox-app`
4. Ejecutar `Main.java`

La base de datos (`virtuapet.db`) se crea automáticamente en la carpeta del proyecto la primera vez que se ejecuta.

## Autor

Enrique — proyecto personal desarrollado durante el ciclo DAM (Desarrollo de Aplicaciones Multiplataforma).
