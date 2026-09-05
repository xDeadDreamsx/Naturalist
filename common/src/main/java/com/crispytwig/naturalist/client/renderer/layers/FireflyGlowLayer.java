package com.crispytwig.naturalist.client.renderer.layers;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.client.model.NaturalistEntityModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.mob.Firefly;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

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
        collector.submitCustomGeometry(poseStack,RenderTypes.entityCutout(GLOW),(pose,buffer)->
                this.getParentModel().renderToBuffer(poseStack,new AnimatedUVVertexConsumer(buffer,TOTAL_FRAMES,frame),lightCoords,OverlayTexture.NO_OVERLAY,-1));
        if(entity.isGlowing()){
            collector.submitCustomGeometry(poseStack,RenderTypes.entityTranslucentEmissive(GLOW_E),(pose,buffer)->
                    this.getParentModel().renderToBuffer(poseStack,new AnimatedUVVertexConsumer(buffer,TOTAL_FRAMES,frame),lightCoords,OverlayTexture.NO_OVERLAY,-1));
        }
    }
}
