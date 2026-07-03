package com.crispytwig.naturalist.server.entity.mob;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class WhalePart extends Entity {
    public static final double[] PART_Z = {4.0D, 1.5D, -1.0D, -3.5D, -5.3D};
    public static final float[][] SIZES = {{2.2F, 2.2F}, {3.0F, 2.5F}, {3.0F, 2.5F}, {2.2F, 2.0F}, {1.4F, 1.2F}};
    public static final int PART_COUNT = PART_Z.length;

    private final Whale parent;
    private final EntityDimensions baseSize;
    private float scale = 1.0F;

    public WhalePart(Whale parent, int index) {
        super(parent.getType(), parent.level());
        this.parent = parent;
        this.baseSize = EntityDimensions.scalable(SIZES[index][0], SIZES[index][1]);
        this.refreshDimensions();
    }

    public Whale getParent() {
        return this.parent;
    }

    public void updateScale(float newScale) {
        if (newScale != this.scale) {
            this.scale = newScale;
            this.refreshDimensions();
        }
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) {
        return this.baseSize.scale(this.scale);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean is(@NotNull Entity entity) {
        return this == entity || this.parent == entity;
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        return !this.isInvulnerableTo(source) && this.parent.hurt(source, amount);
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        return this.parent.interact(player, hand);
    }

    @Override
    public @NotNull ItemStack getPickResult() {
        return this.parent.getPickResult();
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket(@NotNull ServerEntity serverEntity) {
        throw new UnsupportedOperationException();
    }
}
