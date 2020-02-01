### Environs-1.14.4-0.0.5.0
**Requirements**
* Forge 1.14.4-28.1.96+
* Dynamic Surroundings: Sound Control 1.14.4-0.0.5.0+
* 100% client side; no server side deployment needed

**What's New**
* Initial release with the following features from Dynamic Surroundings:
  * Biome sounds
  * Random spot sounds that play around the player based on the biome
  * Village sounds
    * Because village implementation changed, effects are triggered if player is within 64 blocks of a bell as well as a villager.  On the plus side server side support is not required.
  * Waterfall splash and acoustics
  * Aurora Borealis
  * Fog
  * Steam, Fire, and Bubble jets
  * Fireflies
  * Water/Lava drop sounds and effects
* Uses JavaScript engine for condition string evaluation
* What's **not** included:
  * Does not have weather effects; planned for another mod
  * External config support; will be added when code changes settle a bit more
  * Battle Music
  * Speech Bubbles and Entity Chat
  * Damage pop-offs/crit words
  * Gnatt, insect, snake, bison, elephant, and crock sounds - removed because they didn't fit "Minecraft"
  
**Extras**
* Waterfall spray drops create water ripples when landing in water, or steam clouds when hitting hot blocks
* Full grown nether wart can give off flame particle effect
* Lit Furnaces and Blast Furnaces are considered hot blocks for steam production
* A full cauldron when near a hot block can produce steam
* Soul Sand laughter effect only happens when the block is in the nether
