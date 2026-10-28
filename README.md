# Expense Tracker

Android app to keep track of monthly income and expenses.

> 🚧 Work in progress. Personal project.

## Planned features

- Record income and expenses with an amount, category, date and note.
- Monthly summary: income, expenses and balance.
- Category management.
- Statistics per category and month-by-month trends.
- Export and import data (CSV) as a backup.

All data is stored **on the device only**: no accounts, no servers.

## Tech stack

- [Kotlin](https://kotlinlang.org/)
- [Jetpack Compose](https://developer.android.com/compose) + Material 3
- [Room](https://developer.android.com/training/data-storage/room) (SQLite) for the local database
- MVVM architecture with Coroutines and Flow

## Requirements

- Android 8.0 (API 26) or higher.
- To build: Android Studio (it ships with the required JDK).

## Build and run

1. Clone the repository:
   ```bash
   git clone https://github.com/davidragus-dev/ExpenseTracker.git
   ```
2. Open it in Android Studio and wait for the Gradle sync to finish.
3. Connect a phone with USB debugging enabled (or use an emulator) and press **Run**.

From the command line:

```bash
./gradlew assembleDebug   # build the debug APK
./gradlew test            # run the unit tests
```

If Gradle cannot find Java, point `JAVA_HOME` to the JDK bundled with Android Studio
(for example `/opt/android-studio/jbr` on Linux).

## License

Distributed under the MIT License. See [LICENSE](LICENSE).
