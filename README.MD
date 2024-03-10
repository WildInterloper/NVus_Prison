## 🔗 Useful Stuff
Author(s): [@never2nv](https://git.fngnation.net/never2nv)
| [Documentation](https://www.spigotmc.org/resources/nvus-prison-lite.115441/field?field=documentation)

![portfolio](https://img.shields.io/badge/my_portfolio-000?style=for-the-badge&logo=ko-fi&logoColor=white)

# NVus Prison (GOLD EDITION)

Useful features for Minecraft Prison Servers & Then Some :P




## Features
![NVus Prison Lite Info](https://git.fngnation.net/never2nv/NVus_Prison_Lite/raw/branch/main/assets/plugin_info.png)

Currently, the plugin implements the following features, so far:

- Equips orange leather armor to all prisoners. It also prevents prisoners from changing or equipping any other armor.

- It generates a banned_items.yml file which can be configured to ban certain items for prisoners. Banned items cannot be used, equipped in hand, transferred to storage or even crafted by prisoners.

- Adds a toggle in the config.yml to turn on/off AutoPickup for prisoners to automatically pickup mined resource drops (if they have enough room in their inventory for the drop(s)).

- Adds a toggle in the config.yml for AutoSwitch, when set to true it allows server admins to customize the auto_switch.yml which will list what type of tools prisoners can auto switch to and what materials will trigger the auto switch to certain tools. Mining stone then prisoners hit clay? It will auto switch to the proper tool, if they have it in their inventory!

- You can now toggle PrisonerArmor and RestrictArmor options in the main config.yml file. Turning off PrisonerArmor will of course not give any prisoners the default orange leather armor. Turning off RestrictArmor will allow prisoners to change/remove their armor
    .
- PrisonerTools: You can select which tool(s) are considered "Prisoner Tools" these tools are used for the auto switch and tool damage features.

- You can toggle ToolDamage. Setting to FALSE will prevent prisoner tools from being damaged when prisoners use them for mining etc!

- Commands for admins to use in-game to toggle certain features on/off, reload the configs and check plugin version.

- /nvus menu will now open an easy GUI Menu to toggle options on/off

- Armor is still craftable by prisoners. We felt this to be a cool roleplay element like allowing prisoners to craft and sell armor in the prison shops. Almost like the age ol' prisoners creating license plates! HAHA. If you don't like this you can always add various types of armor to the banned_items.yml file ie IRON_HELMET, DIAMOND_CHESTPLATE etc


## Documentation

[Documentation](https://www.spigotmc.org/resources/nvus-prison-lite.115441/field?field=documentation)

## Upcoming Features:
 - [X]  GUI Menu
 - [X]  Toggle Prisoner Tool Damage
 - [ ]  Prison Gangs
 - [ ]  Additional Gang Commands (kick, promote, disband)
 - [ ]  Tree Farm

