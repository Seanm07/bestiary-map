# Runelite Worldmap Bestiary Overlay Plugin
This plugin is currently under development! It's my first Runelite project I've worked on, so I'm slowly learning as I go.

As a newbie I found it annoying needing to check the wiki every time I needed the location of a monster and was surprised nobody had made a bestiary plugin to just search monsters on the world map to find their location.

### Preview of current status
Some things shown are for placeholder or debugging purposes and will be changed as the project continues.

![World Map Preview](https://www.spacemeat.space/webm/java_ta2zhgVRlr.png)

### Current features

- Button to toggle the bestiary overlay on/off
- Button tooltips and click events
- Search box which functions similar to the existing world map search (with some improvements)
- Search text input which doesn't type in chat or get affected by other key remapping plugins
- Monster spawns shown as dots on map filtered by current search
- Ability to click on monster spawns to jump to them
- Left and right arrows next to the search box to jump between groups of spawns across the map [Preview Video](https://www.spacemeat.space/webm/java_dB4l3DqBGh.mp4)
- Label to show current group focus and total groups

### Planned features

- Search suggestions similar to existing world map red text suggestions as you type
- Hide other map markers while bestiary overlay enabled (togglable via plugin config)
- Better icon for showing monster spawns than a red dot (icons for each monster?)
- Checkbox for including map zones other than the current focus
- Right click menu options on the bestiary toggle button (currently implemented but not working)
- Cache the monster spawn data on serverside and clientside
- Automate serverside regeneration of the monster spawn data
- Add support for monster spawns other than just "point" spawns? (Not sure if needed)