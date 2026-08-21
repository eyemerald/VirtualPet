# VirtualPet

Aplicación de gestión de historiales médicos de mascotas, desarrollada en Java con interfaz JavaFX y persistencia local en SQLite.

## Estado actual

Proyecto migrado a Maven, con estructura estándar (`src/main/java`) y build reproducible.

### Funcionalidades implementadas

- **Mascotas**: alta, edición y borrado (con cascada sobre sus registros asociados)
- **Vacunas**: alta, edición y borrado, con aviso automático de vencidas o próximas a caducar
- **Revisiones veterinarias**: alta, edición y borrado
- **Tratamientos**: alta y edición (incluye tratamientos crónicos sin fecha fin)
- **Registros de peso**: alta, edición y borrado
- **Exportación a PDF**: genera ficha completa de la mascota (datos básicos, vacunas, revisiones, tratamientos, historial de peso) usando Apache PDFBox

### Arquitectura

- **Persistencia**: SQLite vía JDBC puro (sin ORM), con claves foráneas y `ON DELETE CASCADE`
- **Capa de datos**: `GestorMascotas.java` centraliza todo el acceso a la base de datos
- **Interfaz**: JavaFX, con ventanas modales para cada operación (`FormularioMascota`, `FormularioRegistro`, `GestionTratamientosVentana`, `GestionVacunasVentana`, `GestionRevisionesVentana`, `GestionPesosVentana`)
- **Logging**: `AppLogger.java` (parcialmente integrado — pendiente unificar con `GestorMascotas`, ver más abajo)
- Offline-first: no requiere conexión a internet ni servidor

## Cómo compilar y ejecutar

```
mvn -DskipTests package
mvn javafx:run
```

O desde IntelliJ: botón ▶️ sobre el método `main` en `VirtualPetApp.java`.

## Pendiente / próximos pasos

- **Exportación a PDF**: actualmente el nombre del archivo se genera solo con el nombre de la mascota, así que cada exportación **sobrescribe** el PDF anterior de esa mascota. Pendiente decidir si se añade fecha/hora al nombre del archivo o se permite elegir ubicación con un `FileChooser`.
- **Desplegable de razas**: sustituir el campo de texto libre de raza por un `ComboBox` con catálogo por especie, para evitar errores de escritura y datos inconsistentes.
- **Desplegable de vacunas**: catálogo de vacunas habituales por especie en vez de campo de texto libre.
- **Limpieza de logging**: unificar todos los `System.out.println` de `GestorMascotas.java` para que usen `AppLogger`, igual que ya hacen `CrearTablas` y `ExportadorMascota`.
- **Backup manual**: importación/exportación de la base de datos para copias de seguridad.
- Ver roadmap completo en `VirtualPet-roadmap.md` (funcionalidades a más largo plazo: freemium, publicidad, etc.)

## Notas de arquitectura para el futuro

Pensado como posible base para una futura app Android/iOS: la capa de datos (`GestorMascotas`) está desacoplada de la interfaz JavaFX, lo que facilita en el futuro sustituir SQLite local por una API si se necesita sincronización entre dispositivos.
