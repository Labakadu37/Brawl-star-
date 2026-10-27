plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// The JS mod built by ../mod (npm run build) is shipped inside the APK.
val bundleFile = rootProject.file("../mod/dist/vercors.js")
val generatedAssets = layout.buildDirectory.dir("generated/vercorsAssets")

val copyBundle by tasks.registering(Copy::class) {
    doFirst {
        check(bundleFile.exists()) { "Missing ${bundleFile.path}: run `npm install && npm run build` in discord-client/mod first" }
    }
    from(bundleFile)
    into(generatedAssets)
}

android {
    namespace = "dev.vercors.xposed"
    compileSdk = 34

    defaultConfig {
        applicationId = "dev.vercors.xposed"
        minSdk = 24
        targetSdk = 34
        versionCode = 15
        versionName = "0.5.4"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Debug-signed so the APK installs as-is; Xposed doesn't care about the key.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    sourceSets["main"].assets.srcDir(generatedAssets)

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

tasks.named("preBuild") { dependsOn(copyBundle) }

dependencies {
    compileOnly("de.robv.android.xposed:api:82")
}
