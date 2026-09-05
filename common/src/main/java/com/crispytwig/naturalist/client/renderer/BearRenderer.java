package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.BearBabyModel;
import com.crispytwig.naturalist.client.model.BearModel;
import com.crispytwig.naturalist.client.model.NaturalistEntityModel;
import com.crispytwig.naturalist.client.renderer.layers.DyeLayer;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.mob.Bear;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class BearRenderer extends NaturalistMobRenderer<Bear> {
    public BearRenderer(EntityRendererProvider.Context context) {
        super(context, new BearModel(context.bakeLayer(BearModel.LAYER_LOCATION)), new BearBabyModel(context.bakeLayer(BearBabyModel.LAYER_LOCATION)), 0.9F);
        this.addLayer(new BearHeldItemLayer(this, context.getItemModelResolver()));
        this.addLayer(new DyeLayer<>(this, "bear"));
    }

    private static class BearHeldItemLayer extends RenderLayer<NaturalistRenderState<Bear>, NaturalistEntityModel<Bear>> {
        private final ItemModelResolver itemModelResolver;
        private final ItemStackRenderState itemState = new ItemStackRenderState();

        BearHeldItemLayer(RenderLayerParent<NaturalistRenderState<Bear>, NaturalistEntityModel<Bear>> parent, ItemModelResolver itemModelResolver) {
            super(parent);
            this.itemModelResolver = itemModelResolver;
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                           NaturalistRenderState<Bear> state, float yRot, float xRot) {
            Bear entity = state.entity;
            if (entity == null || entity.getEatCounter() < 8) return;
            ItemStack stack = entity.getMainHandItem();
            if (stack.isEmpty() || !(this.getParentModel() instanceof BearModel bearModel)) return;

            this.itemModelResolver.updateForLiving(this.itemState, stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, entity);
            poseStack.pushPose();
            bearModel.translateToRightHand(poseStack);
            poseStack.mulPose(Axis.XP.rotationDegrees(-22.5F));
            poseStack.translate(1 / 16F, -8 / 16F, 2 / 16F);
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            this.itemState.submit(poseStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }
    }
}
