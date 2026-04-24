package com.starfish_studios.naturalist.server.item;

import com.starfish_studios.naturalist.server.entity.mob.Snail;
import com.starfish_studios.naturalist.registry.NaturalistEntityTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class NoFluidMobBucketWithVariantsItem extends MobBucketItem {
    private final @NotNull Supplier<? extends EntityType<?>> typeSup;

    private EntityType<?> type() {
        return this.typeSup.get();
    }
    public NoFluidMobBucketWithVariantsItem(Supplier<? extends EntityType<?>> entitySupplier, Supplier<? extends Fluid> fluidSupplier, Supplier<? extends SoundEvent> soundSupplier, @NotNull Properties properties, int colorCount) {
        super(entitySupplier.get(), fluidSupplier.get(), soundSupplier.get(), properties);
        this.typeSup = entitySupplier;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        if (this.type() == NaturalistEntityTypes.SNAIL.get()) {
            CompoundTag compoundnbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            if (compoundnbt.contains("Color", 3)) {
                Snail.Color color = Snail.Color.getTypeById(compoundnbt.getInt("Color"));
                tooltip.add((Component.translatable(String.format("item.minecraft.firework_star.%s", color.toString().toLowerCase())).withStyle(ChatFormatting.GRAY)));
            }
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (blockhitresult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemstack);
        } else if (blockhitresult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemstack);
        } else {
            BlockPos pos = blockhitresult.getBlockPos();
            Direction direction = blockhitresult.getDirection();
            BlockPos blockpos1 = pos.relative(direction);
            if (level.mayInteract(player, pos) && player.mayUseItemAt(blockpos1, direction, itemstack)) {
                this.checkExtraContent(player, level, itemstack, pos);
                this.playEmptySound(player, level, pos);
                player.awardStat(Stats.ITEM_USED.get(this));
                return InteractionResultHolder.sidedSuccess(getEmptySuccessItem(itemstack, player), level.isClientSide());
            } else {
                return InteractionResultHolder.fail(itemstack);
            }
        }
    }
}
