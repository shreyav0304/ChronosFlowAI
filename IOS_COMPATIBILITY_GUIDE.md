# ChronosFlow AI — iOS & Multiplatform Compatibility Blueprint

This guide details how **ChronosFlow AI** is fundamentally designed for full cross-platform compatibility, and shows how you can migrate and compile this exact codebase to run natively on **iOS**, **Android**, **Desktop**, and **Web** using **Compose Multiplatform** (developed by JetBrains) and **Kotlin Multiplatform (KMP)**.

---

## 1. Multiplatform Architecture overview

Because ChronosFlow AI utilizes Clean Architecture (separation of UI, Domain, and Data), the transition of code to a multi-platform compilation scheme is seamless:

```
                  ┌───────────────────────┐
                  │      :commonMain      │
                  ├───────────────────────┤
                  │  - Custom Solver      │ (100% Platform Agnostic Kotlin)
                  │  - Repositories/Room  │ (Room KMP multiplatform database)
                  │  - Core ViewModels    │ (Kotlin coroutines & Flow state)
                  │  - Jetpack Compose UI │ (Shared via Compose Multiplatform)
                  └───────────┬───────────┘
                              │
             ┌────────────────┴────────────────┐
             ▼                                 ▼
   ┌───────────────────┐             ┌───────────────────┐
   │    :androidMain   │             │      :iosMain     │
   ├───────────────────┤             ├───────────────────┤
   │  Android Launcher │             │  UIKit App Entry  │
   │  Activity / Theme │             │  Compose UI Host  │
   └───────────────────┘             └───────────────────┘
```

### Shared vs Platform-Specific Components
- **Scheduling Constraint Solver Engine (`ScheduleEngine.kt`)**: Implemented in **100% pure Kotlin**. It contains zero dependencies on `android.*` SDK libraries, allowing direct platform-independent compilation via Kotlin/Native for iOS.
- **Database Persistence (`Room`)**: Jetpack Room natively supports Kotlin Multiplatform. The `@Entity` definitions translate perfectly across platforms using SQLite.
- **State Management & Flows (`MainViewModel.kt` & view states)**: ViewModels are written with standard Kotlin Coroutines and Flow mechanics, allowing shared reactive streams on iOS.
- **Jetpack Compose UI Layouts**: Compose Multiplatform allows you to compile 100% of the screen composables in this project to run as a native Swift UIView (wrapper) on iOS devices.

---

## 2. Setting Up the Multiplatform Project Structure

To target iOS alongside Android, restructure your projects directories as follows:

```
├── composeApp
│   ├── build.gradle.kts      # Shared multiplatform build configuration
│   └── src
│       ├── androidMain/       # Android target wrapper & entry points
│       ├── commonMain/        # Shared code: Copy YOUR UI SCREENS, VIEW MODELS, and ENGINEs HERE!
│       └── iosMain/           # iOS target wrapper & application hosting view controller
├── iosApp
│   └── iosApp.xcodeproj      # Xcode configuration containing native iOS project
```

### Step A: Declare Kotlin Multiplatform in your `build.gradle.kts`

In `composeApp/build.gradle.kts`, enable target presets for both Android and iOS:

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.roomMultiplatform)
}

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(libs.room.runtime)
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.kotlinx.coroutines.android)
        }
        iosMain.dependencies {
            // Native platform-specific configurations (if any)
        }
    }
}
```

---

## 3. iOS Entry Point Strategy (Compose in Swift)

To render the Compose multiplatform screens natively on iOS devices:

### Step A: Expose Compose Screen in `src/iosMain/kotlin/main.ios.kt`

```kotlin
import androidx.compose.ui.window.ComposeUIViewController
import com.example.ui.screens.LoginScreen
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    // Simply render the App Composable directly inside iOS UIKit Host Window!
    AppTheme {
        MainAppContent()
    }
}
```

### Step B: Host in Xcode Native SwifUI View (`iosApp/iosApp/ContentView.swift`)

Compile the Kotlin code to a native Framework, and hook it up directly inside the Xcode project:

```swift
import SwiftUI
import ComposeApp // Import compile-output Framework from Kotlin Multiplatform

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        Main_iosKt.MainViewController() // Expose the ComposeUIViewController natively!
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard) // Standard keyboard handling for iOS text input fields
    }
}
```

---

## 4. Multiplatform Database Persistence (Room KMP)

Our database declarations are ready for KMP with compile-time generation. In standard multiplatform applications, database instantiatons use `expect`/`actual` declarations allowing native SQLite engines:

```kotlin
// In commonMain (expect class Room Database builder):
expect class DatabaseBuilder {
    fun getBuilder(): RoomDatabase.Builder<ScheduleDatabase>
}

// In androidMain (actual class):
actual class DatabaseBuilder(private val context: Context) {
    actual fun getBuilder(): RoomDatabase.Builder<ScheduleDatabase> {
        val dbFile = context.getDatabasePath("schedule.db")
        return Room.databaseBuilder<ScheduleDatabase>(
            context = context,
            name = dbFile.absolutePath
        )
    }
}

// In iosMain (actual class utilizing native directory layouts):
actual class DatabaseBuilder {
    actual fun getBuilder(): RoomDatabase.Builder<ScheduleDatabase> {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
        return Room.databaseBuilder<ScheduleDatabase>(
            name = documentDirectory?.path + "/schedule.db"
        )
    }
}
```

---

## 5. UI and UX Design Optimizations for iOS Devices

To ensure ChronosFlow AI feels fully native on iOS:
1. **Dynamic Edge-to-Edge Insets**: Our usage of Compose `safeDrawing` or `.windowInsetsPadding()` handles both the Android Camera Punch Hole/Notch and the premium **iOS Dynamic Island** and **Home Indicator pill** automatically.
2. **Smooth Spring Animations**: The timing variables have been structured to match iOS's core animation physics framework.
3. **Typography Scaling**: Screen typography loads scalable SP tokens that match iOS Dynamic Type accessibility constraints without visual truncation.
