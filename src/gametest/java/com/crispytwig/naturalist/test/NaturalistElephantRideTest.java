package com.crispytwig.naturalist.test;

import com.crispytwig.naturalist.server.entity.mob.Elephant;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CameraType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

/** Verifies Naturalist's real tame/saddled elephant mount path and rider rendering. */
@SuppressWarnings("UnstableApiUsage")
final class NaturalistElephantRideTest {
    private NaturalistElephantRideTest() {
    }

    static void verify(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runCommand("/tp @a 8 100 -5 0 5");

        int elephantId = world.getServer().computeOnServer(server -> {
            var level = server.overworld();
            var players = server.getPlayerList().getPlayers();
            require(!players.isEmpty(), "No server player available for elephant riding test");
            var player = players.getFirst();

            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(
                    Identifier.fromNamespaceAndPath("naturalist", "elephant"));
            require(type != null, "Missing Naturalist elephant type");
            Entity entity = type.create(level, EntitySpawnReason.COMMAND);
            require(entity instanceof Elephant, "Could not create Naturalist elephant");
            Elephant elephant = (Elephant) entity;

            elephant.setAge(0);
            elephant.tame(player);
            elephant.setSaddled(true);
            elephant.setPersistenceRequired();
            elephant.snapTo(8.0D, 100.0D, -3.5D, 180.0F, 0.0F);
            require(level.addFreshEntity(elephant), "Could not add riding-test elephant");

            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            elephant.mobInteract(player, InteractionHand.MAIN_HAND);
            require(player.getVehicle() == elephant,
                    "Tame saddled adult elephant did not mount its owner after empty-hand interaction");
            require(elephant.getControllingPassenger() == player,
                    "Mounted elephant did not expose its owner as controlling passenger");
            return elephant.getId();
        });

        context.waitFor(client -> client.player != null
                && client.player.getVehicle() instanceof Elephant
                && client.level != null
                && client.level.getEntity(elephantId) instanceof Elephant, 120);

        CameraType previousCamera = context.computeOnClient(client -> client.options.getCameraType());
        context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_BACK));
        try {
            context.waitTicks(30);
            world.getConnection().waitForChunksRender();
            context.takeScreenshot("naturalist-elephant-rider");
        } finally {
            context.runOnClient(client -> client.options.setCameraType(previousCamera));
        }

        world.getServer().runOnServer(server -> {
            var players = server.getPlayerList().getPlayers();
            require(!players.isEmpty(), "Server player disappeared during elephant riding test");
            var player = players.getFirst();
            require(player.getVehicle() instanceof Elephant,
                    "Player dismounted before elephant riding render verification completed");
            Entity elephant = player.getVehicle();
            player.stopRiding();
            if (elephant != null) {
                elephant.discard();
            }
        });

        System.out.println("NATURALIST_ELEPHANT_RIDE: mounted and rendered");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
