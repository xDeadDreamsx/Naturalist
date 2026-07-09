package com.crispytwig.naturalist.server.entity.variant;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.registry.NaturalistMobVariants;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ServerLevelAccessor;

import java.util.Optional;

public interface DataDrivenVariantAnimal {
    String VARIANT_TAG = "Variant";

    default ResourceKey<Registry<MobVariant>> variantRegistryKey() {
        return NaturalistMobVariants.registryFor(((Mob) this).getType());
    }

    default ResourceKey<MobVariant> defaultVariant() {
        return NaturalistMobVariants.defaultFor(((Mob) this).getType());
    }

    default String[] legacyVariantNames() {
        return new String[]{this.defaultVariant().location().getPath()};
    }

    ResourceLocation fallbackVariantTexture();

    String getVariantRawId();

    void setVariantRawId(String id);

    default ResourceLocation getVariantId() {
        ResourceLocation id = ResourceLocation.tryParse(this.getVariantRawId());
        return id != null ? id : this.defaultVariant().location();
    }

    default void setVariant(Holder<MobVariant> variant) {
        variant.unwrapKey().ifPresent(key -> this.setVariantRawId(key.location().toString()));
    }

    default Optional<Holder.Reference<MobVariant>> getVariantHolder() {
        return MobVariantUtil.byId(((Mob) this).level().registryAccess(), this.variantRegistryKey(), this.getVariantId());
    }

    default boolean hasNonDefaultVariant() {
        return !this.getVariantId().equals(this.defaultVariant().location());
    }

    default ResourceLocation getVariantTexture() {
        return this.getVariantHolder().map(holder -> holder.value().texture()).orElseGet(this::fallbackVariantTexture);
    }

    default ResourceLocation getVariantBabyTexture() {
        return this.getVariantHolder().map(holder -> holder.value().babyTexture()).orElseGet(this::fallbackVariantTexture);
    }

    default ResourceLocation getVariantModel(ResourceLocation fallback) {
        return this.getVariantHolder().flatMap(holder -> holder.value().model()).orElse(fallback);
    }

    default ResourceLocation getVariantAnimation(ResourceLocation fallback) {
        return this.getVariantHolder().flatMap(holder -> holder.value().animation()).orElse(fallback);
    }

    default ResourceLocation getVariantBabyModel(ResourceLocation fallback) {
        return this.getVariantHolder().flatMap(holder -> holder.value().babyModel()).orElse(fallback);
    }

    default ResourceLocation getVariantBabyAnimation(ResourceLocation fallback) {
        return this.getVariantHolder().flatMap(holder -> holder.value().babyAnimation()).orElse(fallback);
    }

    default void saveVariant(CompoundTag tag) {
        tag.putString(VARIANT_TAG, this.getVariantId().toString());
    }

    default void loadVariant(CompoundTag tag) {
        MobVariantUtil.readVariantId(tag, this.legacyVariantNames())
                .ifPresent(id -> this.setVariantRawId(id.toString()));
    }

    default int getLegacyVariantIndex() {
        String path = this.getVariantId().getPath();
        String[] legacy = this.legacyVariantNames();
        for (int i = 0; i < legacy.length; i++) {
            if (legacy[i].equals(path)) {
                return i;
            }
        }
        return 0;
    }

    default void setVariantByLegacyIndex(int index) {
        String[] legacy = this.legacyVariantNames();
        String name = legacy[Math.floorMod(index, legacy.length)];
        this.setVariantRawId(Naturalist.location(name).toString());
    }

    default void pickVariantForSpawn(ServerLevelAccessor level) {
        MobVariantUtil.selectVariantForSpawn(level, ((Mob) this).blockPosition(), this.variantRegistryKey())
                .ifPresent(this::setVariant);
    }

    default String inheritVariantFrom(AgeableMob otherParent, RandomSource random) {
        return otherParent instanceof DataDrivenVariantAnimal other && random.nextBoolean()
                ? other.getVariantRawId()
                : this.getVariantRawId();
    }
}
