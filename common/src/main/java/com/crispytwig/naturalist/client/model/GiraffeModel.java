package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Giraffe;
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
public class GiraffeModel extends IKGeoModel<Giraffe> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Giraffe giraffe) {
        if (giraffe.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/giraffe_baby.geo.json");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/giraffe.geo.json");
    }

    @Override
    @SuppressWarnings("removal")
    public @NotNull ResourceLocation getTextureResource(Giraffe giraffe) {
        if (giraffe.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/giraffe/giraffe_baby.png");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/giraffe/giraffe.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Giraffe giraffe) {
        if (giraffe.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/giraffe_baby.animation.json");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/giraffe.animation.json");
    }

    @Override
    public void setCustomAnimations(@NotNull Giraffe entity, long instanceId, AnimationState<Giraffe> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        if (animationState == null) return;

        EntityModelData extraDataOfType = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        GeoBone head = this.getBone("head").orElse(null);

        if (head != null) {
            head.setRotX(head.getRotX() + extraDataOfType.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(head.getRotY() + extraDataOfType.netHeadYaw() * Mth.DEG_TO_RAD);
            head.resetStateChanges();
        }

        this.getBone("saddle").ifPresent(saddle -> saddle.setHidden(!entity.isTame()));

        articulateLegs(entity, animationState.getPartialTick());
    }

    @Override
    protected TerrainLegSolver getLegSolver(Giraffe entity) {
        return entity.legSolver;
    }

    @Override
    protected String headBone() {
        return "head";
    }

    @Override
    protected String[] legBones(Giraffe entity) {
        if (entity.isBaby()) {
            return new String[]{"leftLeg", "rightLeg", "leftArm", "rightArm"};
        }
        return new String[]{"leftFoot", "rightFoot", "leftHand", "rightHand"};
    }
}
