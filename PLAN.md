# Plan: Mikamentos - Alarma de Medicamentos

## Resumen
Aplicación Android para personas mayores que gestiona alarmas de medicamentos con TTS, múltiples idiomas y almacenamiento JSON local.

## Arquitectura
- **UI**: Jetpack Compose + Material3
- **DI**: Hilt
- **Almacenamiento**: Gson + SharedPreferences (JSON local)
- **Alarms**: AlarmManager + BroadcastReceiver
- **TTS**: Android TextToSpeech API
- **Navigation**: Navigation Compose

## Estructura del Proyecto
```
C:\Proyectos_IA\Proyectos\Mikatementos\
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/mikamentos/app/
│       │   ├── MikamentosApp.kt (Application class)
│       │   ├── MainActivity.kt
│       │   ├── data/
│       │   │   ├── model/
│       │   │   │   ├── Medication.kt
│       │   │   │   └── UserSettings.kt
│       │   │   └── repository/
│       │   │       └── MedicationRepository.kt
│       │   ├── di/
│       │   │   └── AppModule.kt
│       │   ├── service/
│       │   │   ├── AlarmScheduler.kt
│       │   │   ├── AlarmReceiver.kt
│       │   │   ├── AlarmService.kt
│       │   │   ├── BootReceiver.kt
│       │   │   └── TtsManager.kt
│       │   ├── ui/
│       │   │   ├── theme/
│       │   │   │   ├── Color.kt
│       │   │   │   ├── Theme.kt
│       │   │   │   └── Type.kt
│       │   │   ├── navigation/
│       │   │   │   └── NavGraph.kt
│       │   │   ├── home/
│       │   │   │   ├── HomeScreen.kt
│       │   │   │   └── HomeViewModel.kt
│       │   │   ├── editor/
│       │   │   │   ├── MedicationEditorScreen.kt
│       │   │   │   └── MedicationEditorViewModel.kt
│       │   │   └── settings/
│       │   │       ├── SettingsScreen.kt
│       │   │       └── SettingsViewModel.kt
│       │   └── util/
│       │       └── Extensions.kt
│       └── res/
│           ├── drawable/
│           ├── mipmap-*/
│           └── values/
│               ├── strings.xml
│               ├── strings-es.xml
│               ├── strings-en.xml
│               ├── strings-sk.xml
│               ├── strings-ca.xml
│               ├── strings-fr.xml
│               └── strings-it.xml
├── build.gradle.kts (root)
├── settings.gradle.kts
└── gradle/
    └── libs.versions.toml
```

## Modelos de Datos

### Medication
```kotlin
data class Medication(
    val id: String = UUID.randomUUID().toString(),
    val title: String,           // Nombre del medicamento
    val description: String,     // Para qué sirve
    val hour: Int,               // 0-23
    val minute: Int,             // 0-59
    val daysOfWeek: List<Boolean>, // [Lun, Mar, Mié, Jue, Vie, Sáb, Dom]
    val ringtoneUri: String?,    // URI del tono seleccionado
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
```

### UserSettings
```kotlin
data class UserSettings(
    val language: String = "es",
    val silenceSeconds: Int = 2,      // 1-5 segundos
    val is24HourFormat: Boolean = true,
    val userGender: String = "",       // "Sra.", "Sr.", etc.
    val userName: String = ""          // Nombre del usuario
)
```

## Pantallas

### 1. HomeScreen (Pantalla Principal)
- Lista de medicamentos agrupados por día/hora
- FAB para agregar nuevo medicamento
- Toggle habilitar/deshabilitar cada medicamento
- Botón de configuración (engranaje)
- Font grande para personas mayores

### 2. MedicationEditorScreen (Editor)
- Campo título (teclado + micrófono)
- Campo descripción (teclado + micrófono)
- Selector de hora (TimePicker)
- Selector de días de la semana (7 toggle buttons)
- Selector de tono de alarma
- Botón guardar/cancelar

### 3. SettingsScreen (Configuración)
- Selector de idioma (6 opciones)
- Intervalo de silencio (1-5 segundos, slider)
- Formato de hora (24h/12h, toggle)
- Campo género/título (Sra., Sr., etc.)
- Campo nombre de usuario

## Servicios

### AlarmScheduler
- Programa alarmas exactas con `setExactAndAllowWhileIdle()`
- Calcula próxima ocurrencia basada en días seleccionados
- Maneja permisos de Android 12+

### AlarmReceiver
- Recibe broadcast cuando suena la alarma
- Inicia AlarmService con los medicamentos de esa hora
- Reprograma para la próxima semana

### AlarmService
- Servicio foreground que reproduce tono + TTS
- Dice: "[Género] [Nombre], tiene que tomar: [Medicamento 1], [silencio], [Medicamento 2]..."
- Maneja múltiples medicamentos a la misma hora

### BootReceiver
- Reprograma todas las alarmas después de reiniciar el dispositivo

### TtsManager
- Inicializa TextToSpeech
- Reproduce texto con voz configurada
- Respetar idioma del usuario

## Idiomas Soportados
1. Español (es) - por defecto
2. Inglés (en)
3. Eslovaco (sk)
4. Catalán (ca)
5. Francés (fr)
6. Italiano (it)

## Permisos Necesarios
- `SCHEDULE_EXACT_ALARM` (Android 12+)
- `RECEIVE_BOOT_COMPLETED`
- `WAKE_LOCK`
- `POST_NOTIFICATIONS` (Android 13+)
- `RECORD_AUDIO` (para micrófono)

## Pasos de Implementación

1. **Crear estructura del proyecto** (build.gradle.kts, settings, etc.)
2. **Configurar dependencias** (Compose, Hilt, Gson, Navigation)
3. **Crear modelos de datos** (Medication, UserSettings)
4. **Crear repositorio** (guardar/cargar JSON)
5. **Crear tema** (colores, tipografía grande)
6. **Crear pantallas** (Home, Editor, Settings)
7. **Crear navegación**
8. **Implementar AlarmScheduler + Receiver**
9. **Implementar TtsManager**
10. **Implementar AlarmService**
11. **Crear strings.xml para 6 idiomas**
12. **Configurar AndroidManifest**
13. **Crear icono de la app**

## Icono
- Diseño de pastillero/píldora
- Colores: verde medicinal + blanco
- Forma simple y reconocible

## Notas Importantes
- Font mínimo 16sp para legibilidad
- Contraste alto para personas mayores
- Botones grandes (mínimo 48dp)
- Guardado automático de datos
- Alarmas que funcionan con app cerrada
