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
# The date format pattern the notify plugin uses.
# For more information, see the official Java documentation: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/text/SimpleDateFormat.html
date-format: dd.MM.yyyy HH:mm:ss

# Filter server states to notify about.
server-state-filter:
    # server-state obviously sets the server state to filter for
-   server-state: STARTING
    # The message that is being sent to the permitted players goes here. 
    # Don't get irritated by the |- format, it's just a multiline string. You can also pass oneliners.
    # Possible placeholders you can use here are:
    # - <server_group>: The group name the server belongs to
    # - <server_id>: The numerical id of the server
    # - <server_uuid>: The UUID of the server
    # - <server_state>: The state of the server after the update
    # - <server_ip>: The IP that the server is running on
    # - <server_port>: The port that the server is bound to
    # - <online_players>: The amount of players currently online on the server
    # - <max_players>: The maximum amount of players that can join the server
    # - <server_update_date>: The date of the last update of the server
    # - <server_create_date>: The date of the initial update (creation) of the server
    message: |-
        <color:#38bdf8><bold>⚡</bold></color> <hover:show_text:'<color:#38bdf8><bold>⚡</bold></color> Information of <server_group> <server_id>
           <color:#a3a3a3>Timestamp:</color> <color:#38bdf8><server_update_date></color>
           <color:#a3a3a3>State:</color> <color:#38bdf8><server_state></color>
           <color:#a3a3a3>Server-IP:</color> <color:#38bdf8><server_ip></color>
           <color:#a3a3a3>Port:</color> <color:#38bdf8><server_port></color>
           <color:#a3a3a3>Players:</color> <color:#38bdf8><online_players>/<max_players></color>'><color:white>Server <server_group> <server_id></hover> updated its state to <color:#fbbf24><server_state></color>.
    # Users will need this permission to receive the message. Leave it empty (just '') to allow everyone to receive it.
    permission: notify.receive.state-changed.starting
    
    # You can add more server states to filter like this:
-   server-state: AVAILABLE
    # To design your own messages, we recommend using the minimessage web-ui: https://webui.advntr.dev/
    message: |-
        <color:#38bdf8><bold>⚡</bold></color> <hover:show_text:'<color:#38bdf8><bold>⚡</bold></color> Information of <server_group> <server_id>
           <color:#a3a3a3>Timestamp:</color> <color:#38bdf8><server_update_date></color>
           <color:#a3a3a3>State:</color> <color:#38bdf8><server_state></color>
           <color:#a3a3a3>Server-IP:</color> <color:#38bdf8><server_ip></color>
           <color:#a3a3a3>Port:</color> <color:#38bdf8><server_port></color>
           <color:#a3a3a3>Players:</color> <color:#38bdf8><online_players>/<max_players></color>'><color:white>Server <server_group> <server_id></hover> updated its state to <color:#fbbf24><server_state></color>.
    # The default permission is notify.receive.state-changed.[server-state]
    permission: notify.receive.state-changed.available
    
-   server-state: STOPPING
    message: |-
        <color:#38bdf8><bold>⚡</bold></color> <hover:show_text:'<color:#38bdf8><bold>⚡</bold></color> Information of <server_group> <server_id>
           <color:#a3a3a3>Timestamp:</color> <color:#38bdf8><server_update_date></color>
           <color:#a3a3a3>State:</color> <color:#38bdf8><server_state></color>
           <color:#a3a3a3>Server-IP:</color> <color:#38bdf8><server_ip></color>
           <color:#a3a3a3>Port:</color> <color:#38bdf8><server_port></color>
           <color:#a3a3a3>Players:</color> <color:#38bdf8><online_players>/<max_players></color>'><color:white>Server <server_group> <server_id></hover> updated its state to <color:#fbbf24><server_state></color>.
    # Here you can see that everyone will receive the message, as the permission is empty.
    permission: ''
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
