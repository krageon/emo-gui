package me.eater.emo.gui

import javafx.application.Application
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import javafx.collections.transformation.FilteredList
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.CheckBox
import javafx.scene.control.ChoiceBox
import javafx.scene.control.ComboBox
import javafx.stage.Stage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URL
import java.util.*


class ModpackBuilderController : Initializable {
    @FXML
    var minecraftVersionSelector: ComboBox<Version>? = null
    @FXML
    var forgeVersionSelector: ComboBox<*>? = null
    @FXML
    var forgeEnabledInput: CheckBox? = null
    @FXML
    var useMinecraftSnapshotsInput: CheckBox? = null


    override fun initialize(location: URL?, resources: ResourceBundle?) {
        forgeEnabledInput!!.disableProperty().bind(useMinecraftSnapshotsInput!!.selectedProperty())
        forgeVersionSelector!!.disableProperty()
            .bind(forgeEnabledInput!!.selectedProperty().not().or(useMinecraftSnapshotsInput!!.selectedProperty()))


        useMinecraftSnapshotsInput!!.selectedProperty().addListener { _ ->
            minecraftVersionSelector!!.items = FXCollections.observableList(
                InfoStore.minecraftVersions.filter {
                    it.type == if (useMinecraftSnapshotsInput!!.isSelected) "snapshot" else "release"
                }
            )
        }
    }
}

class MainController : Initializable {
    override fun initialize(location: URL?, resources: ResourceBundle?) {

    }

    fun onNewProfileClick(actionEvent: ActionEvent) {
        WindowStore.modpackBuilderStage.show()
    }
}

class InfoStore {
    companion object {
        val minecraftVersions: MutableList<Version> = mutableListOf()
    }
}

class WindowStore {
    companion object {
        val mainWindow = FXMLLoader.load<Parent>(this::class.java.getResource("/main.fxml"))!!
        val modpackBuilderWindow = FXMLLoader.load<Parent>(this::class.java.getResource("/modpack-builder.fxml"))!!
        val modpackBuilderStage = Stage().apply {
            title = "New modpack - Eater's Mod Organizer"
            scene = Scene(modpackBuilderWindow)
        }
    }
}

class Main : Application() {


    override fun start(primaryStage: Stage?) {
        primaryStage!!.title = "emo - Eater's Mod Organizer"
        primaryStage.scene = Scene(WindowStore.mainWindow)
        primaryStage.show()

        GlobalScope.launch {
            InfoStore.minecraftVersions.addAll(getMinecraftVersionsManifest().versions)
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(Main::class.java)
        }
    }
}