#!/usr/bin/env python3
"""Restore Clam and Crab held-item layers using Minecraft 26.2 item render states."""
from pathlib import Path

ROOT = Path("common/src/main/java/com/crispytwig/naturalist/client/renderer")
LAYERS = ROOT / "layers"

CLAM = r'''package com.crispytwig.naturalist.client.renderer.layers;

import com.crispytwig.naturalist.client.model.ClamModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.mob.Clam;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

public class ClamItemLayer extends RenderLayer<NaturalistRenderState<Clam>, ClamModel> {
    private final ItemModelResolver itemModelResolver;
    private final EntityRenderDispatcher dispatcher;
    private final ItemStackRenderState itemState = new ItemStackRenderState();
    private final Quaternionf scratchRotation = new Quaternionf();

    public ClamItemLayer(RenderLayerParent<NaturalistRenderState<Clam>, ClamModel> parent,
                         ItemModelResolver itemModelResolver, EntityRenderDispatcher dispatcher) {
        super(parent);
        this.itemModelResolver = itemModelResolver;
        this.dispatcher = dispatcher;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                       NaturalistRenderState<Clam> state, float yRot, float xRot) {
        Clam clam = state.entity;
        if (clam == null) return;
        ItemStack held = clam.getMainHandItem();
        if (held.isEmpty()) return;
        this.itemModelResolver.updateForLiving(this.itemState, held, ItemDisplayContext.FIXED, clam);
        poseStack.pushPose();
        this.getParentModel().translateToItem(poseStack);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.scale(0.8F, 0.8F, 0.8F);
        poseStack.translate(0.0F, 0.6F + Mth.sin((clam.tickCount + state.partialTick) * 0.1F) * 0.2F, 0.0F);
        poseStack.mulPose(poseStack.last().pose().getNormalizedRotation(this.scratchRotation).conjugate());
        poseStack.mulPose(this.dispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        this.itemState.submit(poseStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}
'''

CRAB = r'''package com.crispytwig.naturalist.client.renderer.layers;

import com.crispytwig.naturalist.client.model.CrabModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.mob.Crab;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CrabItemLayer extends RenderLayer<NaturalistRenderState<Crab>, CrabModel> {
    private final ItemModelResolver itemModelResolver;
    private final ItemStackRenderState itemState = new ItemStackRenderState();

    public CrabItemLayer(RenderLayerParent<NaturalistRenderState<Crab>, CrabModel> parent, ItemModelResolver itemModelResolver) {
        super(parent);
        this.itemModelResolver = itemModelResolver;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                       NaturalistRenderState<Crab> state, float yRot, float xRot) {
        Crab crab = state.entity;
        if (crab == null) return;
        ItemStack held = crab.getMainHandItem();
        if (held.isEmpty()) return;
        this.itemModelResolver.updateForLiving(this.itemState, held, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, crab);
        poseStack.pushPose();
        this.getParentModel().translateToItem(poseStack);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        this.itemState.submit(poseStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}
'''


def add_import(text: str, q: str) -> str:
    line=f"import {q};\n"
    if line in text: return text
    ps=[i for i in range(len(text)) if text.startswith("import ",i)]
    if not ps:return text
    e=text.find("\n",ps[-1])+1
    return text[:e]+line+text[e:]


def attach(name: str, imp: str, line: str) -> bool:
    p=ROOT/name; text=p.read_text(); old=text
    text=add_import(text,imp)
    if line not in text:
        s=text.find("        super(context,"); e=text.find(";",s)
        if s<0 or e<0: raise RuntimeError(f"No constructor super in {p}")
        text=text[:e+1]+"\n"+line+text[e+1:]
    if text!=old:p.write_text(text);return True
    return False


def main():
    LAYERS.mkdir(parents=True,exist_ok=True)
    changed=[]
    for n,c in (("ClamItemLayer.java",CLAM),("CrabItemLayer.java",CRAB)):
        p=LAYERS/n
        if not p.exists() or p.read_text()!=c:p.write_text(c);changed.append(str(p))
    if attach("ClamRenderer.java","com.crispytwig.naturalist.client.renderer.layers.ClamItemLayer",
              "        this.addLayer(new ClamItemLayer(this, context.getItemModelResolver(), context.getEntityRenderDispatcher()));"):
        changed.append(str(ROOT/"ClamRenderer.java"))
    if attach("CrabRenderer.java","com.crispytwig.naturalist.client.renderer.layers.CrabItemLayer",
              "        this.addLayer(new CrabItemLayer(this, context.getItemModelResolver()));"):
        changed.append(str(ROOT/"CrabRenderer.java"))
    print(f"26.2 held-item render parity changed {len(changed)} files")
    for p in changed: print(p)

if __name__=="__main__":main()
