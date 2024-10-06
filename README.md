# 💽 **Vinyls**

*Enhance your Minecraft experience with custom music discs!*

---

## 🎧 **Key Features**

| Feature | Description |
| --- | --- |
| **🎶 Custom Music Discs** | Easily add your own tracks to Minecraft. |
| **⚡ Performance Optimization** | Built to ensure smooth server performance. |
| **💡 User-Friendly** | Simple commands for reloading configuration and giving discs to players. |
| **⚙ Configurable** | Manage your custom discs via a straightforward `discs.yml` file. |
| **🎨 Resource Pack Generation** | Automatically generate a resource pack from your configuration. |
| **🔃 Cross-Version Compatibility** | Supports Minecraft versions from `1.16.5` to the latest. |
| **🔌 Fabric Support** | Supports Fabric from versions `1.20+` to `1.21.1`. |

---

## 💻 **Available Commands**

| Command | Description |
| --- | --- |
| **`/vinyls reload`** | Reload the plugin's configuration without a server restart. |
| **`/vinyls give <disc_name> [player]`** | Grant a custom music disc to a player. |

---

## 🔒 **Permissions**

| Permission | Description |
| --- | --- |
| **`vinyls.admin`** | Grants access to all admin commands. |

---

## 📩 **Installation Steps**

1. **[Download Vinyls](https://modrinth.com/plugin/vinyls):** Get the latest version from Modrinth.
2. **Install:** 
   - Place the `.jar` file into your server's `plugins` directory.
   - Example:
     ```bash
     /plugins/Vinyls-x.x.x.jar
     ```
3. **Activate:**
   - Restart your server to load the plugin.
4. **Customize:** 
   - Edit the `discs.yml` file located in `plugins/Vinyls/discs.yml` to add your custom discs.
   - Example:
   ```yaml
   # Root node for all custom discs. Each disc entry is nested under this "discs" node.
   discs:
     # Unique identifier for each disc. Ensure that the identifier is consistent
     # with the OGG and PNG files located in the `source_files` folder.
     # Modifying this identifier after items are distributed will make previously
     # distributed items unusable.
     epic_disc:
       # The display name of the disc as shown in the game. You can use color codes
       # to customize the appearance of the name.
       display_name: "&bMusic Disc"
       # The material type used to represent the disc in-game. "PAPER" is used here,
       # but you can choose any material that suits your design.
       material: "PAPER"
       # Custom model data value to distinguish this item from others with the same
       # material. This is used for applying unique textures via a resource pack.
       custom_model_data: 1
       # Lore provides additional context or flavor text for the disc. Each line in
       # the lore array will appear as a separate line in the item's lore.
       lore:
         - "&7Vinyls - Epic" # First line of lore.
         - "&8Epic music by Vinyls!" # Second line of lore.
   ```
5. **Load Resource Pack:**
   - Upload the resource pack to [mc-packs.net](https://mc-packs.net).
   - Update the `server.properties` file:
     | Property | Value |
     | --- | --- |
     | `resource-pack` | *URL to resource pack* |
     | `resource-pack-sha1` | *SHA1 hash of the resource pack* |
     | `require-resource-pack` | `true` |
   - **Note:** If you're using an existing resource pack, merge it with the Vinyls resource pack.
   
---

## 📞 **Support**

For assistance, visit the [GitHub Repository](https://github.com/furq07/vinyls/issues) or join our [Discord Server](https://discord.gg/XhZzmvzPDV).

---

## 📜 **License**

Vinyls is released under the [MIT License](https://opensource.org/licenses/MIT).

---

## 🤝 **Partner**

<p align="center"> <a href="https://billing.revivenode.com/aff.php?aff=517"> <img src="https://versions.revivenode.com/resources/banner_wide_one.gif" alt="Partner GIF"> </a> </p> <p align="center"> Use code <b>FURQ</b> for 15% off your order! </p>
