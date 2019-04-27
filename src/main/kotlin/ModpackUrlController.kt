package me.eater.emo.gui

import com.beust.klaxon.Klaxon
import com.github.kittinunf.fuel.coroutines.awaitString
import com.github.kittinunf.fuel.httpGet
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.paint.Paint
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import java.net.URL
import java.util.*

class ModpackUrlController : Initializable {
    @FXML
    lateinit var statusLabel: Label
    @FXML
    lateinit var urlInput: TextField
    @FXML
    lateinit var doneButton: Button

    var modpack: Modpack? = null
    var currentJob: Deferred<Pair<String, Boolean>>? = null

    override fun initialize(location: URL?, resources: ResourceBundle?) {

        urlInput.textProperty().addListener { _, _, newValue -> checkUrl(newValue) }
    }

    fun checkUrl(url: String) {
        val oldJob = currentJob
        doneButton.isDisable = true
        currentJob = GlobalScope.async {
            try {
                if (oldJob !== null) oldJob.cancelAndJoin()
            } catch (_: Exception) {
            }
            try {
                val json = url
                    .httpGet()
                    .awaitString()
                modpack = Klaxon().parse<Modpack>(json)!!
            } catch (e: Exception) {
                return@async Pair("Invalid modpack URL", false)
            }

            Pair(
                "Found modpack: ${modpack!!.name}\n\nMinecraft: ${modpack!!.minecraft}\nForge: ${modpack!!.forge
                    ?: "no"}\nMods: ${modpack!!.mods.size}",
                true
            )
        }

        GlobalScope.launch {
            val (text, success) = currentJob?.await() ?: return@launch

            launch(Dispatchers.JavaFx) {
                statusLabel.text = text
                statusLabel.textFill = if (success) Paint.valueOf("#20bf6b") else Paint.valueOf(
                    "#eb3b5a"
                )
                if (url == urlInput.text && modpack !== null) {
                    doneButton.isDisable = !success
                }
            }
        }
    }

    fun onCancelAction() {
        WindowStore.modpackUrlStage.close()
    }

    fun onDoneAction() {
        if (modpack !== null) {
            InfoStore.instance.addModpack(modpack!!)
            WindowStore.modpackUrlStage.close()
        }
    }
}