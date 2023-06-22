import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "net.skyprison"
version = "7.0.0"
description = "SkyPrisonCore"


repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.purpurmc.org/snapshots")
    maven("https://ci.mg-dev.eu/plugin/repository/everything/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://maven.playpro.com/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://nexus.bencodez.com/repository/maven-public/")
    maven("https://repo.kryptonmc.org/releases")
    maven("https://repo.md-5.net/content/groups/public/")
    maven("https://repo.rosewooddev.io/repository/public/")
    maven("https://maven.wolfyscript.com/repository/public/")
    maven("https://repo.maven.apache.org/maven2/")
}

dependencies {
    implementation("org.javacord:javacord:3.7.0")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.1.4")
    implementation("cloud.commandframework", "cloud-paper", "1.8.3")
    compileOnly("org.jetbrains:annotations:24.0.0")
    compileOnly("org.purpurmc.purpur:purpur-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("io.github.miniplaceholders:miniplaceholders-api:2.1.0")
    compileOnly("me.neznamy:tab-api:3.2.4")
    compileOnly("com.github.DieReicheErethons:Brewery:3.1.1")
    compileOnly("com.arcaniax:HeadDatabase-API:1.3.1")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.8-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.10.10")
    compileOnly("com.github.brcdev-minecraft:shopgui-api:3.0.0")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.11")
    compileOnly("com.bencodez:votingplugin:6.11.3")
    compileOnly("net.coreprotect:coreprotect:21.2")
    compileOnly("me.NoChance.PvPManager:PvPManager:3.10.9")
    compileOnly("com.github.alex9849:advanced-region-market:3.4.4")
    compileOnly("com.gitlab.ruany:LiteBansAPI:0.3.5")
    compileOnly("LibsDisguises:LibsDisguises:10.0.33")
    compileOnly("dev.esophose:playerparticles:8.3")
    compileOnly("com.github.Realizedd.Duels:duels-api:3.5.1")
    compileOnly("com.wolfyscript.wolfyutilities:wolfyutilities:3.16.1.0")
    compileOnly("com.wolfyscript.customcrafting:customcrafting-spigot:3.16.3.3")

    // Jars
    compileOnly(fileTree("libs") { include("*.jar")})
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<ShadowJar> {
    relocate("org.mariadb.jdbc", "net.skyprison.skyprisoncore.shaded.mariadb")
    relocate("cloud.commandframework", "net.skyprison.skyprisoncore.shaded.cloud")
    relocate("o.leangen.geantyref", "net.skyprison.skyprisoncore.shaded.typetoken")
}
