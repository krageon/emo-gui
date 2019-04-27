package me.eater.emo.gui

import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

class WindowStore {
    companion object {
        val mainWindow = FXMLLoader.load<Parent>(this::class.java.getResource("/main.fxml"))!!
        val modpackUrl =
            FXMLLoader.load<Parent>(this::class.java.getResource("/modpack-url.fxml"))
        val modpackUrlStage = Stage().apply {
            title = "Add modpack via URL - Eater's Mod Organizer"
            scene = Scene(modpackUrl)
            isResizable = false
            isAlwaysOnTop = true
        }

        val profileInstaller = ProfileInstaller()
        val loginModal = LoginModal()

        lateinit var primaryStage: Stage
    }
}