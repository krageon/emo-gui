package me.eater.emo.gui

import com.beust.klaxon.Klaxon
import com.github.kittinunf.fuel.coroutines.awaitString
import com.github.kittinunf.fuel.httpGet

class VersionsManifest(
    val latest: Latest,
    val versions: Array<Version>
)

class Version(
    val id: String,
    val type: String,
    val url: String,
    val time: String,
    val releaseTime: String
) {
    override fun toString(): String {
        return "$id ($type)"
    }
}

class Latest(val release: String, val snapshot: String)

suspend fun getMinecraftVersionsManifest(): VersionsManifest {
    val json = "https://launchermeta.mojang.com/mc/game/version_manifest.json"
        .httpGet()
        .awaitString()

    return Klaxon().parse<VersionsManifest>(json)!!
}