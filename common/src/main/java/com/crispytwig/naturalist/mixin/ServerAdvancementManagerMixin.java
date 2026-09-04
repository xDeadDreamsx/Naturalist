package com.crispytwig.naturalist.mixin;

import com.crispytwig.naturalist.registry.NaturalistRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.triggers.FilledBucketTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(ServerAdvancementManager.class)
public class ServerAdvancementManagerMixin {
    @Unique
    private static final Identifier naturalist$TACTICAL_FISHING = Identifier.withDefaultNamespace("husbandry/tactical_fishing");

    @Shadow
    @Final
    private HolderLookup.Provider registries;

    @SuppressWarnings("unused")
    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("HEAD"))
    private void naturalist$addFishBucketsToTacticalFishing(Map<Identifier, Advancement> map, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        Advancement advancement = map.get(naturalist$TACTICAL_FISHING);
        if (advancement == null) {
            return;
        }

        Map<String, Criterion<?>> criteria = new HashMap<>(advancement.criteria());
        HolderGetter<Item> items = this.registries.lookupOrThrow(Registries.ITEM);
        naturalist$addBucketCriterion(criteria, items, "catfish_bucket", NaturalistRegistry.CATFISH_BUCKET.get());
        naturalist$addBucketCriterion(criteria, items, "bass_bucket", NaturalistRegistry.BASS_BUCKET.get());

        List<List<String>> requirements = new ArrayList<>();
        for (List<String> group : advancement.requirements().requirements()) {
            requirements.add(new ArrayList<>(group));
        }
        if (requirements.isEmpty()) {
            requirements.add(new ArrayList<>(criteria.keySet()));
        } else {
            naturalist$appendRequirement(requirements.get(0), "catfish_bucket");
            naturalist$appendRequirement(requirements.get(0), "bass_bucket");
        }

        map.put(naturalist$TACTICAL_FISHING, new Advancement(
                advancement.parent(),
                advancement.display(),
                advancement.rewards(),
                Map.copyOf(criteria),
                new AdvancementRequirements(requirements),
                advancement.sendsTelemetryEvent(),
                advancement.name()
        ));
    }

    @Unique
    private static void naturalist$addBucketCriterion(Map<String, Criterion<?>> criteria, HolderGetter<Item> items, String name, Item item) {
        criteria.putIfAbsent(name, FilledBucketTrigger.TriggerInstance.filledBucket(ItemPredicate.Builder.item().of(items, item)));
    }

    @Unique
    private static void naturalist$appendRequirement(List<String> requirements, String criterion) {
        if (!requirements.contains(criterion)) {
            requirements.add(criterion);
        }
    }
}
