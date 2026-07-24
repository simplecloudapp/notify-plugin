# Notify Plugin

![Banner][banner]

<div align="center">

  [![Modrinth][badge-modrinth]][modrinth]
  [![License][badge-license]][license]
  <br>

  [![Discord][badge-discord]][social-discord]
  [![Follow @simplecloudapp][badge-x]][social-x]
  [![Follow @simplecloudapp][badge-bluesky]][social-bluesky]
  [![Follow @simplecloudapp][badge-youtube]][social-youtube]
  <br>

  [Report a Bug][issue-bug-report]
  ·
  [Request a Feature][issue-feature-request]
  <br>

🌟 Give us a star — your support means the world to us!
</div>
<br>

> All information about this project can be found in our detailed [documentation][docs-thisproject].

Keep your staff informed about server state changes with real-time notifications using MiniMessage formatting.

## Features

- [x] **Ingame Notifiction**: Get chat messages about server changes
- [x] **Customize Messages**: Configurable messages ([minimessage](https://docs.papermc.io/adventure/minimessage/) supported)
- [x] **Velocity & BungeeCord support**

## Configuration

Here you can see the configuration file for the plugin. All possible options are explained in the comments.
```yaml
version: 1

# ───────────────────────────────────────────────────────────────────────────────
# Format Settings
# ───────────────────────────────────────────────────────────────────────────────
format:
    date: "dd.MM.yyyy HH:mm:ss"

# ───────────────────────────────────────────────────────────────────────────────
# Variables
# Reusable variables that can be used throughout the messages.
# Usage: <variable_name> will be replaced with the defined value.
# ───────────────────────────────────────────────────────────────────────────────
variables:
    prefix: "<color:#0EA5E9><bold>⚡</bold></color>"

# ───────────────────────────────────────────────────────────────────────────────
# Command Messages
# ───────────────────────────────────────────────────────────────────────────────
command:
    help:
        title: "<prefix> <#0EA5E9>SimpleCloud Notify commands"
        entry: "<#E2E8F0><command>"
        empty: "<prefix> <#F59E0B>No notify commands are available for you."

    usage:
        invalid: "<prefix> <#DC2626>Use <#F8FAFC><command> <#DC2626>instead."
        entry: "<#E2E8F0><command>"
        invalid-state: "<prefix> <#DC2626>Use <#F8FAFC>enable <#DC2626>or <#F8FAFC>disable<#DC2626>."
        invalid-toggle-value: "<prefix> <#DC2626>Use <#F8FAFC>true <#DC2626>or <#F8FAFC>false<#DC2626>."
        missing-player: "<prefix> <#DC2626>Missing player <#F8FAFC><playername><#DC2626>."

    permission:
        denied: "<prefix> <#DC2626>You do not have permission to use this command."

    notify:
        enabled: "<prefix> <#A3E635>Server notifications are now <#F8FAFC>enabled<#A3E635>."
        disabled: "<prefix> <#A3E635>Server notifications are now <#F8FAFC>disabled<#A3E635>."
        already-enabled: "<prefix> <#F59E0B>Server notifications are already <#F8FAFC>enabled<#F59E0B>."
        already-disabled: "<prefix> <#F59E0B>Server notifications are already <#F8FAFC>disabled<#F59E0B>."

    set:
        enabled: "<prefix> <#A3E635>Server notifications are now <#F8FAFC>enabled <#A3E635>for <white><head:<playername>:true> <#F8FAFC><displayname><#A3E635>."
        disabled: "<prefix> <#A3E635>Server notifications are now <#F8FAFC>disabled <#A3E635>for <white><head:<playername>:true> <#F8FAFC><displayname><#A3E635>."
        already-enabled: "<prefix> <#F59E0B>Server notifications are already <#F8FAFC>enabled <#F59E0B>for <white><head:<playername>:true> <#F8FAFC><displayname><#F59E0B>."
        already-disabled: "<prefix> <#F59E0B>Server notifications are already <#F8FAFC>disabled <#F59E0B>for <white><head:<playername>:true> <#F8FAFC><displayname><#F59E0B>."

    reload:
        config:
            success: "<prefix> <#A3E635>Notify configuration was reloaded."
            failed: "<prefix> <#DC2626>Notify configuration could not be reloaded."

    error:
        player-not-found: "<prefix> <#DC2626>Player <#F8FAFC><playername> <#DC2626>was not found."
        player-not-online: "<prefix> <#DC2626>Player <#F8FAFC><playername> <#DC2626>is not online."
        only-players: "<prefix> <#DC2626>This command can only be used by players."
        storage-unavailable: "<prefix> <#DC2626>Notify settings are currently unavailable."
        internal: "<prefix> <#DC2626>An internal error occurred. Try again later."

# ───────────────────────────────────────────────────────────────────────────────
# Notifications
# Notification messages for different server and group states.
#
# NOTE: If you delete a state entry, the corresponding notification will not be sent.
# ───────────────────────────────────────────────────────────────────────────────
notifications:
    hover:
        server: |-
            <#E2E8F0>Server information
            <br><#94A3B8>Time<#475569>: <#E2E8F0><time>
            <br><#94A3B8>State<#475569>: <#E2E8F0><state>
            <br><#94A3B8>Address<#475569>: <#E2E8F0><ip>:<port>
            <br><#94A3B8>Players<#475569>: <#E2E8F0><players>/<max>
        group: |-
            <#E2E8F0>Group information
            <br><#94A3B8>Time<#475569>: <#E2E8F0><time>
    server:
        starting: "<prefix> <#0EA5E9>Server <hover:show_text:'<notifications.hover.server>'><#F8FAFC><server></hover> <#0EA5E9>is now <#F8FAFC>starting<#0EA5E9>."
        available: "<prefix> <#A3E635>Server <hover:show_text:'<notifications.hover.server>'><#F8FAFC><server></hover> <#A3E635>is now <#F8FAFC>available<#A3E635>."
        stopping: "<prefix> <#F59E0B>Server <hover:show_text:'<notifications.hover.server>'><#F8FAFC><server></hover> <#F59E0B>is now <#F8FAFC>stopping<#F59E0B>."
        stopped: "<prefix> <#DC2626>Server <hover:show_text:'<notifications.hover.server>'><#F8FAFC><server></hover> <#DC2626>is now <#F8FAFC>stopped<#DC2626>."
    group:
        created: "<prefix> <#0EA5E9>Group <hover:show_text:'<notifications.hover.group>'><#F8FAFC><group></hover> <#0EA5E9>was created."
        deleted: "<prefix> <#DC2626>Group <hover:show_text:'<notifications.hover.group>'><#F8FAFC><group></hover> <#DC2626>was deleted."
```

## Contributing
Contributions to SimpleCloud are welcome and highly appreciated. However, before you jump right into it, we would like you to read our [Contribution Guide][docs-contribute].

## License
This repository is licensed under [Apache 2.0][license].


<!-- LINK GROUP -->

<!-- ✅ PLEASE EDIT -->
[banner]: https://raw.githubusercontent.com/simplecloudapp/branding/refs/heads/main/readme/banner/plugin/notify.png
[issue-bug-report]: https://github.com/simplecloudapp/notify-plugin/issues/new?labels=bug&projects=template=01_BUG-REPORT.yml&title=%5BBUG%5D+%3Ctitle%3E
[issue-feature-request]: https://github.com/simplecloudapp/notify-plugin/discussions/new?category=ideas
[docs-thisproject]: https://docs.simplecloud.app/en/manual/plugin/notify
[docs-contribute]: https://docs.simplecloud.app/contribute

[modrinth]: https://modrinth.com/plugin/notify-plugin
[maven-central]: https://central.sonatype.com/artifact/app.simplecloud.controller/controller-api
[dev]: https://repo.simplecloud.app/#/snapshots/app/simplecloud/controller/controller-api


[artifacts]: https://repo.simplecloud.app/#/snapshots/app/simplecloud/controller/controller-api
[dev-artifacts]: https://repo.simplecloud.app/#/snapshots/app/simplecloud/controller/controller-api

[badge-maven-central]: https://img.shields.io/maven-central/v/app.simplecloud.controller/controller-api?labelColor=18181b&style=flat-square&color=65a30d&label=Release
[badge-dev]: https://repo.simplecloud.app/api/badge/latest/snapshots/app/simplecloud/controller/controller-api?name=Dev&style=flat-square&color=0ea5e9

<!-- ⛔ DON'T TOUCH -->
[license]: https://opensource.org/licenses/Apache-2.0
[snapshots]: https://repo.simplecloud.app/#/snapshots

[social-x]: https://x.com/simplecloudapp
[social-bluesky]: https://bsky.app/profile/simplecloud.app
[social-youtube]: https://www.youtube.com/@thesimplecloud9075
[social-discord]: https://discord.simplecloud.app

[badge-modrinth]: https://img.shields.io/badge/modrinth-18181b.svg?style=flat-square&logo=modrinth
[badge-license]: https://img.shields.io/badge/apache%202.0-blue.svg?style=flat-square&label=license&labelColor=18181b&style=flat-square&color=e11d48
[badge-discord]: https://img.shields.io/badge/Community_Discord-d95652.svg?style=flat-square&logo=discord&color=27272a
[badge-x]: https://img.shields.io/badge/Follow_@simplecloudapp-d95652.svg?style=flat-square&logo=x&color=27272a
[badge-bluesky]: https://img.shields.io/badge/Follow_@simplecloud.app-d95652.svg?style=flat-square&logo=bluesky&color=27272a
[badge-youtube]: https://img.shields.io/badge/youtube-d95652.svg?style=flat-square&logo=youtube&color=27272a
