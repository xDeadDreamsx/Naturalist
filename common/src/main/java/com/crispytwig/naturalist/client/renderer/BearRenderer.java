package com.crispytwig.naturalist.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.crispytwig.naturalist.client.model.BearModel;
import com.crispytwig.naturalist.server.entity.mob.Bear;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import com.mojang.math.Axis;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

@Environment(EnvType.CLIENT)
public class BearRenderer extends GeoEntityRenderer<Bear> {
    public BearRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BearModel());
        this.shadowRadius = 0.9F;
        this.addRenderLayer(new BlockAndItemGeoLayer<>(this) {
            @Nullable
            @Override
            protected ItemStack getStackForBone(GeoBone bone, Bear animatable) {
                if ("rightHand".equals(bone.getName()) && animatable.getEatCounter() >= 8)
                    return animatable.getMainHandItem();
                return null;
            }

            @Override
            protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, Bear animatable) {
                return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            }

            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, Bear animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
                poseStack.mulPose(Axis.XP.rotationDegrees(-22.5F));
                poseStack.translate(1 / 16F, -8 / 16F, 2 / 16F);
                super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
            }
        });
    }

    @SuppressWarnings("unused")
    @Override
    public float getMotionAnimThreshold(Bear animatable) {
        return 0.000001f;
    }

    @Override
    public void render(@NotNull Bear entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        this.shadowRadius = entity.isBaby() ? 0.45F : 0.9F;
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
