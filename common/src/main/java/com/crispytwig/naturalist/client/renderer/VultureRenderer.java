package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.NaturalistEntityModel;
import com.crispytwig.naturalist.client.model.VultureBabyModel;
import com.crispytwig.naturalist.client.model.VultureModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.mob.Vulture;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class VultureRenderer extends NaturalistMobRenderer<Vulture> {
    public VultureRenderer(EntityRendererProvider.Context context) {
        super(context, new VultureModel(context.bakeLayer(VultureModel.LAYER_LOCATION)), new VultureBabyModel(context.bakeLayer(VultureBabyModel.LAYER_LOCATION)), 0.65F, 0.3F);
        this.addLayer(new VultureHeldItemLayer(this, context.getItemModelResolver()));
    }

    private static class VultureHeldItemLayer extends RenderLayer<NaturalistRenderState<Vulture>, NaturalistEntityModel<Vulture>> {
        private final ItemModelResolver itemModelResolver;
        private final ItemStackRenderState itemState = new ItemStackRenderState();

        VultureHeldItemLayer(RenderLayerParent<NaturalistRenderState<Vulture>, NaturalistEntityModel<Vulture>> parent, ItemModelResolver itemModelResolver) {
            super(parent);
            this.itemModelResolver = itemModelResolver;
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                           NaturalistRenderState<Vulture> state, float yRot, float xRot) {
            Vulture entity = state.entity;
            if (entity == null) return;
            ItemStack stack = entity.getItemBySlot(EquipmentSlot.MAINHAND);
            if (stack.isEmpty() || !(this.getParentModel() instanceof VultureModel vultureModel)) return;

            this.itemModelResolver.updateForLiving(this.itemState, stack, ItemDisplayContext.GROUND, entity);
            poseStack.pushPose();
            vultureModel.translateToHeldItem(poseStack);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            this.itemState.submit(poseStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }
    }
}
