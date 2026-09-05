package com.crispytwig.naturalist.test;

import com.crispytwig.naturalist.server.entity.mob.Snake;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
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
    private NaturalistPredatorBehaviorTest() {
    }

    static void verifySnakeHuntsChicken(ClientGameTestContext context, TestSingleplayerContext world) {
        int[] ids = world.getServer().computeOnServer(server -> {
            var level = server.overworld();
            EntityType<?> snakeType = BuiltInRegistries.ENTITY_TYPE.getValue(
                    Identifier.fromNamespaceAndPath("naturalist", "snake"));
            EntityType<?> chickenType = BuiltInRegistries.ENTITY_TYPE.getValue(
                    Identifier.fromNamespaceAndPath("minecraft", "chicken"));
            require(snakeType != null, "Missing Naturalist snake type");
            require(chickenType != null, "Missing vanilla chicken type");

            Entity snakeEntity = snakeType.create(level, EntitySpawnReason.COMMAND);
            Entity chickenEntity = chickenType.create(level, EntitySpawnReason.COMMAND);
            require(snakeEntity instanceof Snake, "Could not create Naturalist snake");
            require(chickenEntity instanceof Mob, "Could not create chicken mob");

            Snake snake = (Snake) snakeEntity;
            Mob chicken = (Mob) chickenEntity;
            snake.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            snake.setSleeping(false);
            snake.setHuntingCooldown(0);
            snake.snapTo(11.0D, 100.0D, -11.0D, 0.0F, 0.0F);
            chicken.snapTo(13.0D, 100.0D, -11.0D, 0.0F, 0.0F);
            chicken.setNoAi(true);
            chicken.setPersistenceRequired();
            snake.setPersistenceRequired();

            require(level.addFreshEntity(chicken), "Could not add chicken to test world");
            require(level.addFreshEntity(snake), "Could not add snake to test world");
            return new int[]{snake.getId(), chicken.getId()};
        });

        context.waitTicks(80);

        world.getServer().runOnServer(server -> {
            Entity snakeEntity = server.overworld().getEntity(ids[0]);
            Entity chickenEntity = server.overworld().getEntity(ids[1]);
            require(snakeEntity instanceof Snake, "Snake disappeared before predator regression check");

            // A successful bite is the strongest proof that target acquisition survived aiStep.
            // The chicken may already be dead/discarded after two bites, which is also success.
            if (chickenEntity instanceof LivingEntity chicken) {
                require(chicken.getHealth() < chicken.getMaxHealth(),
                        "Snake never attacked its nearby chicken prey within 80 ticks");
            }
        });
        System.out.println("NATURALIST_PREDATOR_BEHAVIOR: snake hunted chicken");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
