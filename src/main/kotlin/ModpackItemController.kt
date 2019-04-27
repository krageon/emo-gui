package me.eater.emo.gui

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.control.TitledPane
import java.io.IOException
import java.net.URL
import java.util.*

class ModpackItemController(val modpack: Modpack) : Initializable {
    @FXML
    lateinit var versionLabel: Label
    @FXML
    lateinit var nameLabel: Label
    @FXML
    lateinit var modsPane: TitledPane
    @FXML
    lateinit var modListTextFlow: Label

    val parent: Parent

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        modsPane.text = "Mods (${modpack.mods.size})"
        versionLabel.text = "Minecraft: ${modpack.minecraft} Forge: ${modpack.forge ?: "no"}"
        nameLabel.text = modpack.name
        modListTextFlow.text = modpack.mods
            .map { it.name }
            .joinToString("\n")
    }

    fun onCreateProfileAction() {
        WindowStore.profileInstaller.showFor(this.modpack)
    }

    init {
        val fxmlLoader = FXMLLoader(javaClass.getResource("/modpack-item.fxml"))
        fxmlLoader.setController(this)

        try {
            parent = fxmlLoader.load<Parent>()
        } catch (exception: IOException) {
            throw RuntimeException(exception)
        }
    }
}