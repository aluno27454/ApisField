plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "pt.uptomar.dam2026.apisfield"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "pt.uptomar.dam2026.apisfield"
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        // Endereço da API. Em debug é o PC de desenvolvimento visto a partir do emulador
        // (10.0.2.2 é o "localhost" do computador); em release é o servidor no Azure.
        debug {
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:5000/\"")
        }
        release {
            buildConfigField("String", "API_BASE_URL", "\"https://apisfield-api.azurewebsites.net/\"")
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    lint {
        // O CI falha se aparecerem erros de lint; os avisos ficam no relatório.
        abortOnError = true
        warningsAsErrors = false
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
