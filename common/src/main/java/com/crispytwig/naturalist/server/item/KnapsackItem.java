package com.crispytwig.naturalist.server.item;

import com.crispytwig.naturalist.registry.NaturalistRegistry;
import com.crispytwig.naturalist.registry.NaturalistSoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.storage.TagValueOutput;

public class KnapsackItem extends Item {
    public KnapsackItem(Properties properties) {
        super(properties);
    }

    private static CompoundTag entityTag(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static boolean isFilled(ItemStack stack) {
        return entityTag(stack).contains("id");
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack) && !stack.isEmpty()) {
            ItemEntity item = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack);
            item.setNoPickUpDelay();
            item.setTarget(player.getUUID());
            player.level().addFreshEntity(item);
        }
    }

    public static boolean isCapturable(@NotNull LivingEntity target) {
        if (!(target instanceof Mob) || !target.isAlive() || target.isPassenger()) {
            return false;
        }
        if (target instanceof AgeableMob ageable) {
            return ageable.getAge() < 0;
        }
        return target.isBaby();
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity target, @NotNull InteractionHand hand) {
        if (isFilled(stack) || !isCapturable(target)) {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        Mob mob = (Mob) target;
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, mob.registryAccess());
        mob.save(output);
        CompoundTag entityTag = output.buildResult();

        ItemStack filled = new ItemStack(NaturalistRegistry.KNAPSACK.get());
        filled.set(DataComponents.CUSTOM_DATA, CustomData.of(entityTag));

        BugNetItem.swing(player.level(), player);
        BugNetItem.playCaughtEffects(player.level(), mob);
        mob.discard();
        player.swing(hand);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), NaturalistSoundEvents.KNAPSACK_PICKUP.get(), SoundSource.NEUTRAL, 0.6F, 1.0F);

        if (stack.getCount() <= 1) {
            player.setItemInHand(hand, filled);
        } else {
            stack.shrink(1);
            giveOrDrop(player, filled);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        CompoundTag tag = entityTag(stack);
        if (!tag.contains("id")) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel) {
            BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
            Entity entity = EntityType.loadEntityRecursive(tag, serverLevel, EntitySpawnReason.LOAD, e -> {
                e.snapTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, e.getYRot(), e.getXRot());
                return e;
            });
            if (entity != null) {
                serverLevel.addFreshEntity(entity);
            }
            level.playSound(null, pos, NaturalistSoundEvents.KNAPSACK_PLACE.get(), SoundSource.NEUTRAL, 0.6F, 1.0F);

            Player player = context.getPlayer();
            ItemStack empty = new ItemStack(NaturalistRegistry.KNAPSACK.get());
            if (player != null) {
                if (stack.getCount() <= 1) {
                    player.setItemInHand(context.getHand(), empty);
                } else {
                    stack.shrink(1);
                    giveOrDrop(player, empty);
                }
            } else {
                stack.shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull TooltipDisplay display, @NotNull java.util.function.Consumer<Component> tooltip, @NotNull TooltipFlag flag) {
        CompoundTag tag = entityTag(stack);
        if (!tag.contains("id")) {
            return;
        }
        Component label = null;
        String entityId = tag.getStringOr("id", "");
        Identifier parsedId = Identifier.tryParse(entityId);
        EntityType<?> type = parsedId == null ? null : BuiltInRegistries.ENTITY_TYPE.getValue(parsedId);
        if (type != null) {
            label = type.getDescription();
        }
        if (label != null) {
            tooltip.accept(label.copy().withStyle(ChatFormatting.GRAY));
        }
    }
}
