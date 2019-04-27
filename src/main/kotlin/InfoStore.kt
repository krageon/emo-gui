package me.eater.emo.gui

import Profile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import java.util.*

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