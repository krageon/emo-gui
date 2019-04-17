package me.eater.emo.gui

import com.beust.klaxon.Klaxon
import com.github.kittinunf.fuel.coroutines.awaitString
import com.github.kittinunf.fuel.httpGet
import com.mojang.authlib.Agent
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
import javafx.application.Application
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.scene.paint.Paint
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.stage.DirectoryChooser
import javafx.stage.Modality
import javafx.stage.Stage
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import me.eater.emo.*
import me.eater.emo.emo.Settings
import java.io.IOException
import java.net.Proxy
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

class Modpack(
    val name: String,
    val minecraft: String,
    val forge: String? = null,
    val mods: List<Mod> = listOf()
)

class Mod(
    val name: String,
    val url: String
)

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
        if (res.isDirectory()) {
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
                InfoStore.instance.addProfile(Profile(nameInput.text, profileDirInput.text, currentModpack))

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

class LoginModal {
    private val parent: Parent
    val stage: Stage

    @FXML
    lateinit var usernameInput: TextField

    @FXML
    lateinit var passwordInput: PasswordField

    @FXML
    lateinit var failedLoginLabel: Label



    fun onLoginAction() {
        val settings = Settings.load()
        val authService = YggdrasilAuthenticationService(Proxy.NO_PROXY, settings.clientToken)
            .createUserAuthentication(Agent.MINECRAFT)
            .apply {
                setUsername(usernameInput.text)
                setPassword(passwordInput.text)
            }

        try {
            authService.logIn()
        } catch (e: Exception) {
            failedLoginLabel.text = "Failed to login: ${e.message}"
            failedLoginLabel.isVisible = true
            return
        }

        if (!authService.isLoggedIn) {
            failedLoginLabel.text = "Failed to login: Reason unknown"
            failedLoginLabel.isVisible = true
            return
        }

        val stor = authService.saveForStorage()
        settings.addAccount(stor)
        settings.selectAccount(authService.selectedProfile.id.toString())
        settings.save()
        stage.close()
    }

    fun onCancelAction() {
        stage.close()
    }

    fun show() {
        stage.minHeight = 162.0
        stage.minWidth = 376.0
        usernameInput.text = ""
        passwordInput.text = ""
        failedLoginLabel.isVisible = false
        stage.showAndWait()
    }

    init {
        val fxmlLoader = FXMLLoader(javaClass.getResource("/account-login.fxml"))
        fxmlLoader.setController(this)

        try {
            parent = fxmlLoader.load<Parent>()
            stage = Stage().apply {
                scene = Scene(parent)
                title = "Login - Eater's Mod Organizer"
            }
        } catch (exception: IOException) {
            throw RuntimeException(exception)
        }
    }
}

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
                statusLabel.textFill = if (success) Paint.valueOf("#20bf6b") else Paint.valueOf("#eb3b5a")
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

class MainController : Initializable {
    @FXML
    lateinit var tabViews: TabPane
    @FXML
    lateinit var modpacksList: VBox
    @FXML
    lateinit var profileList: VBox

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        modpacksList.children.addAll(InfoStore.instance.modpacks.map { ModpackItemController(it).parent })
        profileList.children.addAll(InfoStore.instance.profiles.map { ProfileItem(it).parent })

        InfoStore.instance.addObserver { _, _ ->
            modpacksList.children.clear()
            modpacksList.children.addAll(InfoStore.instance.modpacks.map { ModpackItemController(it).parent })

            profileList.children.clear()
            profileList.children.addAll(InfoStore.instance.profiles.map { ProfileItem(it).parent })
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
}

class Profile(
    val name: String,
    val location: String,
    val modpack: Modpack
)

class InfoStore(
    val modpacks: MutableList<Modpack> = mutableListOf(),
    val profiles: MutableList<Profile> = mutableListOf()
) : Observable() {

    fun addModpack(modpack: Modpack) {
        this.modpacks.add(modpack)
        this.notifyJavaFX()
    }

    fun addProfile(profile: Profile) {
        this.profiles.add(profile)
        this.notifyJavaFX()
    }

    fun notifyJavaFX() {
        val store = this
        GlobalScope.launch(Dispatchers.JavaFx) {
            store.setChanged()
            store.notifyObservers()
        }
    }

    companion object {
        lateinit var instance: InfoStore
    }
}

class WindowStore {
    companion object {
        val mainWindow = FXMLLoader.load<Parent>(this::class.java.getResource("/main.fxml"))!!
        val modpackUrl = FXMLLoader.load<Parent>(this::class.java.getResource("/modpack-url.fxml"))
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