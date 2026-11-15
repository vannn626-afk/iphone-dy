import java.util.Base64

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.google.services) apply false
}

// Automatically restore debug.keystore from debug.keystore.base64 on fresh GitHub clones
// where debug.keystore is excluded by .gitignore. Does not touch debug.keystore if it already exists.
val debugKeystoreFile = rootProject.file("debug.keystore")
val debugKeystoreBase64File = rootProject.file("debug.keystore.base64")
if (!debugKeystoreFile.exists() && debugKeystoreBase64File.exists()) {
  runCatching {
    val cleanBase64 = debugKeystoreBase64File.readText().replace("\\s".toRegex(), "")
    val decodedBytes = Base64.getDecoder().decode(cleanBase64)
    debugKeystoreFile.writeBytes(decodedBytes)
  }
}
