package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.client.model.BlobfishPinkModel;
import com.crispytwig.naturalist.server.entity.mob.Blobfish;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class BlobfishRenderer extends NaturalistSingleMobRenderer<Blobfish> {
    private static final Identifier GRAY = Naturalist.location("textures/entity/blobfish/gray.png");
    private static final Identifier PINK = Naturalist.location("textures/entity/blobfish/pink.png");

    public BlobfishRenderer(EntityRendererProvider.Context context) {
        super(context, new BlobfishPinkModel(context.bakeLayer(BlobfishPinkModel.LAYER_LOCATION)), 0.0F);
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull NaturalistRenderState<Blobfish> state) {
        Blobfish entity = state.entity;
        if (entity.hasNonDefaultVariant()) {
            return entity.getVariantTexture();
        }
        return entity.isGray() ? GRAY : PINK;
    }

    @Override
    protected void setupRotations(@NotNull NaturalistRenderState<Blobfish> state, @NotNull PoseStack poseStack, float yBodyRot, float nativeScale) {
        Blobfish entity = state.entity;
        float partialTick = state.partialTick;
        super.setupRotations(state, poseStack, yBodyRot, nativeScale);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-entity.swimTilt.getTilt(partialTick)));
    }
}
