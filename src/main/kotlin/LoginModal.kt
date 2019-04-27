package me.eater.emo.gui

import com.mojang.authlib.Agent
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.stage.Stage
import me.eater.emo.emo.Settings
import java.io.IOException
import java.net.Proxy

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
        val authService = YggdrasilAuthenticationService(
            Proxy.NO_PROXY,
            settings.clientToken
        )
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