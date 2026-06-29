package com.crispytwig.naturalist.client.renderer.layers;

import com.crispytwig.naturalist.server.entity.mob.Clam;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

@Environment(EnvType.CLIENT)
public class ClamItemLayer extends BlockAndItemGeoLayer<Clam> {
    public ClamItemLayer(GeoRenderer<Clam> renderer) {
        super(renderer);
    }

    @Nullable
    @Override
    protected ItemStack getStackForBone(GeoBone bone, Clam clam) {
        ItemStack held = clam.getMainHandItem();
        return (bone.getName().equals("rightItem") && !held.isEmpty()) ? held : null;
    }

    @Override
    protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, Clam clam) {
        return ItemDisplayContext.FIXED;
    }

    @Override
    protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, Clam clam, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
        if (bone.getName().equals("rightItem")) {
            poseStack.scale(0.8F, 0.8F, 0.8F);

            float age = clam.tickCount + partialTick;
            poseStack.translate(0.0F, 0.6F + Mth.sin(age * 0.1F) * 0.2F, 0.0F);

            Quaternionf rotation = poseStack.last().pose().getNormalizedRotation(new Quaternionf());
            poseStack.mulPose(rotation.conjugate());
            poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        }
        super.renderStackForBone(poseStack, bone, stack, clam, bufferSource, partialTick, packedLight, packedOverlay);
    }
}
