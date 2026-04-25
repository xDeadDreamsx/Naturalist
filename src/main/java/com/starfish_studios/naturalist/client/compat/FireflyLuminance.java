package com.starfish_studios.naturalist.client.compat;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.server.entity.mob.Firefly;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Range;

public final class FireflyLuminance implements EntityLuminance {
    public static final FireflyLuminance INSTANCE = new FireflyLuminance();
    public static final EntityLuminance.Type TYPE = EntityLuminance.Type.registerSimple(
            ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "firefly"),
            INSTANCE
    );

    private FireflyLuminance() {}

    @Override
    public Type type() {
        return TYPE;
    }

    @Override
    public @Range(from = 0, to = 15) int getLuminance(ItemLightSourceManager itemLightSourceManager, Entity entity) {
        if (entity instanceof Firefly firefly) {
            return firefly.getGlowLuminance();
        }
        return 0;
    }
}
