package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.HippoBabyModel;
import com.crispytwig.naturalist.client.model.HippoModel;
import com.crispytwig.naturalist.client.model.NaturalistEntityModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.mob.Hippo;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BlockItem;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class HippoRenderer extends NaturalistMobRenderer<Hippo> {
    public HippoRenderer(EntityRendererProvider.Context context) {
        super(context, new HippoModel(context.bakeLayer(HippoModel.LAYER_LOCATION)), new HippoBabyModel(context.bakeLayer(HippoBabyModel.LAYER_LOCATION)), 1.1F);
        this.addLayer(new HippoJawBlockLayer(this));
    }

    private static class HippoJawBlockLayer extends RenderLayer<NaturalistRenderState<Hippo>, NaturalistEntityModel<Hippo>> {
        HippoJawBlockLayer(RenderLayerParent<NaturalistRenderState<Hippo>, NaturalistEntityModel<Hippo>> parent) {
            super(parent);
        }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                           NaturalistRenderState<Hippo> state, float yRot, float xRot) {
            Hippo entity = state.entity;
            if (entity == null || !(entity.getMainHandItem().getItem() instanceof BlockItem blockItem)
                    || !(this.getParentModel() instanceof HippoModel hippoModel)) return;

            poseStack.pushPose();
            hippoModel.translateToBotJaw(poseStack);
            poseStack.translate(-0.4D, 0.76D, -1.8D);
            poseStack.scale(0.675F, 0.675F, 0.675F);
            collector.submitBlock(poseStack, blockItem.getBlock().defaultBlockState(), lightCoords,
                    OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }
    }
}
