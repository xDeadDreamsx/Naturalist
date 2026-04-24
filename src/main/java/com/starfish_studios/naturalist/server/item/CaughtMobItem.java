package com.starfish_studios.naturalist.server.item;

import com.starfish_studios.naturalist.server.entity.mob.Butterfly;
import com.starfish_studios.naturalist.server.entity.base.Catchable;
import com.starfish_studios.naturalist.registry.NaturalistEntityTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class CaughtMobItem extends NoFluidMobBucketItem {
    private final Supplier<? extends EntityType<?>> typeSup;

    private EntityType<?> type() {
        return this.typeSup.get();
    }

    public CaughtMobItem(Supplier<? extends EntityType<?>> entitySupplier, Supplier<? extends Fluid> fluidSupplier, Supplier<? extends SoundEvent> soundSupplier, Properties properties) {
        super(entitySupplier.get(), fluidSupplier.get(), soundSupplier.get(), properties);
        this.typeSup = entitySupplier;
    }

    @SuppressWarnings("unused")
    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        if (this.type() == NaturalistEntityTypes.BUTTERFLY.get()) {
            CompoundTag compoundnbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            if (compoundnbt.contains("Variant", 3)) {
                Butterfly.Variant variant = Butterfly.Variant.getTypeById(compoundnbt.getInt("Variant"));
                tooltip.add((Component.translatable(String.format("tooltip.naturalist.%s", variant.toString().toLowerCase())).withStyle(ChatFormatting.GRAY)));
            }
        }
    }

    private void spawn(ServerLevel serverLevel, ItemStack itemStack, BlockPos pos) {
        Entity entity = this.type().spawn(serverLevel, itemStack, null, pos, MobSpawnType.BUCKET, true, false);
        if (entity instanceof Catchable catchable) {
            CompoundTag tag = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            catchable.loadFromHandTag(tag);
            catchable.setFromHand(true);
        }

    }

    @Override
    public void checkExtraContent(@Nullable Player player, @NotNull Level level, @NotNull ItemStack containerStack, @NotNull BlockPos pos) {
        if (level instanceof ServerLevel) {
            this.spawn((ServerLevel)level, containerStack, pos);
            level.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
        }

    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
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

    public static @NotNull ItemStack getEmptySuccessItem(@NotNull ItemStack bucketStack, Player player) {
        return !player.getAbilities().instabuild ? new ItemStack(Items.AIR) : bucketStack;
    }
}
