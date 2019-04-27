package me.eater.emo.gui

import Profile
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.paint.Paint
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import me.eater.emo.EmoContext
import me.eater.emo.EmoMod
import me.eater.emo.VersionSelector
import me.eater.emo.getInstallWorkflow
import java.io.IOException
import java.nio.file.Paths

class ProfileInstaller {
    private val parent: Parent
    val stage: Stage

    @FXML
    lateinit var nameInput: TextField

    @FXML
    lateinit var profileDirInput: TextField

    @FXML
    lateinit var installerOutputText: TextFlow

    @FXML
    lateinit var installButton: Button

    lateinit var currentModpack: Modpack

    fun showFor(modpack: Modpack) {
        currentModpack = modpack
        nameInput.text = modpack.name
        installerOutputText.children.clear()
        installerOutputText.children.add(Text("Install for ${modpack.name}\n"))
        stage.showAndWait()
    }

    fun onProfileDirSelect() {
        val x = DirectoryChooser()
        x.title = "Choose profile directory - Eater's Mod Organizer"
        val res = x.showDialog(this.stage)
        profileDirInput.text = res.absolutePath
        if (res.isDirectory) {
            installButton.isDisable = false
        }
    }

    fun onInstallClick() {
        val workflow = getInstallWorkflow(
            EmoContext(
                forgeVersion = if (currentModpack.forge == null) null else VersionSelector(currentModpack.forge!!),
                minecraftVersion = VersionSelector(currentModpack.minecraft),
                mods = currentModpack.mods.map { EmoMod(it.url, it.name) },
                installLocation = Paths.get(profileDirInput.text)
            )
        )

        workflow.processStarted += {
            GlobalScope.launch(Dispatchers.JavaFx) {
                installerOutputText.children.add(Text("${it.step}\n"))
            }
        }

        GlobalScope.launch {
            try {
                workflow.execute()
                workflow.waitFor()
            } catch (e: Exception) {
                launch(Dispatchers.JavaFx) {
                    installerOutputText.children.add(Text("Failed with exception:"))
                    installerOutputText.children.add(Text(e.toString()).apply {
                        fill = Paint.valueOf("#eb3b5a")
                    })
                }
                return@launch
            }

            launch(Dispatchers.JavaFx) {
                InfoStore.instance.addProfile(
                    Profile(
                        nameInput.text,
                        profileDirInput.text,
                        currentModpack
                    )
                )

                installerOutputText.children.add(Text("Success."))

                installButton.text = "Close"
                installButton.onAction = EventHandler {
                    stage.close()
                }
            }
        }
    }

    fun onCancelClick() {
        stage.close()
    }

    init {
        val fxmlLoader = FXMLLoader(javaClass.getResource("/profile-installer.fxml"))
        fxmlLoader.setController(this)

        try {
            parent = fxmlLoader.load<Parent>()
            stage = Stage().apply {
                scene = Scene(parent)
                title = "Profile installer - Eater's Mod Organizer"
            }
        } catch (exception: IOException) {
            throw RuntimeException(exception)
        }
    }
}