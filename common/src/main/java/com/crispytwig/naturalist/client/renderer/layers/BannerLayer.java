package com.crispytwig.naturalist.client.renderer.layers;

import com.crispytwig.naturalist.client.model.NaturalistEntityModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.mob.Elephant;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public class BannerLayer extends RenderLayer<NaturalistRenderState<Elephant>, NaturalistEntityModel<Elephant>> {
    private final BannerRenderer bannerRenderer;
    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;
    private final float scale;

    public BannerLayer(RenderLayerParent<NaturalistRenderState<Elephant>, NaturalistEntityModel<Elephant>> parent,
                       EntityRendererProvider.Context context, float offsetX, float offsetY, float offsetZ, float scale) {
        super(parent);
        this.bannerRenderer = new BannerRenderer(context.getModelSet(), context.getSprites());
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.scale = scale;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                       NaturalistRenderState<Elephant> state, float yRot, float xRot) {
        Elephant entity = state.entity;
        if (entity == null || entity.isBaby() || state.isInvisible) return;
        ItemStack stack = entity.getBanner();
        if (!(stack.getItem() instanceof BannerItem bannerItem)) return;

        DyeColor color = bannerItem.getColor();
        BannerPatternLayers patterns = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        float tilt = Mth.lerp(state.partialTick, entity.bannerSwingO, entity.bannerSwing) * Mth.DEG_TO_RAD + entity.getRenderRoll();
        float lift = Mth.lerp(state.partialTick, entity.bannerLiftO, entity.bannerLift) * Mth.DEG_TO_RAD;

        poseStack.pushPose();
        this.getParentModel().root().translateAndRotate(poseStack);
        this.getParentModel().root().getChild("body").translateAndRotate(poseStack);
        for (int side = 1; side >= -1; side -= 2) {
            poseStack.pushPose();
            poseStack.translate(side * this.offsetX / 16.0F, this.offsetY / 16.0F, this.offsetZ / 16.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F * side));
            poseStack.mulPose(Axis.XP.rotation(-side * tilt));
            poseStack.mulPose(Axis.ZP.rotation(side * lift));
            poseStack.scale(this.scale, this.scale, this.scale);
            this.bannerRenderer.submitSpecial(BannerBlock.AttachmentType.WALL, poseStack, collector,
                    lightCoords, OverlayTexture.NO_OVERLAY, color, patterns, state.outlineColor);
            poseStack.popPose();
        }
        poseStack.popPose();
    }
}
