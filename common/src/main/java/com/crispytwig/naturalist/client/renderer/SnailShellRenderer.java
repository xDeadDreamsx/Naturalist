package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.server.block.SnailShellBlock;
import com.crispytwig.naturalist.server.block.entity.SnailShellBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class SnailShellRenderer implements BlockEntityRenderer<SnailShellBlockEntity, SnailShellRenderer.State> {
    private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();
    private final BlockModelResolver blockModelResolver;

    public SnailShellRenderer(BlockEntityRendererProvider.Context context) {
        this.blockModelResolver = context.blockModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SnailShellBlockEntity blockEntity, State state, float partialTicks,
                                   Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        BlockState blockState = blockEntity.getBlockState();
        state.rotation = blockState.getBlock() instanceof SnailShellBlock
                ? blockState.getValue(SnailShellBlock.ROTATION)
                : 0;
        this.blockModelResolver.update(state.shell, blockState, BLOCK_DISPLAY_CONTEXT);

        if (!blockEntity.getFlower().isEmpty() && blockEntity.getFlower().getItem() instanceof BlockItem blockItem
                && SnailShellBlock.getPottedBlock(blockItem.getBlock()) != null) {
            this.blockModelResolver.update(state.plant, blockItem.getBlock().defaultBlockState(), BLOCK_DISPLAY_CONTEXT);
            state.hasPlant = true;
        } else {
            state.plant.clear();
            state.hasPlant = false;
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-RotationSegment.convertToDegrees(state.rotation)));
        poseStack.translate(0.0D, -0.28125D, 0.15625D);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.translate(-0.5D, -0.21875D, -0.65625D);
        state.shell.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();

        if (state.hasPlant) {
            poseStack.pushPose();
            poseStack.translate(0.0D, 0.15D, 0.0D);
            state.plant.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    public static final class State extends BlockEntityRenderState {
        private final BlockModelRenderState shell = new BlockModelRenderState();
        private final BlockModelRenderState plant = new BlockModelRenderState();
        private int rotation;
        private boolean hasPlant;
    }
}
