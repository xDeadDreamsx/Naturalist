package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.BlackBearModel;
import com.crispytwig.naturalist.client.renderer.layers.DyeOverlayLayer;
import com.crispytwig.naturalist.server.entity.mob.BlackBear;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

@Environment(EnvType.CLIENT)
public class BlackBearRenderer extends GeoEntityRenderer<BlackBear> {
    public BlackBearRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BlackBearModel());
        this.shadowRadius = 0.9F;
        this.addRenderLayer(new BlockAndItemGeoLayer<>(this) {
            @Nullable
            @Override
            protected ItemStack getStackForBone(GeoBone bone, BlackBear animatable) {
                if ("right_arm".equals(bone.getName()) && animatable.getEatCounter() >= 8) {
                    ItemStack stack = animatable.getMainHandItem();
                    return stack.is(Items.SWEET_BERRIES) || stack.is(Items.HONEYCOMB) ? null : stack;
                }
                return null;
            }

            @Override
            protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, BlackBear animatable) {
                return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            }

            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, BlackBear animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
                poseStack.mulPose(Axis.XP.rotationDegrees(-22.5F));
                poseStack.translate(0.0F, -7 / 16F, 0.0F);
                super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
            }
        });
        this.addRenderLayer(new DyeOverlayLayer<>(this, "black_bear"));
    }

    @SuppressWarnings("unused")
    @Override
    public float getMotionAnimThreshold(BlackBear animatable) {
        return 0.000001f;
    }

    @Override
    public void render(@NotNull BlackBear entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        this.shadowRadius = entity.isBaby() ? 0.45F : 0.9F;
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
