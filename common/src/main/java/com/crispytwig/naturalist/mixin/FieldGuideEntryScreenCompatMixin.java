package com.crispytwig.naturalist.mixin;

import com.crispytwig.naturalist.server.entity.base.Catchable;
import com.crispytwig.naturalist.server.entity.variant.DataDrivenVariantAnimal;
import com.crispytwig.naturalist.server.entity.variant.MobVariant;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Optional Field Guide 26.2 screen integration without a hard compile/runtime dependency.
 * Restores Naturalist's catch-container drop and variant-biome behavior from 1.21.1.
 */
@Pseudo
@Mixin(targets = "com.evandev.fieldguide.client.gui.screens.FieldGuideEntryScreen", remap = false)
public abstract class FieldGuideEntryScreenCompatMixin {
    @Shadow(remap = false)
    private Entity renderedEntity;

    @Shadow(remap = false)
    private List<ItemStack> loadedDrops;

    @Shadow(remap = false)
    @Final
    private List<Identifier> spawnBiomes;

    @Inject(method = "setupDropWidget", at = @At("HEAD"), remap = false, require = 0)
    private void naturalist$showCatchItems(boolean unlocked, CallbackInfo ci) {
        if (!unlocked) return;

        Entity entity = this.renderedEntity;
        if (entity == null) return;
        Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (!"naturalist".equals(entityId.getNamespace())) return;

        ItemStack stack = ItemStack.EMPTY;
        if (entity instanceof Catchable catchable) {
            stack = catchable.getCaughtItemStack();
            catchable.saveToHandTag(stack);
        } else if (entity instanceof Bucketable bucketable) {
            stack = bucketable.getBucketItemStack();
            bucketable.saveToBucketTag(stack);
        }

        if (stack.isEmpty()) return;

        ItemStack catchStack = stack;
        List<ItemStack> drops = this.loadedDrops == null ? new ArrayList<>() : new ArrayList<>(this.loadedDrops);
        drops.removeIf(existing -> ItemStack.isSameItemSameComponents(existing, catchStack));
        drops.add(catchStack);
        this.loadedDrops = drops;
    }

    @Inject(method = "setupBiomeWidget", at = @At("HEAD"), remap = false, require = 0)
    private void naturalist$filterBiomesByVariant(boolean unlocked, CallbackInfo ci) {
        if (!(this.renderedEntity instanceof DataDrivenVariantAnimal animal)) return;

        var holder = animal.getVariantHolder();
        if (holder.isEmpty()) return;

        var variantBiomes = holder.get().value().biomes();
        if (variantBiomes.isPresent()) {
            List<Identifier> biomes = naturalist$resolveBiomes(variantBiomes.get());
            if (!biomes.isEmpty()) {
                this.spawnBiomes.clear();
                this.spawnBiomes.addAll(biomes);
            }
            return;
        }

        Optional<Registry<MobVariant>> registry = this.renderedEntity.level().registryAccess().lookup(animal.getVariantRegistryKey());
        if (registry.isEmpty()) return;

        Set<Identifier> claimed = new HashSet<>();
        registry.get().listElements().forEach(variant ->
                variant.value().biomes().ifPresent(biomes -> claimed.addAll(naturalist$resolveBiomes(biomes))));
        this.spawnBiomes.removeIf(claimed::contains);
    }

    @Unique
    private static List<Identifier> naturalist$resolveBiomes(HolderSet<Biome> biomes) {
        return biomes.stream()
                .map(Holder::unwrapKey)
                .flatMap(Optional::stream)
                .map(key -> key.identifier())
                .distinct()
                .toList();
    }
}
