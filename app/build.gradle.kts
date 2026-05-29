plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") version "1.9.22" // O la versión que soporte tu IDE
}

android {
    namespace = "com.cletaeats"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.cletaeats"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "API_BASE_URL", "\"https://cletaeatsbe-production.up.railway.app/\"")
    }

    compileOptions {
        // Sincronizado con tu Backend Java 21
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        // Solución al problema de jvmTarget
        jvmTarget = "21"
    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }

    composeOptions {
        // Asegúrate de que esta versión sea compatible con tu versión de Kotlin (1.9.22 -> 1.5.8 aprox)
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    // Configuración portable de Java (Toolchains)
    // java {
    //     toolchain {
    //         languageVersion.set(JavaLanguageVersion.of(21))
    //     }
    // }

    // kotlin {
    //     jvmToolchain(21)
    // }
}

dependencies {
    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    
    // Legacy support for XML layouts
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    // Icons
    implementation("androidx.compose.material:material-icons-extended")

    // Retrofit (Para conectar con tus Servlets Java)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // OpenStreetMap (osmdroid)
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // WorkManager (sync en background)
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:core:1.6.1")

    androidTestImplementation("androidx.test:runner:1.6.1")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

}