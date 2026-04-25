### Note from the author, me, crispytwig :-)

It's definitely been a while, thanks for your patience! Please report any bugs you find in the port and I will try to address them as soon as possible.

With that being said, going forward, updates will likely not be as frequent as they used to, but I will be trying to maintain this more. In the future I'd like to add more animals from the Bedrock version, but I primarily want to focus on compatibility for other mods so I can focus on creating new content.

This version is 1.0.0 _(I know, after 5.0?)_ because it marks the first major update for Naturalist after our Bedrock port, and this will now serve as the baseline for the mod going forward. With that set, I want to properly start putting out things like hotfixes. Thank you again!

### Changelog :

- Ported from Architectury 1.20.1 -> NeoForge 1.21.1
    - Removed unused content.
- Added an optional resource pack for Spawn Eggs in the style of 1.21.5+
- Updated animal models, animations, textures, and sounds.
    - Big thanks to @MattDearGameAudio for the sounds :-)
    - Zebras now use a custom model and animations instead of the default Horse model.
    - Babies now have custom models, animations, and sounds
___

- Reorganized file paths a bit
- Fixed animation bugs -- using the animations of nearby entities of the same type, bone scaling, etc.
- Simplified entity configs to removal-only
- Adjusted entity panic speeds
- Gave "beefier" animals knockback resistance
- Fireflies have compat with Lamb Dynamic Lights
- Added Biomes o' Plenty compat to animals' biome tag spawning
    - Additionally fixed a lot of animals' spawn rules / blocks, they will now spawn on blocks like Podzol and Coarse Dirt, etc.
- Added new Butterfly variants, equaling :
    - Red Admiral
    - Monarch
    - Clouded Yellow
    - Green Swallowtail
    - Emerald Green Swallowtail
    - Blue Morpho
    - Purple Admiral
- Renamed "Bug Net" to "Capture Net"
- Renamed "Teddy Bear" to "Plush Bear"