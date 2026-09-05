#!/usr/bin/env python3
"""Restore the remaining Naturalist 1.21.1 renderer parity on Minecraft 26.2.

Ports the Alligator emissive glow, Elephant/Mammoth banners, and the animated-seat rider
submission for Elephant, Mammoth, Giraffe, and Ostrich to Mojang's 26.2 render-state API.
"""
from pathlib import Path

ROOT = Path("common/src/main/java/com/crispytwig/naturalist/client/renderer")
LAYERS = ROOT / "layers"

GLOW = r'''package com.crispytwig.naturalist.client.renderer.layers;

import com.crispytwig.naturalist.client.model.NaturalistEntityModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import java.util.function.Function;

public class GlowLayer<T extends Entity> extends RenderLayer<NaturalistRenderState<T>, NaturalistEntityModel<T>> {
    private final Function<T, Identifier> glowmask;

    public GlowLayer(RenderLayerParent<NaturalistRenderState<T>, NaturalistEntityModel<T>> parent,
                     Function<T, Identifier> glowmask) {
        super(parent);
        this.glowmask = glowmask;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                       NaturalistRenderState<T> state, float yRot, float xRot) {
        T entity = state.entity;
        if (entity == null || state.isInvisible) return;
        Identifier texture = this.glowmask.apply(entity);
        if (texture == null) return;
        collector.submitModel(this.getParentModel(), state, poseStack,
                RenderTypes.entityTranslucentEmissive(texture), lightCoords,
                LivingEntityRenderer.getOverlayCoords(state, 0.0F), -1, null, state.outlineColor, null);
    }
}
'''

BANNER = r'''package com.crispytwig.naturalist.client.renderer.layers;

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
'''

RIDER = r'''package com.crispytwig.naturalist.client.renderer.layers;

import com.crispytwig.naturalist.client.model.NaturalistEntityModel;
import com.crispytwig.naturalist.client.model.SeatedModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.variant.DataDrivenVariantAnimal;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public class SeatedRiderLayer<T extends Mob & DataDrivenVariantAnimal>
        extends RenderLayer<NaturalistRenderState<T>, NaturalistEntityModel<T>> {
    private final EntityRenderDispatcher dispatcher;

    public SeatedRiderLayer(RenderLayerParent<NaturalistRenderState<T>, NaturalistEntityModel<T>> parent,
                            EntityRenderDispatcher dispatcher) {
        super(parent);
        this.dispatcher = dispatcher;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                       NaturalistRenderState<T> state, float yRot, float xRot) {
        T entity = state.entity;
        if (entity == null || !(entity.getFirstPassenger() instanceof Player player)
                || !(this.getParentModel() instanceof SeatedModel seatedModel)) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (player == minecraft.player && minecraft.options.getCameraType().isFirstPerson()) return;

        EntityRenderState riderState = this.dispatcher.extractEntity(player, state.partialTick);
        // The normal passenger render already owns name tags and shadows. This submission exists only
        // to follow Naturalist's animated seat transform, matching the 1.21.1 SeatedRiderLayer.
        riderState.nameTag = null;
        riderState.scoreText = null;
        riderState.shadowPieces.clear();
        riderState.shadowRadius = 0.0F;

        CameraRenderState cameraState = new CameraRenderState();
        minecraft.gameRenderer.getMainCamera().extractRenderState(cameraState, state.partialTick);

        poseStack.pushPose();
        seatedModel.translateToSeat(poseStack);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-seatedModel.seatZRot() * 0.1F * (float)(180.0 / Math.PI)));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.bodyRot - 180.0F));
        poseStack.translate(0.0F, -seatedModel.seatHeight(), 0.0F);
        this.dispatcher.submit(riderState, cameraState, 0.0, 0.0, 0.0, poseStack, collector);
        poseStack.popPose();
    }
}
'''


def add_import(text: str, qualified: str) -> str:
    line = f"import {qualified};\n"
    if line in text:
        return text
    positions = [i for i in range(len(text)) if text.startswith("import ", i)]
    if not positions:
        return text
    end = text.find("\n", positions[-1]) + 1
    return text[:end] + line + text[end:]


def attach(renderer: str, imports: list[str], lines: list[str]) -> bool:
    path = ROOT / renderer
    text = path.read_text(encoding="utf-8")
    original = text
    for imp in imports:
        text = add_import(text, imp)
    missing = [line for line in lines if line not in text]
    if missing:
        start = text.find("        super(context,")
        end = text.find(";", start)
        if start < 0 or end < 0:
            raise RuntimeError(f"No renderer constructor super call in {path}")
        text = text[:end + 1] + "\n" + "\n".join(missing) + text[end + 1:]
    if text != original:
        path.write_text(text, encoding="utf-8")
        return True
    return False


def main() -> None:
    LAYERS.mkdir(parents=True, exist_ok=True)
    changed = []
    for name, content in (("GlowLayer.java", GLOW), ("BannerLayer.java", BANNER), ("SeatedRiderLayer.java", RIDER)):
        path = LAYERS / name
        if not path.exists() or path.read_text(encoding="utf-8") != content:
            path.write_text(content, encoding="utf-8")
            changed.append(str(path))

    specs = [
        ("AlligatorRenderer.java", ["com.crispytwig.naturalist.client.renderer.layers.GlowLayer"],
         ["        this.addLayer(new GlowLayer<>(this, entity -> entity.isBaby() ? BABY_GLOWMASK : GLOWMASK));"]),
        ("ElephantRenderer.java", ["com.crispytwig.naturalist.client.renderer.layers.BannerLayer", "com.crispytwig.naturalist.client.renderer.layers.SeatedRiderLayer"],
         ["        this.addLayer(new SeatedRiderLayer<>(this, context.getEntityRenderDispatcher()));", "        this.addLayer(new BannerLayer(this, context, BANNER_X, BANNER_Y, BANNER_Z, BANNER_SCALE));"]),
        ("MammothRenderer.java", ["com.crispytwig.naturalist.client.renderer.layers.BannerLayer", "com.crispytwig.naturalist.client.renderer.layers.SeatedRiderLayer"],
         ["        this.addLayer(new SeatedRiderLayer<>(this, context.getEntityRenderDispatcher()));", "        this.addLayer(new BannerLayer(this, context, BANNER_X, BANNER_Y, BANNER_Z, BANNER_SCALE));"]),
        ("GiraffeRenderer.java", ["com.crispytwig.naturalist.client.renderer.layers.SeatedRiderLayer"],
         ["        this.addLayer(new SeatedRiderLayer<>(this, context.getEntityRenderDispatcher()));"]),
        ("OstrichRenderer.java", ["com.crispytwig.naturalist.client.renderer.layers.SeatedRiderLayer"],
         ["        this.addLayer(new SeatedRiderLayer<>(this, context.getEntityRenderDispatcher()));"]),
    ]
    for renderer, imports, lines in specs:
        if attach(renderer, imports, lines):
            changed.append(str(ROOT / renderer))

    print(f"26.2 remaining render parity pass changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()
