package com.crispytwig.naturalist.test;

import com.crispytwig.naturalist.registry.NaturalistEntityTypes;
import com.crispytwig.naturalist.registry.NaturalistRegistry;
import com.crispytwig.naturalist.server.entity.mob.Hedgehog;
import com.crispytwig.naturalist.server.entity.mob.Snail;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.AABB;

/**
 * Exercises the interaction/state-transfer paths that changed most during the 26.2 port:
 * vanilla dye components plus Naturalist capture/release data for catchable and tame animals.
 */
@SuppressWarnings("UnstableApiUsage")
final class NaturalistInteractionRegressionTest {
    private NaturalistInteractionRegressionTest() {
    }

    static void verify(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runCommand("/fill -6 159 -6 6 159 6 minecraft:stone");
        world.getServer().runCommand("/tp @a 0 160 0 0 10");

        world.getServer().runOnServer(server -> {
            var level = server.overworld();
            var players = server.getPlayerList().getPlayers();
            require(!players.isEmpty(), "No server player available for interaction regression tests");
            Player player = players.getFirst();

            verifySnailDyeAndCapture(level, player);
            verifyHedgehogTameCapture(level, player);
        });

        context.waitTicks(10);
        System.out.println("NATURALIST_INTERACTION_REGRESSIONS: snail dye/capture and hedgehog tame capture verified");
    }

    private static void verifySnailDyeAndCapture(net.minecraft.server.level.ServerLevel level, Player player) {
        Snail snail = NaturalistEntityTypes.SNAIL.get().create(level, EntitySpawnReason.COMMAND);
        require(snail != null, "Could not create Naturalist snail");
        snail.setAge(0);
        snail.setPersistenceRequired();
        snail.setHealth(2.0F);
        snail.snapTo(-2.0D, 160.0D, 0.0D, 0.0F, 0.0F);
        require(level.addFreshEntity(snail), "Could not add snail interaction-test entity");

        ItemStack redDye = new ItemStack(DyeItem.byColor(DyeColor.RED));
        player.setItemInHand(InteractionHand.MAIN_HAND, redDye);
        snail.mobInteract(player, InteractionHand.MAIN_HAND);
        require(snail.getColor() == DyeColor.RED,
                "Red vanilla dye did not recolor a Naturalist snail through the 26.2 interaction path");

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(NaturalistRegistry.CAPTURE_NET.get()));
        snail.mobInteract(player, InteractionHand.MAIN_HAND);
        require(snail.isRemoved(), "Capture net did not remove the captured snail");

        ItemStack captured = findInventoryItem(player, NaturalistRegistry.SNAIL.get());
        require(!captured.isEmpty(), "Captured snail item was not added to the player inventory");
        CompoundTag tag = captured.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        require(tag.getIntOr("Color", -1) == Snail.Color.RED.getId(),
                "Captured snail item lost its dyed color");
        require(Math.abs(tag.getFloatOr("Health", -1.0F) - 2.0F) < 0.01F,
                "Captured snail item lost its health state");

        BlockPos releasePos = new BlockPos(-2, 160, 3);
        NaturalistRegistry.SNAIL.get().checkExtraContent(player, level, captured, releasePos);
        Snail released = level.getEntitiesOfClass(Snail.class, new AABB(releasePos).inflate(2.0D)).stream()
                .filter(entity -> entity != snail)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Releasing the captured snail did not spawn a snail"));
        require(released.getColor() == DyeColor.RED, "Released snail did not restore its dyed color");
        require(released.fromHand(), "Released snail was not marked as originating from a captured item");
        require(Math.abs(released.getHealth() - 2.0F) < 0.01F, "Released snail did not restore its health");
        released.discard();
    }

    private static void verifyHedgehogTameCapture(net.minecraft.server.level.ServerLevel level, Player player) {
        Hedgehog hedgehog = NaturalistEntityTypes.HEDGEHOG.get().create(level, EntitySpawnReason.COMMAND);
        require(hedgehog != null, "Could not create Naturalist hedgehog");
        hedgehog.setAge(0);
        hedgehog.tame(player);
        hedgehog.setFollowingOwner(false);
        hedgehog.setOrderedToSit(true);
        hedgehog.setDyeColor(DyeColor.BLUE);
        hedgehog.setHealth(5.0F);
        hedgehog.setPersistenceRequired();
        hedgehog.snapTo(2.0D, 160.0D, 0.0D, 0.0F, 0.0F);
        require(level.addFreshEntity(hedgehog), "Could not add hedgehog interaction-test entity");

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(NaturalistRegistry.CAPTURE_NET.get()));
        hedgehog.mobInteract(player, InteractionHand.MAIN_HAND);
        require(hedgehog.isRemoved(), "Capture net did not remove the captured hedgehog");

        ItemStack captured = findInventoryItem(player, NaturalistRegistry.HEDGEHOG.get());
        require(!captured.isEmpty(), "Captured hedgehog item was not added to the player inventory");
        CompoundTag tag = captured.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        require(tag.getBooleanOr("Tame", false), "Captured hedgehog item lost its tame flag");
        require(tag.getIntArray("Owner").map(owner -> owner.length == 4).orElse(false),
                "Captured hedgehog item lost its owner reference");
        require(!tag.getBooleanOr("FollowingOwner", true), "Captured hedgehog item lost its follow state");
        require(tag.getBooleanOr("Sitting", false), "Captured hedgehog item lost its sitting state");
        require(tag.getIntOr("DyeColor", -1) == DyeColor.BLUE.getId(), "Captured hedgehog item lost its dye color");
        require(Math.abs(tag.getFloatOr("Health", -1.0F) - 5.0F) < 0.01F,
                "Captured hedgehog item lost its health state");

        BlockPos releasePos = new BlockPos(2, 160, 3);
        NaturalistRegistry.HEDGEHOG.get().checkExtraContent(player, level, captured, releasePos);
        Hedgehog released = level.getEntitiesOfClass(Hedgehog.class, new AABB(releasePos).inflate(2.0D)).stream()
                .filter(entity -> entity != hedgehog)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Releasing the captured hedgehog did not spawn a hedgehog"));
        require(released.isTame(), "Released hedgehog lost its tame state");
        require(released.isOwnedBy(player), "Released hedgehog lost its owner");
        require(!released.isFollowingOwner(), "Released hedgehog lost its disabled follow state");
        require(released.isOrderedToSit(), "Released hedgehog lost its sitting state");
        require(released.getDyeColor() == DyeColor.BLUE, "Released hedgehog lost its dye color");
        require(Math.abs(released.getHealth() - 5.0F) < 0.01F, "Released hedgehog did not restore its health");
        require(released.fromHand(), "Released hedgehog was not marked as originating from a captured item");
        released.discard();
    }

    private static ItemStack findInventoryItem(Player player, Item item) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(item)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
