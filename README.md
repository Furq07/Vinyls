# Vinyls

Vinyls is a plugin designed to add the ability to create configurable custom music discs using resource packs.

## Key Features

- **Custom Music Discs:** Easily add your own music tracks to Minecraft.
- **Performance Optimization:** Developed with server performance in mind, ensuring smooth operation.
- **User-Friendly:** Includes commands for reloading the configuration and giving discs to players.
- **Cross-Version Compatibility:** Supports Minecraft versions from 1.16.5 to the latest, ensuring broad applicability.
- **Configurable:** Utilize a simple `discs.yml` file to configure your custom discs.
- **Resource Pack Generation:** Automatic Resource Pack generation from the `discs.yml` configuration file.

## Available Commands

- **/vinyls reload:** Reload the plugin's configuration without restarting the server.
- **/vinyls givedisc <discname> <player>:** Give a specified custom music disc to a player.

## Permissions

- `vinyls.admin`: Grants the ability to execute all admin commands of the plugin.

## Installation Steps

1. Obtain the latest version of Vinyls from the [Modrinth Page](https://modrinth.com/plugin/vinyls).
2. Place the downloaded `.jar` file into your server's `plugins` directory.
3. Restart your server to activate the plugin.
4. Customize the `discs.yml` file located in `plugins/Vinyls/discs.yml` to add your custom music discs.
5. Load the provided resource pack by uploading it to a site like [mc-packs.net](https://mc-packs.net) then in your `server.properties` file:
- Add the provided URL to `resource-pack` property.
- Add the provided SHA1 to `resource-pack-sha1` property.
- Set `require-resource-pack` property to `true`.
- Incase you are already using a resource pack, merge it with your existing resource pack.

## Note to Server Owners

To ensure the custom music discs work correctly, Following the Step 5 mentioned above is crucial.

## Support

For any queries or assistance with Vinyls, please visit the [GitHub repository](https://github.com/furq07/vinyls/issues) or [Discord Server](https://discord.gg/7ugrBEKza4).

## License

Vinyls is released under the MIT License.
