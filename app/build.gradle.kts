plugins {
    id("com.android.application")

    id("com.google.gms.google-services")
}

android {
    namespace = "com.saaweel.healthcheckai"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.saaweel.healthcheckai"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))

    implementation("com.google.firebase:firebase-analytics")

    implementation("com.google.firebase:firebase-firestore:25.0.0")

    implementation("com.google.firebase:firebase-storage:20.0.0")

    implementation("de.hdodenhof:circleimageview:3.1.0")

    implementation("com.squareup.picasso:picasso:2.8")

    implementation("com.theokanning.openai-gpt3-java:service:0.11.1")
}