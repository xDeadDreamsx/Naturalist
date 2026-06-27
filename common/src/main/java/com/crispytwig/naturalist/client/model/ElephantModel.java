package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Elephant;
import com.crispytwig.naturalist.server.entity.util.TerrainLegSolver;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.data.EntityModelData;

@Environment(EnvType.CLIENT)
public class ElephantModel extends IKGeoModel<Elephant> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Elephant elephant) {
        if (elephant.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/elephant_baby.geo.json");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/elephant.geo.json");
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Elephant elephant) {
        if (elephant.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/elephant/elephant_baby.png");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/elephant/elephant.png");
    }

    @Override
    public @NotNull ResourceLocation getAnimationResource(Elephant elephant) {
        if (elephant.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/elephant_baby.animation.json");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/elephant.animation.json");
    }

    @Override
    public void setCustomAnimations(Elephant entity, long instanceId, AnimationState<Elephant> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        if (animationState == null) return;

        EntityModelData extraDataOfType = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        GeoBone head = this.getBone("neck").orElse(null);

        if (head != null) {
            head.setRotX(head.getRotX() + extraDataOfType.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(head.getRotY() + extraDataOfType.netHeadYaw() * Mth.DEG_TO_RAD);
            head.resetStateChanges();
        }

        boolean saddled = entity.isSaddled();
        this.getBone("saddle").ifPresent(saddle -> saddle.setHidden(!saddled));
        this.getBone("saddleFront").ifPresent(saddle -> saddle.setHidden(!saddled));

        boolean chested = entity.isChested();
        this.getBone("chests").ifPresent(chest -> chest.setHidden(!chested));
        this.getBone("leftChest").ifPresent(chest -> chest.setHidden(!chested));
        this.getBone("rightChest").ifPresent(chest -> chest.setHidden(!chested));

        this.getBone("awake").ifPresent(awake -> awake.setHidden(false));
        this.getBone("asleep").ifPresent(asleep -> asleep.setHidden(true));

        articulateLegs(entity, animationState.getPartialTick());
    }

    @Override
    protected TerrainLegSolver getLegSolver(Elephant entity) {
        return entity.legSolver;
    }

    @Override
    protected String headBone() {
        return "neck";
    }

    @Override
    protected String[] legBones(Elephant entity) {
        return new String[]{"leftLeg", "rightLeg", "leftArm", "rightArm"};
    }
}
