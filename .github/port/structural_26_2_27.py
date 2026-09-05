#!/usr/bin/env python3
"""Restore Naturalist render layers that were dropped during the 26.2 render-state migration.

This pass ports the model-overlay layers whose behavior maps directly to Minecraft 26.2's
RenderLayer#submit API: generic dye overlays, Capybara/Ostrich baby dye variants, Hedgehog glint,
and the named Tortoise masks. It also re-attaches those layers to the migrated renderers.
"""
from pathlib import Path
import runpy

ROOT = Path("common/src/main/java/com/crispytwig/naturalist/client/renderer")
LAYERS = ROOT / "layers"

FILES = {
"DyeLayer.java": r'''package com.crispytwig.naturalist.client.renderer.layers;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.base.DyeableAnimal;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;

import java.util.EnumMap;

public class DyeLayer<T extends LivingEntity & DyeableAnimal, M extends EntityModel<NaturalistRenderState<T>>>
        extends RenderLayer<NaturalistRenderState<T>, M> {
    private final String folder;
    private final EnumMap<DyeColor, Identifier> textures = new EnumMap<>(DyeColor.class);

    public DyeLayer(RenderLayerParent<NaturalistRenderState<T>, M> parent, String folder) {
        super(parent);
        this.folder = folder;
    }

    protected Identifier getDyeTexture(T entity, DyeColor color) {
        return this.textures.computeIfAbsent(color, c -> this.getDyeTexture(c.getName()));
    }

    protected Identifier getDyeTexture(String name) {
        return Naturalist.location("textures/entity/" + this.folder + "/dye/" + name + ".png");
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                       NaturalistRenderState<T> state, float yRot, float xRot) {
        T entity = state.entity;
        if (entity == null || state.isInvisible) return;
        DyeColor color = entity.getDyeColor();
        if (color == null) return;
        collector.submitModel(this.getParentModel(), state, poseStack,
                RenderTypes.entityCutout(this.getDyeTexture(entity, color)), lightCoords,
                LivingEntityRenderer.getOverlayCoords(state, 0.0F), -1, null, state.outlineColor, null);
    }
}
''',
"CapybaraDyeLayer.java": r'''package com.crispytwig.naturalist.client.renderer.layers;

import com.crispytwig.naturalist.client.model.NaturalistEntityModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.mob.Capybara;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

public class CapybaraDyeLayer extends DyeLayer<Capybara, NaturalistEntityModel<Capybara>> {
    private static final Set<DyeColor> BABY_COLORS = EnumSet.of(
            DyeColor.WHITE, DyeColor.MAGENTA, DyeColor.LIGHT_BLUE, DyeColor.LIME, DyeColor.GRAY,
            DyeColor.BLUE, DyeColor.BROWN, DyeColor.GREEN, DyeColor.RED, DyeColor.BLACK);
    private final EnumMap<DyeColor, Identifier> babyTextures = new EnumMap<>(DyeColor.class);

    public CapybaraDyeLayer(RenderLayerParent<NaturalistRenderState<Capybara>, NaturalistEntityModel<Capybara>> parent) {
        super(parent, "capybara");
    }

    @Override
    protected Identifier getDyeTexture(Capybara entity, DyeColor color) {
        if (!entity.isBaby() || !BABY_COLORS.contains(color)) return super.getDyeTexture(entity, color);
        return this.babyTextures.computeIfAbsent(color, c -> this.getDyeTexture(c.getName() + "_baby"));
    }
}
''',
"OstrichDyeLayer.java": r'''package com.crispytwig.naturalist.client.renderer.layers;

import com.crispytwig.naturalist.client.model.NaturalistEntityModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.mob.Ostrich;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

import java.util.EnumMap;

public class OstrichDyeLayer extends DyeLayer<Ostrich, NaturalistEntityModel<Ostrich>> {
    private final EnumMap<DyeColor, Identifier> babyTextures = new EnumMap<>(DyeColor.class);

    public OstrichDyeLayer(RenderLayerParent<NaturalistRenderState<Ostrich>, NaturalistEntityModel<Ostrich>> parent) {
        super(parent, "ostrich");
    }

    @Override
    protected Identifier getDyeTexture(Ostrich entity, DyeColor color) {
        if (!entity.isBaby()) return super.getDyeTexture(entity, color);
        return this.babyTextures.computeIfAbsent(color, c -> this.getDyeTexture(c.getName() + "_baby"));
    }
}
''',
"HedgehogGlintLayer.java": r'''package com.crispytwig.naturalist.client.renderer.layers;

import com.crispytwig.naturalist.client.model.NaturalistEntityModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.mob.Hedgehog;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class HedgehogGlintLayer extends RenderLayer<NaturalistRenderState<Hedgehog>, NaturalistEntityModel<Hedgehog>> {
    public HedgehogGlintLayer(RenderLayerParent<NaturalistRenderState<Hedgehog>, NaturalistEntityModel<Hedgehog>> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                       NaturalistRenderState<Hedgehog> state, float yRot, float xRot) {
        Hedgehog entity = state.entity;
        if (entity == null || state.isInvisible || !entity.hasThrowEnchantments()) return;
        collector.submitModel(this.getParentModel(), state, poseStack, RenderTypes.entityGlint(), lightCoords,
                LivingEntityRenderer.getOverlayCoords(state, 0.0F), -1, null, state.outlineColor, null);
    }
}
''',
"TortoiseMaskLayer.java": r'''package com.crispytwig.naturalist.client.renderer.layers;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.client.model.NaturalistEntityModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.mob.Tortoise;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class TortoiseMaskLayer extends RenderLayer<NaturalistRenderState<Tortoise>, NaturalistEntityModel<Tortoise>> {
    private static final Identifier DONATELLO = Naturalist.location("textures/entity/tortoise/donatello.png");
    private static final Identifier LEONARDO = Naturalist.location("textures/entity/tortoise/leonardo.png");
    private static final Identifier MICHELANGELO = Naturalist.location("textures/entity/tortoise/michelangelo.png");
    private static final Identifier RAPHAEL = Naturalist.location("textures/entity/tortoise/raphael.png");

    public TortoiseMaskLayer(RenderLayerParent<NaturalistRenderState<Tortoise>, NaturalistEntityModel<Tortoise>> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                       NaturalistRenderState<Tortoise> state, float yRot, float xRot) {
        Tortoise entity = state.entity;
        if (entity == null || state.isInvisible || !entity.hasCustomName()) return;
        Identifier skin = switch (entity.getName().getString()) {
            case "Donatello" -> DONATELLO;
            case "Leonardo" -> LEONARDO;
            case "Michelangelo" -> MICHELANGELO;
            case "Raphael" -> RAPHAEL;
            default -> null;
        };
        if (skin == null) return;
        collector.submitModel(this.getParentModel(), state, poseStack, RenderTypes.entityCutout(skin), lightCoords,
                LivingEntityRenderer.getOverlayCoords(state, 0.0F), -1, null, state.outlineColor, null);
    }
}
'''
}


def add_import(text: str, qualified: str) -> str:
    line = f"import {qualified};\n"
    if line in text: return text
    positions = [i for i in range(len(text)) if text.startswith("import ", i)]
    if not positions: return text
    end = text.find("\n", positions[-1]) + 1
    return text[:end] + line + text[end:]


def attach(renderer: str, imports: list[str], lines: list[str]) -> bool:
    path = ROOT / renderer
    text = path.read_text(encoding="utf-8")
    original = text
    for imp in imports: text = add_import(text, imp)
    missing = [line for line in lines if line not in text]
    if missing:
        marker = "        super(context,"
        start = text.find(marker)
        if start < 0: raise RuntimeError(f"No renderer super call in {path}")
        end = text.find(";", start)
        insertion = "\n" + "\n".join(missing)
        text = text[:end+1] + insertion + text[end+1:]
    if text != original:
        path.write_text(text, encoding="utf-8")
        return True
    return False


def main() -> None:
    LAYERS.mkdir(parents=True, exist_ok=True)
    changed = []
    for name, content in FILES.items():
        path = LAYERS / name
        if not path.exists() or path.read_text(encoding="utf-8") != content:
            path.write_text(content, encoding="utf-8")
            changed.append(str(path))

    specs = [
        ("BirdRenderer.java", ["com.crispytwig.naturalist.client.renderer.layers.DyeLayer"], ["        this.addLayer(new DyeLayer<>(this, \"bird\"));"]),
        ("CapybaraRenderer.java", ["com.crispytwig.naturalist.client.renderer.layers.CapybaraDyeLayer"], ["        this.addLayer(new CapybaraDyeLayer(this));"]),
        ("HedgehogRenderer.java", ["com.crispytwig.naturalist.client.renderer.layers.DyeLayer", "com.crispytwig.naturalist.client.renderer.layers.HedgehogGlintLayer"], ["        this.addLayer(new DyeLayer<>(this, \"hedgehog\"));", "        this.addLayer(new HedgehogGlintLayer(this));"]),
        ("OstrichRenderer.java", ["com.crispytwig.naturalist.client.renderer.layers.OstrichDyeLayer"], ["        this.addLayer(new OstrichDyeLayer(this));"]),
        ("TortoiseRenderer.java", ["com.crispytwig.naturalist.client.renderer.layers.TortoiseMaskLayer"], ["        this.addLayer(new TortoiseMaskLayer(this));"]),
    ]
    for renderer, imports, lines in specs:
        if attach(renderer, imports, lines): changed.append(str(ROOT / renderer))

    print(f"26.2 core render-layer parity pass changed {len(changed)} files")
    for path in changed: print(path)


if __name__ == "__main__":
    main()
    runpy.run_path(".github/port/structural_26_2_28.py", run_name="__main__")
