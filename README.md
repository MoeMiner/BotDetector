# BotDetector Bukkit Plugin

BotDetector is a Bukkit plugin designed to help server owners and administrators detect and handle players who use mining bots to gain unfair advantages in survival gameplay. The plugin uses multiple detection methods, including the Turing test for players who mine continuously, and the detection of non-human rotation actions unique to the Baritone cheat client (with high accuracy, but can be bypassed by serveral forks of Baritone/other cheating client).

The installation of this plugin is very simple - just drag and drop it into the plugins folder. The plugin provides two configuration options: the duration of continuous mining required to trigger the plugin's restrictions, and the length of time a player is banned for.

## Features

- Multiple detection methods to identify mining bots and cheating players
- Simple installation process
- Customizable configuration options

## Installation

1. Download the plugin from the releases page, or build manually.
2. Drag and drop the jar file into the `plugins/` folder of your Bukkit server.
3. Restart the server.

## Configuration

The plugin provides two configuration options:

### `constantly-mining-threshold`

The duration of continuous mining required to trigger the plugin's restrictions.

**default:** `15` (minutes)

### `ban-period`

The length of time a player is banned for if the plugin detected inhuman rotations.

**default:** `60` (minutes)

To change the configuration, open the `config.yml` file in the plugin folder and modify the values as desired.

## Usage

Once installed and configured, the plugin will automatically detect and handle players who use mining bots. If a player is detected for the behavior of continuing to mine without any response, they will be kicked and will receive a warning message. If we detected clear evidence that the player is not human, they will be banned for the configured duration.

## Support

If you encounter any issues or have any questions about the plugin, please feel free to open an issue on the GitHub repository.
