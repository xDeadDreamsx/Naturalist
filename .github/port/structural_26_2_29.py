#!/usr/bin/env python3
"""Restore Firefly's animated base/glow overlay through 26.2 custom geometry submission."""
from pathlib import Path

ROOT=Path("common/src/main/java/com/crispytwig/naturalist/client/renderer")
LAYERS=ROOT/"layers"

ANIM=r'''package com.crispytwig.naturalist.client.renderer.layers;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jspecify.annotations.NonNull;

public class AnimatedUVVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private final float vScale;
    private final float vOffset;
    public AnimatedUVVertexConsumer(VertexConsumer delegate,int totalFrames,int currentFrame){
        this.delegate=delegate;this.vScale=1.0F/totalFrames;this.vOffset=(float)currentFrame/totalFrames;
    }
    @Override public @NonNull VertexConsumer addVertex(float x,float y,float z){delegate.addVertex(x,y,z);return this;}
    @Override public @NonNull VertexConsumer setColor(int r,int g,int b,int a){delegate.setColor(r,g,b,a);return this;}
    @Override public @NonNull VertexConsumer setUv(float u,float v){delegate.setUv(u,v*vScale+vOffset);return this;}
    @Override public @NonNull VertexConsumer setUv1(int u,int v){delegate.setUv1(u,v);return this;}
    @Override public @NonNull VertexConsumer setUv2(int u,int v){delegate.setUv2(u,v);return this;}
    @Override public @NonNull VertexConsumer setNormal(float x,float y,float z){delegate.setNormal(x,y,z);return this;}
}
'''

GLOW=r'''package com.crispytwig.naturalist.client.renderer.layers;

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
'''

def add_import(text,q):
    line=f"import {q};\n"
    if line in text:return text
    ps=[i for i in range(len(text)) if text.startswith("import ",i)];e=text.find("\n",ps[-1])+1
    return text[:e]+line+text[e:]

def main():
    LAYERS.mkdir(parents=True,exist_ok=True);changed=[]
    for n,c in (("AnimatedUVVertexConsumer.java",ANIM),("FireflyGlowLayer.java",GLOW)):
        p=LAYERS/n
        if not p.exists() or p.read_text()!=c:p.write_text(c);changed.append(str(p))
    p=ROOT/"FireflyRenderer.java";text=p.read_text();old=text
    text=add_import(text,"com.crispytwig.naturalist.client.renderer.layers.FireflyGlowLayer")
    line="        this.addLayer(new FireflyGlowLayer(this));"
    if line not in text:
        s=text.find("        super(context,");e=text.find(";",s);text=text[:e+1]+"\n"+line+text[e+1:]
    if text!=old:p.write_text(text);changed.append(str(p))
    print(f"26.2 firefly render parity changed {len(changed)} files")
    for x in changed:print(x)
if __name__=="__main__":main()
