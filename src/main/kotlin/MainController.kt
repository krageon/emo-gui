package me.eater.emo.gui

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.TabPane
import javafx.scene.layout.VBox
import java.awt.Desktop
import java.net.URI
import java.net.URL
import java.util.*

class MainController : Initializable {
    @FXML
    lateinit var tabViews: TabPane
    @FXML
    lateinit var modpacksList: VBox
    @FXML
    lateinit var profileList: VBox

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        modpacksList.children.addAll(InfoStore.instance.modpacks.map { ModpackItemController(
            it
        ).parent })
        profileList.children.addAll(InfoStore.instance.profiles.map { ProfileItem(
            it
        ).parent })

        InfoStore.instance.addObserver { _, _ ->
            modpacksList.children.clear()
            modpacksList.children.addAll(InfoStore.instance.modpacks.map { ModpackItemController(
                it
            ).parent })

            profileList.children.clear()
            profileList.children.addAll(InfoStore.instance.profiles.map { ProfileItem(
                it
            ).parent })
        }
    }

    fun onNewProfileClick() {
        tabViews.selectionModel.select(1)
    }

    fun onModpackAddViaUrlAction() {
        WindowStore.modpackUrlStage.minHeight = 159.0
        WindowStore.modpackUrlStage.minWidth = 346.0
        WindowStore.modpackUrlStage.showAndWait()
    }

    fun onAddAccountClick() {
        WindowStore.loginModal.show()
    }

    fun onDonationLinkAction() {
        try {
            Desktop.getDesktop().browse(URI("https://www.patreon.com/LexManos"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}