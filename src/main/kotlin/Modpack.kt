package me.eater.emo.gui

import Mod

class Modpack(
    val name: String,
    val minecraft: String,
    val forge: String? = null,
    val mods: List<Mod> = listOf()
)