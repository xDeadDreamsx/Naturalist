# Naturalist 2.0 Changelog

## New Mobs

**Added Anglerfish**
- Spawn in Oceans `(any biome tagged as Ocean)`
    - Spawns deeper on the Y axis spawn as a glowing variant, like Glow Squids.
- Neutral; hunts Tropical Fish and Cod with a 2 minute cooldown after a kill, and fights back when attacked.
- Can be bucketed with a Water Bucket.
- Drops 1 Anglerfish `(cooked if killed by fire)`, with a 20% chance of Glowstone Dust.

**Added Ants**
- Spawn from Ant Hills, which generate in Jungles and Savannas.
    - Open hill blocks periodically release worker Ants.
- Picks up any dropped food items, carrying their item entity above itself, and stores it in their hill's internal inventory.
    - Picking up / stealing carried food items angers the Ant.
- Ants can enter Ant Hills similar to Bees if there is available space.
- Hurting one Ant alerts the others, similar to Bees, even if they are inside of the Ant Hill.
    - Breaking an Ant Hill angers all Bees in the radius, as well as Bees inside.
- Every Ant Hill contains 1 block that contains an Ant Queen item, though it has no entity of its own.
    - Use a Queen Ant on the ground to plant a new hill `(blocked spots are shown with red particles)` - its Ants spawn tamed to you, and the colony will defend you when you fight nearby.
- Can be caught with a Capture Net.
- They can be healed by feeding them Sugar.

**Added Black Bears**

`These are pretty much just clones of Brown Bears and do not currently have custom behavior because I'm a chud. Feel free to tell me what to change!` 
- Spawns in Forests, Taigas, and Groves.
- Fed with Salmon, Honeycomb, Sweet Berries, and Venison.
    - Cubs can be tamed by feeding, each feed has a 33% chance of taming the cub.
- Neutral by day, attacks Players at night or in darkness, and adults defend nearby cubs.
- Raids Beehives, Sweet Berry Bushes, and Campfires for food; picks up dropped food and sits down to eat it.
    - If a Bear is chasing a Player, but they drop food, the Bear will priotize picking up that food and eating it, pacifying them.
- Hunts Salmon, Bass, and Deer, with a 2-minute kill cooldown.
- Leaves behind Bone Meal after eating Salmon. `(this is a reference to how they  rip off their skin)`
- Adults can be sheared for 1-2 Fur, but it's recommended to do this while they're asleep, otherwise they will aggro.
- Drops 1-2 Fur when killed.

**Added Blobfish**
- Spawns deep in Oceans and stays near the sea floor.
- After 5 seconds near the surface they lose their shape and transform into a pink blob, reforming if they return to the deep and are re-pressurized.
- Avoids Players and Axolotls.
- Attacks Crabs and Snails, with a 2-minute kill cooldown.
- Can be bucketed with a Water Bucket -- turns them into their blob form though.
- Drop 1 Blobfish `(cooked if killed by fire)` when killed. 

**Added Capybaras**
- Spawns in Jungles, Swamps, and Mangrove Swamps in groups of 2-4.
- Tempted, tamed, and bred with Melon Slices -- 33% chance to tame.
    - Only tamed Capybaras can breed, and their babies are born tamed to their owner.
- Completely passive, sleeps through the day, swims well, and takes no fall damage.
- Tamed Capybaras follow you, sit on command, and can be dyed with a collar
- Drops 0-1 Bushmeat `(cooked if killed by fire)` and 0-1 Fur when killed.

**Added Clams**
- Spawns on Ocean floors.
- 5% carry a treasure item - a Capture Net, Ender Pearl, Golden Apple, or Heart of the Sea.
    - Slams shut to guard their treasure when Players approach.
- Snaps at Players who approach them when closed, launching them upward in a burst of bubbles.
- Nearly invulnerable while closed.
- Drops 1 Clam Meat `(cooked if killed by fire)` when killed.

**Added Crabs**
- Spawns on Beaches and Stony Shores with a random variant from 5 colors.
- Tempted, tamed, and bred with Tropical Fish -- 33% chance to tame.
- Hides in their shells when Players come close, unless you sneak or hold Tropical Fish.
    - Pick up dropped swords and tools and fight with them - armed Crabs do not hide in their shells, and will fight back if attacked.
- Tamed Crabs dance near playing Jukeboxes.
- Can be caught with a Capture Net, maintaining their held item.
- Drops 1-2 Crab Meat `(cooked if killed by fire)`, with a 5% chance of a Nautilus Shell in addition.

**Added Desert Scorpions**
- Spawns in Deserts and Badlands.
- Hostile towards Players in darkness `(similar to Spiders)` and hunts Lizards with a 2-minute kill cooldown.
- Venomous - attacks inflict Poison for 6 seconds.
- Healed by feeding them Lizard Tails.
- Can be caught with the Capture Net. 
    - Placed Desert Scorpions never despawn, and do not attack Players.
- 12% chance to drop a Scorpion Poison Gland when killed.

**Added Giant Isopods**
- Spawns on the Ocean floor.
    - Has 2 variants; brown and blue.
- Tempted and bred with any fish items.
- Walks along the seafloor and curls up when Players come within 3 blocks, halving damage taken.
- Can be bucketed with a Water Bucket.
- Occasionally drops Bone Meal when killed.

**Added Great White Sharks**
- Spawns in Warm and Lukewarm Oceans.
- Can be healed with any fish items, but can not be tamed or bred.
- Hunts fish, and attacks Players in the water in low light `(Spider mechanics)`, with a 2-minute kill cooldown.
    - Charges their prey head-on, then retreats and circles back between strikes, swimming faster while aggressive.
- Drops 2–5 Teeth when killed.

**Added Hedgehogs**
- Spawns in Forests, Plains, Meadows, and Taigas.
- Tempted, tamed, and bred with Sweet Berries and Glow Berries -- 50% chance to tame.
- Rolls into a defensive pose when a Player comes within 6 blocks, unless you sneak or hold berries, hurting anything that touches them.
- Immune to fall damage, Cactus, and Sweet Berry Bushes, and fireproof while rolled up.
- Tamed Hedgehogs can be picked up with a Capture Net and thrown as a projectile.
    - The item can be enchanted with Unbreaking, Thorns, Punch, Flame, Looting, and Loyalty.
    - Similar to Wolves, they can be dyed to change the color of their tamed "boots" overlay, or they can be sheared to remove the tame color visual entirely.
- Drops 1 Morsel when killed.

**Added Jellyfish**
- Spawns in Oceans in ~5 different color variants.
- Drifts through the water, stinging anything that touches them.
    - Whales are immune to their sting.
- Can be bucketed with a Water Bucket.
- 20% chance to drop a Slime Ball when killed.

**Added Jungle Scorpions**
- Much larger scorpions that spawn in Jungles in ~2 color variants.
- Hostile toward Players in the darkness `(Spider mechanics)`, hunts Lizards with a 2-minute kill cooldown..
- Attacks inflict Poison for 6 seconds.
- Healed by feeding them Lizard Tails.
- Can not be caught with the Capture Net, unlike Desert Scorpions.
- 12% chance to drop a Scorpion Poison Gland when killed.

**Added Komodo Dragons**
- Spawns in Badlands, Savannas, and Deserts.
- Can be bred with any raw meat, but not tamed.
- Hunts Chickens, Rabbits, Lizards, Snakes, and Boars with a 2-minute kill cooldown.
- Watches Players who linger within 16 blocks and turns hostile after 20 seconds of that Player being within their range.
- Attacks inflict Poison for 5 seconds.
- Basks in the sun during the day.
- Drops 0-1 Hide and 1 Bushmeat `(cooked if killed by fire)` when killed.

**Added Mammoths**
- Spawns in Snowy Plains, Ice Spikes, Snowy Slopes, and Frozen Peaks in herds of 2-3.
- Tempted and bred with Melon Slices.
    - Babies can be tamed by feeding them 5 Cakes. `(a nod to Mo' Creatures)`
- Tamed adults `(after baby grows up)` can be saddled and ridden, and can be given a Chest for a 25 slot inventory with Saddle and Banner slots.
    - Crouch + right-click to open their inventory when not riding.
    - Shears can remove the Chest and/or Saddle.
- Neutral, but avoids Bees.
    - Attacks deal heavy knockback into the air, similar to Iron Golems.
- Drops 2-3 Fat, 2-3 Fur, and 2-3 Mammoth Meat `(cooked if killed by fire)` when killed.

**Added Moles**
- Spawns in Forests, Plains, and Meadows.
- Tempted and bred with Spider Eyes, but can not be tamed.
- Burrows underground when they spot a threat - undead mobs, sprinting Players, or whoever last hurt them, and scurries beneath the surface, leaving trails of dirt mounds.
- Invulnerable while underground, periodically peeking out to check if the coast is clear.
    - They can be damaged when peeking.
    - Wolves can dig Moles out of their mounds, forcing them back out of the ground to attack them.
    - Fire and water force them back to the surface.
- Untamed Wolves attack Moles.
- Drops 1-2 Dirt or 1-2 Fur when killed.

**Added Ostriches**
- Spawn in Savannas.
- Tempted and bred with Seeds. 
    - Babies are tamed instantly with a single Seed
- Buries their head when Players approach, unless you crouch or hold Seeds.
- Breeding produces an egg the mother carries and lays on Sand or Diret. 
    - Ostrich Eggs hatch over time like Turtle Eggs.
- Defends their eggs, attacking Players who come within 10-blocks range of one or breaking it.
- Tamed adults `(after a baby grows up)` can be saddled and ridden, and made to jump.
    - Falling Ostriches slow-fall like Chickens, even with a rider.
    - Shears remove the saddle
- Similar to tamed Wolves, they can be dyed, or Shears can be used to remove their tame visual.
- Drops 1-2 Drumsticks `(cooked if killed by fire)` or 0-2 Feathers when killed.

**Added Piranhas**
- Spawns in Jungle waters and Lush Caves in schools of up to 24.
- The school leader picks the target - Players in the water, Cod, or Salmon, and the whole school follows its lead.
    - Attacking one Piranha makes the entire school aggro on you.
- Can be bucketed with a Water Bucket.
- Drops 1 Piranha `(cooked if killed by fire)` when killed, with a 5% chance of Bone Meal in addition.

**Added Rats**
- Spawn in Forests and Plains.
- Tempted, tamed, and bred with Bread -- 70% chance to tame.
- Can be caught with a Capture Net.
    - Releasing a caught/tamed Rat onto a Chest or other container assigns it as a workstation. The Rat harvests and replants fully grown crops `(including Nether Wart)` within 7 blocks and deposits the produce into the container.
- Can be caught with a Capture Net.
- Drops 0-2 Fur and 1 Morsel when killed.

**Added Rays**
- Spawns in Oceans.
- Passive, but Players who attack it are inflicted with 5 seconds of Poison.
- Can be bucketed with a Water Bucket.
- Drops 1-2 Crab Meat `(cooked if killed by fire)` when killed.

**Added Starfish**
- Spawns on the Ocean floor.
- Can be bucketed with a Water Bucket.
- Drops their placeable block form when killed.

**Added Tigers**
- Spawns in Jungles, Mountains, Savannas, Deserts, Cherry Groves, and Mangrove Swamps.
    - White Tigers in Bamboo Jungles and Cherry Groves, Leopards in Mountains, Badlands, and Deserts, Black Panthers in Mangrove Swamps, and regular Tigers elsewhere
- Can be bred with any meat item.
    - Cubs can be tamed with any meat item, but adults can not be tamed.
- Hunts Boars, Pigs, Deer, Zebras, and Snakes at night, with a 2-minute kill cooldown.
- Drops 1-2 Teeth or 1-2 Fur when killed.

**Added Turkeys**
- Spawns in Forests, Taigas, and Groves.
- Tempted and bred with any Seed items.
- Flees from Players within 6 blocks, unless you crouch or hold Seed items.
- Hunts/kills Ants with a 2-minute kill cooldown.
- Falls slowly like Chickens and takes no fall damage.
- Drops 0-2 Feathers and 1 Drumstick `(cooked if killed by fire)` when killed.

**Added Whales**
- Spawns in Ocean biomes.
- Can be fed/bred with Cod, Salmon, and Crab Meat.
- Immune to Jellyfish and Pufferfish stings.
- Drops 2-5 Fat when killed.

## Changes to Existing Mobs

**Merged all Birds into 1 mob**
- Blue Jay, Cardinal, Robin, Sparrow, Canary, and Finch are now one Bird entity with 8 data-driven variants: American Robin, Blue Jay, Northern Cardinal, Red-Winged Blackbird, Carolina Chickadee, Tufted Titmouse, Steller's Jay, and White-Throated Sparrow
- Variant is chosen by spawn biome, old worlds should migrate automatically
- Wild Birds flee from Players `(unless sneaking)` and fly down to eat dropped Seed items
    - Birds are now tamed by dropped Seed items instead of direct interactions.
- Tamed Birds perch on your head, slow your fall by 40%, and hop off when you crouch
- Can be fed Seeds to heal them, and they can be dyed similar to Wolves or interacted upon with Shears to remove their tame visual.
- Drops 1-2 Feathers when killed.

**Merged Snakes**
- Coral Snakes and Rattlesnakes are now variants of the Snake, old worlds should migrate automatically
    - Coral Snakes spawn in Jungles, Rivers, and Beaches, Rattlesnakes in Deserts, Badlands, and Savannas, and Green Snakes in Forests, Plains, and Swamps
- Can be tamed with Spider Eyes or Rabbit meat.
- Hunts Rabbits, Chickens, Silverfish, Snails, and small Slimes, with a 2-minute kill cooldown.
- Coral Snake and Rattlesnake bites inflict Poison, and Rattlesnakes rattle at players who get within a 4-block radius.
- Drops 0-1 Morsels, with a 40% chance of 1-2 Teeth when killed.

**Revamped Bass**
- Spawns in Swamps, Mangrove Swamps, and Rivers.
- New models, textures, and animations!
- Now comes in three sizes - schools always share one size, and bigger sizes spawn rarer and in smaller groups.
    - Medium Bass hunt small Bass, and large Bass hunt both, swallowing them whole and leaving Bone Meal behind.
    - A medium Bass that eats a small one has a 20% chance to grow into a large Bass.
- Avoids Players, Axolotls, Catfish, and any bigger Bass
- Can be bucketed with a Water Bucket.
- Drops 1 Bass `(cooked if killed by fire)` when killed.

**Revamped Catfish**
- Spawns in Swamps and Mangrove Swamps.
- New model, texture, and animations!
- Hunts Tropical Fish, Cod, Tadpoles, and Bass, with a 2-minute kill cooldown.
- Swallows Bass whole, opening their mouth and chomping down.
- Avoids Players and Axolotls.
- Can be bucketed with a Water Bucket.
- Drops 1 Catfish `(cooked if killed by fire)` when killed.

**Revamped Snails**
- New model, textures, and animations!
- Now spawns in most temperate biomes plus Lush and Dripstone Caves.
- Bred with Beetroot, lays Snail Eggs like Frogspawn.
- Hides in their shell when Players come within 5 blocks, reducing damage taken.
- Can be dyed any color!
- Are now picked up with Capture Nets instead of Buckets.
- Periodically produce Slime Balls, like Chickens laying Eggs.
- Drops 1 Snail Shell and 0-1 Slime Balls when killed.
    - Snail Shells now have a 100% drop chance!

**Nocturnal Hostility**
- Bears now attack Players at night or in darkness, like Spiders do.

**Taming / Pets**
- Baby Lions can be tamed by feeding them meat items.
- Baby Hippos can be tamed with Melons.
- Baby Elephants are tamed by feeding 5 Cakes.
    - Tamed adults can be saddled, ridden, and given a Chest for a 25 slot inventory.
- Ducks can now be tamed with any Fish items.
- Taming a Lizard or Tortoise increases its max health.
- Pets no longer attack your other pets, and won't go after Creepers or other Players' pets when defending you.

**Alligators**
- Now swims smoothly underwater instead of only on the surface.
- Attacks Players in the water, at night, or while defending eggs, not just Players in water.

**Vanilla Mob Changes**
- Parrots no longer dismount your shoulder when on it - you crouch to dismount them.
    - With 2 Parrots on either shoulder, and a Bird on your head, you get slow, Creative-like flight.
- Untamed Wolves target Moles, and can dig them out of the ground when they are burrowing.
- Zombies seek out and trample Ostrich Eggs.
- Foxes and Rabbits now spawn in all Forests.

**Other behavior**
- All mobs now have a 2-minute cooldown after killing another mob instead of killing everything in sight.
- Vultures perch atop Cacti.

## New Items and Blocks `(that are for existing things)`

- Added the Whistle, use on tamed mobs to toggle them between following and wandering.
- Added the Knapsack, which can be used to capture and re-release any baby mob.
- Added Duckling Buckets.
- Capture Net now has swing effects!

## Misc. Changes

- Removed GeckoLib. Hasta la vista :-)
- Riders' rotations are now synced on mobs like Elephants and Giraffes instead of their bodies rotating while the Player stayed stiff
- Elephants and Giraffes use inverse kinematics for their legs `(thanks Alex)`
- Removed aquatic mobs' shadows
- Fixed sheared Bears rendering incorrectly

## Compatibility

- Mob variants are now data-driven, even if the mob does not have variants by default.
- Added Field Guide integration
- Added JustEnoughBreeding support for breeding info in EMI and JEI
- Added BucketLib support and Terralith spawn compatibility
- Added Alex's Mobs predator and prey compatibility `(despite it not being ported to 1.21.1 yet)`
- Bundled Essential partner integration

## Fixes

- Fixed Butterflies and Caterpillars never despawning
- Fixed a crash when a Bears' food items became empty while eating
- Fixed Naturalist fish buckets not counting toward the Tactical Fishing advancement
