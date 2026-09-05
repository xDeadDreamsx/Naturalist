package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.BlackBearBabyModel;
import com.crispytwig.naturalist.client.model.BlackBearModel;
import com.crispytwig.naturalist.client.model.NaturalistEntityModel;
import com.crispytwig.naturalist.client.renderer.layers.DyeLayer;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.mob.BlackBear;
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
import net.minecraft.world.item.Items;

@Environment(EnvType.CLIENT)
public class BlackBearRenderer extends NaturalistMobRenderer<BlackBear> {
    public BlackBearRenderer(EntityRendererProvider.Context context) {
        super(context, new BlackBearModel(context.bakeLayer(BlackBearModel.LAYER_LOCATION)), new BlackBearBabyModel(context.bakeLayer(BlackBearBabyModel.LAYER_LOCATION)), 0.9F);
        this.addLayer(new BlackBearHeldItemLayer(this, context.getItemModelResolver()));
        this.addLayer(new DyeLayer<>(this, "black_bear"));
    }

    private static class BlackBearHeldItemLayer extends RenderLayer<NaturalistRenderState<BlackBear>, NaturalistEntityModel<BlackBear>> {
        private final ItemModelResolver itemModelResolver;
        private final ItemStackRenderState itemState = new ItemStackRenderState();

        BlackBearHeldItemLayer(RenderLayerParent<NaturalistRenderState<BlackBear>, NaturalistEntityModel<BlackBear>> parent, ItemModelResolver itemModelResolver) {
            super(parent);
            this.itemModelResolver = itemModelResolver;
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                           NaturalistRenderState<BlackBear> state, float yRot, float xRot) {
            BlackBear entity = state.entity;
            if (entity == null || entity.getEatCounter() < 8) return;
            ItemStack stack = entity.getMainHandItem();
            if (stack.isEmpty() || stack.is(Items.SWEET_BERRIES) || stack.is(Items.HONEYCOMB)
                    || !(this.getParentModel() instanceof BlackBearModel bearModel)) return;

            this.itemModelResolver.updateForLiving(this.itemState, stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, entity);
            poseStack.pushPose();
            bearModel.translateToRightArm(poseStack);
            poseStack.mulPose(Axis.XP.rotationDegrees(-22.5F));
            poseStack.translate(0.0F, -7 / 16F, 0.0F);
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            this.itemState.submit(poseStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }
    }
}
