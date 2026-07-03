package com.crispytwig.naturalist.server.block.entity;

import com.crispytwig.naturalist.registry.NaturalistBlockEntities;
import com.crispytwig.naturalist.server.block.AntHillBlock;
import com.crispytwig.naturalist.server.entity.base.PetTargeting;
import com.crispytwig.naturalist.server.entity.mob.Ant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class AntHillBlockEntity extends BlockEntity {
    private static final int POLL_INTERVAL = 10;
    private static final double DEFEND_RANGE_SQR = 24.0D * 24.0D;

    @Nullable
    private UUID owner;

    public AntHillBlockEntity(BlockPos pos, BlockState state) {
        super(NaturalistBlockEntities.ANT_HILL.get(), pos, state);
    }

    @Nullable
    public UUID getOwner() {
        return this.owner;
    }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
        this.setChanged();
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.owner != null) {
            tag.putUUID("Owner", this.owner);
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        this.owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AntHillBlockEntity hill) {
        if (hill.owner == null || (level.getGameTime() + pos.asLong()) % POLL_INTERVAL != 0 || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        int workers = state.getValue(AntHillBlock.WORKERS);
        if (workers <= 0) {
            return;
        }
        Player player = level.getPlayerByUUID(hill.owner);
        double centerX = pos.getX() + 0.5D;
        double centerY = pos.getY() + 0.5D;
        double centerZ = pos.getZ() + 0.5D;
        if (player == null || player.distanceToSqr(centerX, centerY, centerZ) > DEFEND_RANGE_SQR) {
            return;
        }
        LivingEntity threat = null;
        if (player.tickCount - player.getLastHurtByMobTimestamp() < POLL_INTERVAL) {
            threat = player.getLastHurtByMob();
        } else if (player.tickCount - player.getLastHurtMobTimestamp() < POLL_INTERVAL) {
            threat = player.getLastHurtMob();
        }
        if (threat == null || !threat.isAlive()
                || !PetTargeting.wantsToAttack(threat, player)
                || threat.distanceToSqr(centerX, centerY, centerZ) > DEFEND_RANGE_SQR) {
            return;
        }
        BlockPos releasePos = AntHillBlock.findReleasePos(serverLevel, pos, serverLevel.getRandom());
        if (releasePos == null) {
            return;
        }
        serverLevel.setBlock(pos, state.setValue(AntHillBlock.WORKERS, 0), 3);
        AntHillBlock.playEnterLeaveEffects(serverLevel, pos, serverLevel.getRandom());
        for (int i = 0; i < workers; i++) {
            Ant defender = AntHillBlock.releaseAnt(serverLevel, releasePos, serverLevel.getRandom(), hill.owner);
            if (defender != null) {
                defender.setTarget(threat);
            }
        }
    }
}
