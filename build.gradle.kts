plugins {
  val kotlinVersion = "2.1.21"
  kotlin("jvm") version kotlinVersion
  kotlin("kapt") version kotlinVersion

  application
}

group = "io.foldright"
version = "0.1.0-SNAPSHOT"

repositories { mavenCentral() }

dependencies {
  implementation("com.github.javaparser:javaparser-core:3.27.0")

  val picocliVersion = "4.7.7"
  implementation("info.picocli:picocli:$picocliVersion")
  kapt("info.picocli:picocli-codegen:$picocliVersion")

  testImplementation(kotlin("test"))
  compileOnly("org.jetbrains:annotations:26.0.2")
}

kotlin { jvmToolchain(8) }

tasks.test { useJUnitPlatform() }


val appName = "jstrdups"
val mainClassName = "io.foldright.dslf.DuplicateStringLiteralFinder"
val buildDir: File = layout.buildDirectory.get().asFile

val taskGenAutoComplete by tasks.registering(JavaExec::class) {
  // The module path: typically the runtimeClasspath of your main source set
  classpath = sourceSets["main"].runtimeClasspath
  workingDir = buildDir
  mainClass = "picocli.AutoComplete"
  args = listOf(mainClassName)
}

distributions {
  val completionFile: File = buildDir.resolve("${appName}_completion")
  main {
    contents {
      into("etc/bash_completion.d") { from(completionFile) }
      into("zsh/site-functions") { from(completionFile).rename { "_$appName" } }
    }
  }
}

application {
  applicationName = appName
  mainClass = mainClassName
}


arrayOf(tasks.distZip, tasks.distTar).forEach {
  it { dependsOn(taskGenAutoComplete) }
}
