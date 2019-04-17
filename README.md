# emo-gui
Eater's Mod Organizer

---

⚠️ **If you're planning to use this** ⚠️ please donate to [Forge's patreon](https://www.patreon.com/LexManos). Forge makes a part of their income via adfocus links. emo does not use the adfocus links so they're missing out on income that way. If you want to support emo, support Forge's patreon. all of this couldn't be possible without Forge.

---

emo is a new Minecraft launcher focused on making mod pack's fair and easy to use and build for everyone.

Instead of distributing modpacks as a jar, emo distributes modpacks as a composition file. emo then creates a Minecraft install based on this.


Because the composition file is a simple JSON (the stuff you make command block commands with) file and collection of mods is handled client-side there is no need to ask mod authors anymore for permission to use in a modpack.

## Composition file
```json
{
  "name": "My modpack",
  "minecraft": "1.12.2",
  "forge": "14.23.5.2768",
  "mods": [
    {
        "name": "ironchest",
        "url": "https://media.forgecdn.net/files/2670/493/ironchest-1.12.2-7.0.59.842.jar"
    }
  ]
}
```

# Status

emo-gui has currently reached MVP (Minimum Viable Product) state. The GUI is still in need of a lot of love. You are free to use it, but don't except everything to go well.

# Planned

* [ ] Repository support for GUI
* [ ] Image and description fields for modpacks
* [ ] Create Web GUI to make modpacks
* [ ] Add homepage and donate links for mod
* [ ] Make available accounts visible
* [ ] Make profile overview "sane"
* [ ] Allow deletion of profiles, accounts and modpacks
