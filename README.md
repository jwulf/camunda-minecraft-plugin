# Camunda Minecraft Plugin

The Camunda Engine embedded in a Minecraft plugin for a Bukkit-compatible Minecraft server.

The plugin has a single delegate method for task execution. The executor takes a `delegate` field injection.

This should be the name of a JavaScript function that will be invoked in a Nashorn Engine.