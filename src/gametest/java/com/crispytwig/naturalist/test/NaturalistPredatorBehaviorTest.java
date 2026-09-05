package com.crispytwig.naturalist.test;

import com.crispytwig.naturalist.server.entity.base.HuntingAnimal;
import com.crispytwig.naturalist.server.entity.base.SleepingAnimal;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

/** Regression coverage for Naturalist predator target acquisition on Minecraft 26.2. */
@SuppressWarnings("UnstableApiUsage")
final class NaturalistPredatorBehaviorTest {
    private static final double LAND_PREDATOR_X = 11.0D;
    private static final double LAND_PREY_X = 13.0D;
    private static final double LAND_Y = 100.0D;
    private static final double LAND_Z = -11.0D;

    private static final double WATER_PREDATOR_X = 22.0D;
    private static final double WATER_PREY_X = 34.0D;
    private static final double WATER_Y = 101.0D;
    private static final double WATER_Z = -9.0D;

    private NaturalistPredatorBehaviorTest() {
    }

    static void verifyLandPredatorsHunt(ClientGameTestContext context, TestSingleplayerContext world) {
        // A predator that finishes its prey early may otherwise acquire the GameTest player before
        // the fixed observation window ends. That killed Player0 in run #269 and shut down the
        // singleplayer server before the next predator could be checked. Creative mode keeps the
        // observer out of target selection without changing animal-to-animal target acquisition.
        world.getServer().runCommand("/gamemode creative @a");
        world.getServer().runCommand("/time set 1000");
        verifyHunt(context, world, "naturalist:snake", "minecraft:chicken", 100,
                LAND_PREDATOR_X, LAND_PREY_X, LAND_Y, LAND_Z);
        verifyHunt(context, world, "naturalist:komodo_dragon", "minecraft:chicken", 100,
                LAND_PREDATOR_X, LAND_PREY_X, LAND_Y, LAND_Z);
        verifyHunt(context, world, "naturalist:alligator", "naturalist:duck", 120,
                LAND_PREDATOR_X, LAND_PREY_X, LAND_Y, LAND_Z);
        verifyHunt(context, world, "naturalist:bear", "naturalist:deer", 140,
                LAND_PREDATOR_X, LAND_PREY_X, LAND_Y, LAND_Z);

        // Lions intentionally hunt at night in the original Naturalist behaviour.
        world.getServer().runCommand("/time set 14000");
        verifyHunt(context, world, "naturalist:lion", "minecraft:horse", 160,
                LAND_PREDATOR_X, LAND_PREY_X, LAND_Y, LAND_Z);
        world.getServer().runCommand("/time set 1000");

        System.out.println("NATURALIST_PREDATOR_BEHAVIOR: land predator hunting verified");
    }

    static void verifyWaterPredatorsHunt(ClientGameTestContext context, TestSingleplayerContext world) {
        // Sealed pool: stone shell from y=99..105, water-filled interior y=100..104. This keeps
        // vanilla fish and Naturalist swimmers inside a deterministic navigation volume.
        world.getServer().runCommand("/fill 20 99 -16 38 105 -2 minecraft:stone");
        world.getServer().runCommand("/fill 21 100 -15 37 104 -3 minecraft:water");

        verifyHunt(context, world, "naturalist:anglerfish", "minecraft:cod", 160,
                WATER_PREDATOR_X, WATER_PREY_X, WATER_Y, WATER_Z);
        verifyHunt(context, world, "naturalist:piranha", "minecraft:cod", 160,
                WATER_PREDATOR_X, WATER_PREY_X, WATER_Y, WATER_Z);
        // Catfish use the original delayed-swallow path: after the bite, the bass is devoured a few
        // ticks later instead of taking ordinary melee damage. A discarded prey counts as success.
        verifyHunt(context, world, "naturalist:catfish", "naturalist:bass", 180,
                WATER_PREDATOR_X, WATER_PREY_X, WATER_Y, WATER_Z);
        // The shark's custom attack behaviour is best exercised with room to approach its target.
        verifyHunt(context, world, "naturalist:great_white_shark", "minecraft:cod", 240,
                WATER_PREDATOR_X, WATER_PREY_X, WATER_Y, WATER_Z);

        System.out.println("NATURALIST_PREDATOR_BEHAVIOR: water predator hunting verified");
    }

    private static void verifyHunt(ClientGameTestContext context, TestSingleplayerContext world,
                                   String predatorId, String preyId, int timeoutTicks,
                                   double predatorX, double preyX, double y, double z) {
        int[] ids = world.getServer().computeOnServer(server -> {
            var level = server.overworld();
            EntityType<?> predatorType = getEntityType(predatorId);
            EntityType<?> preyType = getEntityType(preyId);

            Entity predatorEntity = predatorType.create(level, EntitySpawnReason.COMMAND);
            Entity preyEntity = preyType.create(level, EntitySpawnReason.COMMAND);
            require(predatorEntity instanceof Mob, "Could not create predator " + predatorId);
            require(preyEntity instanceof LivingEntity, "Could not create prey " + preyId);

            Mob predator = (Mob) predatorEntity;
            LivingEntity prey = (LivingEntity) preyEntity;
            if (predator instanceof AgeableMob ageable) {
                ageable.setAge(0);
            }
            if (predator instanceof HuntingAnimal hunter) {
                hunter.setHuntingCooldown(0);
            }
            if (predator instanceof SleepingAnimal sleeper) {
                sleeper.setSleeping(false);
            }
            predator.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            predator.setPersistenceRequired();
            predator.snapTo(predatorX, y, z, -90.0F, 0.0F);

            if (prey instanceof AgeableMob ageablePrey) {
                ageablePrey.setAge(0);
            }
            if (prey instanceof Mob preyMob) {
                preyMob.setNoAi(true);
                preyMob.setPersistenceRequired();
            }
            prey.snapTo(preyX, y, z, 90.0F, 0.0F);

            require(level.addFreshEntity(prey), "Could not add prey " + preyId);
            require(level.addFreshEntity(predator), "Could not add predator " + predatorId);
            return new int[]{predator.getId(), prey.getId()};
        });

        context.waitTicks(timeoutTicks);

        world.getServer().runOnServer(server -> {
            Entity predatorEntity = server.overworld().getEntity(ids[0]);
            Entity preyEntity = server.overworld().getEntity(ids[1]);
            require(predatorEntity instanceof Mob, predatorId + " disappeared before hunting check");

            // Persistence prevents natural despawning during this short test, so a missing prey
            // entity means the predator killed/devoured it. Otherwise it must have taken damage.
            if (preyEntity instanceof LivingEntity prey) {
                require(prey.getHealth() < prey.getMaxHealth(),
                        predatorId + " never attacked nearby prey " + preyId + " within " + timeoutTicks + " ticks");
            }

            predatorEntity.discard();
            if (preyEntity != null) {
                preyEntity.discard();
            }
        });
        System.out.println("NATURALIST_PREDATOR_BEHAVIOR: " + predatorId + " hunted " + preyId);
    }

    private static EntityType<?> getEntityType(String id) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(id));
        require(type != null, "Missing entity type " + id);
        return type;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
