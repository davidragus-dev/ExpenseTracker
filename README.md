# Expense Tracker

App Android para llevar un registro de los ingresos y gastos mensuales.

> 🚧 En desarrollo. Proyecto personal.

## Funcionalidades previstas

- Registrar ingresos y gastos con importe, categoría, fecha y nota.
- Resumen de cada mes: ingresos, gastos y balance.
- Gestión de categorías.
- Estadísticas por categoría y evolución mensual.
- Exportar e importar los datos (CSV) como copia de seguridad.

Los datos se guardan **solo en el móvil**: sin cuentas ni servidores.

## Tecnologías

- [Kotlin](https://kotlinlang.org/)
- [Jetpack Compose](https://developer.android.com/compose) + Material 3
- [Room](https://developer.android.com/training/data-storage/room) (SQLite) para la base de datos local
- Arquitectura MVVM con Coroutines y Flow

## Requisitos

- Android 8.0 (API 26) o superior.
- Para compilar: Android Studio (incluye el JDK necesario).

## Compilar y ejecutar

1. Clona el repositorio:
   ```bash
   git clone https://github.com/davidragus-dev/ExpenseTracker.git
   ```
2. Ábrelo con Android Studio y espera a que termine la sincronización de Gradle.
3. Conecta un móvil con la depuración USB activada (o usa un emulador) y pulsa **Run**.

Desde la terminal:

```bash
./gradlew assembleDebug   # genera el APK de depuración
./gradlew test            # ejecuta los tests unitarios
```

## Licencia

Distribuido bajo la licencia MIT. Consulta [LICENSE](LICENSE).
