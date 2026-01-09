androidApplication {
    namespace = "org.example.app"

    dependencies {
        implementation("org.apache.commons:commons-text:1.11.0")
        implementation(project(":utilities"))

        // UI support libraries (no Compose)
        implementation("androidx.core:core-ktx:1.13.1")
        implementation("androidx.appcompat:appcompat:1.7.0")
        implementation("com.google.android.material:material:1.12.0")
        implementation("androidx.gridlayout:gridlayout:1.0.0")

        // Unit tests (JUnit4)
        implementation("junit:junit:4.13.2")
    }
}
