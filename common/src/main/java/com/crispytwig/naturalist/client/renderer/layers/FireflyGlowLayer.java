package com.crispytwig.naturalist.client.renderer.layers;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.client.model.NaturalistEntityModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.mob.Firefly;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

import java.util.List;

public class FireflyGlowLayer extends RenderLayer<NaturalistRenderState<Firefly>, NaturalistEntityModel<Firefly>> {
    private static final Identifier GLOW=Naturalist.location("textures/entity/firefly_glow.png");
    private static final Identifier GLOW_E=Naturalist.location("textures/entity/firefly_glow_e.png");
    private static final int TOTAL_FRAMES=30;
    public FireflyGlowLayer(RenderLayerParent<NaturalistRenderState<Firefly>,NaturalistEntityModel<Firefly>> parent){super(parent);}

    @Override public void submit(PoseStack poseStack,SubmitNodeCollector collector,int lightCoords,
                                 NaturalistRenderState<Firefly> state,float yRot,float xRot){
        Firefly entity=state.entity;
        if(entity==null||state.isInvisible)return;
        int frame=entity.isGlowing()?Math.min(Math.max(entity.tickCount-entity.getGlowStartTick(),0),TOTAL_FRAMES-1):0;
        NaturalistEntityModel<Firefly> model = this.getParentModel();
        List<ModelPart> parts = model.root().getAllParts();
        List<PartPose> poses = parts.stream().map(ModelPart::storePose).toList();
        collector.submitCustomGeometry(poseStack,RenderTypes.entityCutout(GLOW),(pose,buffer)->
                renderSnapshot(model, parts, poses, pose, buffer, lightCoords, frame));
        if(entity.isGlowing()){
            collector.submitCustomGeometry(poseStack,RenderTypes.entityTranslucentEmissive(GLOW_E),(pose,buffer)->
                    renderSnapshot(model, parts, poses, pose, buffer, lightCoords, frame));
        }
    }

    private static void renderSnapshot(NaturalistEntityModel<Firefly> model, List<ModelPart> parts,
                                       List<PartPose> poses, PoseStack.Pose pose, VertexConsumer buffer,
                                       int lightCoords, int frame) {
        // Geometry is submitted now and rendered later. The caller's pose stack and the
        // shared entity model may already belong to a different firefly at that point.
        PoseStack renderPose = new PoseStack();
        renderPose.mulPose(pose.pose());
        renderPose.last().normal().set(pose.normal());
        List<PartPose> previous = parts.stream().map(ModelPart::storePose).toList();
        try {
            for (int i = 0; i < parts.size(); i++) {
                parts.get(i).loadPose(poses.get(i));
            }
            model.renderToBuffer(renderPose, new AnimatedUVVertexConsumer(buffer, TOTAL_FRAMES, frame),
                    lightCoords, OverlayTexture.NO_OVERLAY, -1);
        } finally {
            for (int i = 0; i < parts.size(); i++) {
                parts.get(i).loadPose(previous.get(i));
            }
        }
    }
}
