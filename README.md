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

Copyright (C) 2026 David Ruiz

This program is free software: you can redistribute it and/or modify it under the terms of the
GNU General Public License as published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
[GNU General Public License](LICENSE) for more details.
