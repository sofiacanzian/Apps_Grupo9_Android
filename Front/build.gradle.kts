// Archivo: build.gradle.kts (ritmo fit)
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // CORRECCIÓN 1: Actualizar la versión del plugin de serialización a 1.9.0 o superior
    // para que coincida con la versión de Kotlin.
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0" apply false

    // CORRECCIÓN 2: Asegúrate de usar la misma versión de Kotlin para todos los plugins
    // Si tus librerías son 1.9.0, usa 1.9.0 aquí. (Dejamos 1.9.0 como estaba).
    id("org.jetbrains.kotlin.jvm") version "1.9.0" apply false
}