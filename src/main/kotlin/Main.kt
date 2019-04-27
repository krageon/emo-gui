package me.eater.emo.gui

import com.beust.klaxon.Klaxon
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Modality
import javafx.stage.Stage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class Main : Application() {


    override fun start(primaryStage: Stage?) {
        WindowStore.primaryStage = primaryStage!!
        WindowStore.modpackUrlStage.initOwner(WindowStore.primaryStage)
        WindowStore.modpackUrlStage.initModality(Modality.APPLICATION_MODAL)
        WindowStore.profileInstaller.stage.initOwner(WindowStore.primaryStage)
        WindowStore.profileInstaller.stage.initModality(Modality.APPLICATION_MODAL)
        WindowStore.loginModal.stage.initOwner(WindowStore.primaryStage)
        WindowStore.loginModal.stage.initModality(Modality.APPLICATION_MODAL)
        primaryStage.title = "emo - Eater's Mod Organizer"
        primaryStage.scene = Scene(WindowStore.mainWindow, 600.0, 400.0)
        primaryStage.show()
    }

    override fun stop() {
        guiConfig.toFile().writeText(Klaxon().toJsonString(InfoStore.instance))
    }

    companion object {
        val guiConfig: Path = Paths.get(System.getProperty("user.home"), ".emo/gui.json")

        @JvmStatic
        fun main(args: Array<String>) {
            Files.createDirectories(guiConfig.parent)
            try {
                InfoStore.instance = Klaxon().parse(guiConfig.toFile())!!
            } catch (_: Exception) {
                InfoStore.instance = InfoStore()
            }

            launch(Main::class.java)
        }
    }
}