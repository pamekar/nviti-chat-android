plugins {
    id("com.android.library")
    kotlin("android")
    id("maven-publish")
}

group = providers.gradleProperty("GROUP").get()
version = providers.gradleProperty("VERSION_NAME").get()

android {
    namespace = "ng.nviti.chat"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures { buildConfig = false }
    publishing { singleVariant("release") { withSourcesJar() } }
    testOptions { unitTests.isReturnDefaultValues = true }
}

dependencies {
    implementation("androidx.webkit:webkit:1.12.1")
    testImplementation(kotlin("test"))
    testImplementation("junit:junit:4.13.2")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                artifactId = "nviti-chat"
                pom {
                    name.set("Nviti Chat Android SDK")
                    description.set("Secure Android WebView SDK for Nviti conversations")
                    url.set("https://github.com/pamekar/nviti-chat-android")
                    licenses { license { name.set("Apache-2.0"); url.set("https://www.apache.org/licenses/LICENSE-2.0") } }
                }
            }
        }
    }
}
