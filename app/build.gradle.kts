// Archivo: build.gradle.kts(app) (CORREGIDO)

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.example.ritmofit"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.ritmofit"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnit4"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        // La versión del compilador de Compose ahora se maneja con el plugin
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // --- DEFINICIÓN DE VERSIONES ---
    val coroutinesVersion = "1.8.1"
    val dataStoreVersion = "1.0.0"
    val lifecycleVersion = "2.8.3"
    val activityComposeVersion = "1.9.0"

    // --- PLATAFORMA DE COMPOSE (BOM) ---
    // Define las versiones para todas las librerías Compose (incluyendo Material3)
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))


    // --- DEPENDENCIAS DE COMPOSE Y MATERIAL 3 ---
    // ❌ ELIMINADA: Esta línea causaba el error de resolución. El contenido está en 'material3'.
    // implementation("androidx.compose.material3:material3-experimental")
// build.gradle (app)

        // ...
    implementation("androidx.compose.material3:material3")
        // Asegúrate de tener también la de íconos
    implementation("androidx.compose.material:material-icons-extended")
    
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Versiones eliminadas para que usen la BOM
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")


    // --- DEPENDENCIAS CLAVE DEL PROYECTO ---

    // Dependencia de ThreeTenABP
    implementation("com.jakewharton.threetenabp:threetenabp:1.4.0")

    // Corrutinas (Versión 1.8.1)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    // DataStore
    implementation("androidx.datastore:datastore-core:$dataStoreVersion")
    implementation("androidx.datastore:datastore-preferences:$dataStoreVersion")

    // Retrofit y Serialización
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Varios
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Dependencias por defecto de Android KTX, Lifecycle y Activity
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
    implementation("androidx.activity:activity-compose:$activityComposeVersion")


    // --- DEPENDENCIAS PARA LOS TESTS ---
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}