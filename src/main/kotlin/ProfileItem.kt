package me.eater.emo.gui

import Profile
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Parent
import javafx.scene.control.Alert
import javafx.scene.control.Label
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import me.eater.emo.StartCommand
import java.io.IOException
import java.net.URL
import java.util.*

class ProfileItem(val profile: Profile) : Initializable {
    val parent: Parent

    @FXML
    lateinit var nameLabel: Label

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        nameLabel.text = profile.name
    }

    fun onStartClick() {
        Platform.setImplicitExit(false)
        WindowStore.primaryStage.close()
        GlobalScope.launch {
            var success = true
            try {
                // Blocking
                StartCommand(listOf(profile.location)).execute()
            } catch (e: Exception) {
                println(e)
                success = false
            }

            launch(Dispatchers.JavaFx) {
                WindowStore.primaryStage.show()
                Platform.setImplicitExit(true)

                if (!success) {
                    Alert(Alert.AlertType.ERROR).apply {
                        contentText = "Failed to start Minecraft"
                    }.showAndWait()
                }
            }
        }
    }

    init {
        val fxmlLoader = FXMLLoader(javaClass.getResource("/profile-item.fxml"))
        fxmlLoader.setController(this)

        try {
            parent = fxmlLoader.load<Parent>()
        } catch (exception: IOException) {
            throw RuntimeException(exception)
        }
    }
}