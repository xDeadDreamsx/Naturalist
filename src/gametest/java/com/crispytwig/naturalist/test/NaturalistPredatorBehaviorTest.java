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
    private static final double PREDATOR_X = 11.0D;
    private static final double PREY_X = 13.0D;
    private static final double TEST_Y = 100.0D;
    private static final double TEST_Z = -11.0D;

    private NaturalistPredatorBehaviorTest() {
    }

    static void verifyLandPredatorsHunt(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runCommand("/time set 1000");
        verifyHunt(context, world, "naturalist:snake", "minecraft:chicken", 100);
        verifyHunt(context, world, "naturalist:komodo_dragon", "minecraft:chicken", 100);
        verifyHunt(context, world, "naturalist:alligator", "naturalist:duck", 120);
        verifyHunt(context, world, "naturalist:bear", "naturalist:deer", 140);

        // Lions intentionally hunt at night in the original Naturalist behaviour.
        world.getServer().runCommand("/time set 14000");
        verifyHunt(context, world, "naturalist:lion", "minecraft:horse", 160);
        world.getServer().runCommand("/time set 1000");

        System.out.println("NATURALIST_PREDATOR_BEHAVIOR: land predator hunting verified");
    }

    private static void verifyHunt(ClientGameTestContext context, TestSingleplayerContext world,
                                   String predatorId, String preyId, int timeoutTicks) {
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
            predator.snapTo(PREDATOR_X, TEST_Y, TEST_Z, -90.0F, 0.0F);

            if (prey instanceof AgeableMob ageablePrey) {
                ageablePrey.setAge(0);
            }
            if (prey instanceof Mob preyMob) {
                preyMob.setNoAi(true);
                preyMob.setPersistenceRequired();
            }
            prey.snapTo(PREY_X, TEST_Y, TEST_Z, 90.0F, 0.0F);

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
            // entity means the predator killed it. Otherwise it must have taken real damage.
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
