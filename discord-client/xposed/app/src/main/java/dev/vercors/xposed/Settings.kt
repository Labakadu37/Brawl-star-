package dev.vercors.xposed

import org.json.JSONObject
import java.io.File

/** files/vercors/settings.json, written by the JS side: { "plugins": { "Name": { "enabled": true } } } */
class Settings(private val file: File) {
    fun read(): JSONObject = runCatching { JSONObject(file.readText()) }.getOrNull() ?: JSONObject()
}
