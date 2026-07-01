package com.crispytwig.naturalist.server.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("unused")
public class NaturalistBucketItem extends MobBucketItem {
    private final boolean noFluid;
    private final boolean allowMidWater;
    private final @Nullable String tooltipPrefix;
    private final @Nullable String[] variantNames;

    public NaturalistBucketItem(EntityType<?> entityType, Fluid content, SoundEvent emptySound, Properties properties) {
        this(entityType, content, emptySound, properties, true, null, null);
    }

    public NaturalistBucketItem(EntityType<?> entityType, Fluid content, SoundEvent emptySound, Properties properties, boolean allowMidWater, @Nullable String tooltipPrefix, @Nullable String[] variantNames) {
        super(entityType, content, emptySound, properties);
        this.noFluid = content == Fluids.EMPTY;
        this.allowMidWater = allowMidWater;
        this.tooltipPrefix = tooltipPrefix;
        this.variantNames = variantNames;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.noFluid) {
            BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
            if (hit.getType() != HitResult.Type.BLOCK) {
                return InteractionResultHolder.pass(stack);
            }
            BlockPos pos = hit.getBlockPos();
            Direction direction = hit.getDirection();
            if (!level.mayInteract(player, pos) || !player.mayUseItemAt(pos.relative(direction), direction, stack)) {
                return InteractionResultHolder.fail(stack);
            }
            return release(level, player, stack, pos);
        }
        if (this.allowMidWater) {
            BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
            if (hit.getType() == HitResult.Type.BLOCK && level.getFluidState(hit.getBlockPos()).is(FluidTags.WATER)) {
                return release(level, player, stack, hit.getBlockPos());
            }
        }
        return super.use(level, player, hand);
    }

    private InteractionResultHolder<ItemStack> release(Level level, Player player, ItemStack stack, BlockPos pos) {
        this.checkExtraContent(player, level, stack, pos);
        this.playEmptySound(player, level, pos);
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(getEmptySuccessItem(stack, player), level.isClientSide());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (this.variantNames == null || this.tooltipPrefix == null) {
            return;
        }
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains("Variant")) {
            int variant = Math.floorMod(tag.getInt("Variant"), this.variantNames.length);
            tooltip.add(Component.translatable(this.tooltipPrefix + this.variantNames[variant]).withStyle(ChatFormatting.GRAY));
        }
    }
}
