#!/usr/bin/env python3
"""Fix compile/API details exposed by the first 26.2 render-layer parity build.

The common Naturalist renderer exposes NaturalistEntityModel<T>, not each concrete model type;
Clam/Crab therefore cast only where their custom translateToItem hook is required. Minecraft 26.2
also added VertexConsumer#setLineWidth and moved camera orientation access onto Camera#rotation().
"""
from pathlib import Path

ROOT = Path("common/src/main/java/com/crispytwig/naturalist/client/renderer")
LAYERS = ROOT / "layers"


def add_import(text: str, qualified: str) -> str:
    line = f"import {qualified};\n"
    if line in text:
        return text
    positions = [i for i in range(len(text)) if text.startswith("import ", i)]
    if not positions:
        return text
    end = text.find("\n", positions[-1]) + 1
    return text[:end] + line + text[end:]


def patch_clam() -> bool:
    path = LAYERS / "ClamItemLayer.java"
    text = path.read_text(encoding="utf-8")
    original = text
    text = add_import(text, "com.crispytwig.naturalist.client.model.NaturalistEntityModel")
    text = text.replace(
        "extends RenderLayer<NaturalistRenderState<Clam>, ClamModel>",
        "extends RenderLayer<NaturalistRenderState<Clam>, NaturalistEntityModel<Clam>>",
    )
    text = text.replace(
        "RenderLayerParent<NaturalistRenderState<Clam>, ClamModel> parent,",
        "RenderLayerParent<NaturalistRenderState<Clam>, NaturalistEntityModel<Clam>> parent,",
    )
    old = """        ItemStack held = clam.getMainHandItem();
        if (held.isEmpty()) return;
        this.itemModelResolver.updateForLiving(this.itemState, held, ItemDisplayContext.FIXED, clam);
        poseStack.pushPose();
        this.getParentModel().translateToItem(poseStack);"""
    new = """        ItemStack held = clam.getMainHandItem();
        if (held.isEmpty() || !(this.getParentModel() instanceof ClamModel model)) return;
        this.itemModelResolver.updateForLiving(this.itemState, held, ItemDisplayContext.FIXED, clam);
        poseStack.pushPose();
        model.translateToItem(poseStack);"""
    text = text.replace(old, new)
    text = text.replace(
        "        poseStack.mulPose(this.dispatcher.cameraOrientation());",
        "        if (this.dispatcher.camera != null) {\n            poseStack.mulPose(this.dispatcher.camera.rotation());\n        }",
    )
    if text != original:
        path.write_text(text, encoding="utf-8")
        return True
    return False


def patch_crab() -> bool:
    path = LAYERS / "CrabItemLayer.java"
    text = path.read_text(encoding="utf-8")
    original = text
    text = add_import(text, "com.crispytwig.naturalist.client.model.NaturalistEntityModel")
    text = text.replace(
        "extends RenderLayer<NaturalistRenderState<Crab>, CrabModel>",
        "extends RenderLayer<NaturalistRenderState<Crab>, NaturalistEntityModel<Crab>>",
    )
    text = text.replace(
        "RenderLayerParent<NaturalistRenderState<Crab>, CrabModel> parent,",
        "RenderLayerParent<NaturalistRenderState<Crab>, NaturalistEntityModel<Crab>> parent,",
    )
    old = """        ItemStack held = crab.getMainHandItem();
        if (held.isEmpty()) return;
        this.itemModelResolver.updateForLiving(this.itemState, held, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, crab);
        poseStack.pushPose();
        this.getParentModel().translateToItem(poseStack);"""
    new = """        ItemStack held = crab.getMainHandItem();
        if (held.isEmpty() || !(this.getParentModel() instanceof CrabModel model)) return;
        this.itemModelResolver.updateForLiving(this.itemState, held, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, crab);
        poseStack.pushPose();
        model.translateToItem(poseStack);"""
    text = text.replace(old, new)
    if text != original:
        path.write_text(text, encoding="utf-8")
        return True
    return False


def patch_uv_consumer() -> bool:
    path = LAYERS / "AnimatedUVVertexConsumer.java"
    text = path.read_text(encoding="utf-8")
    original = text
    if "setLineWidth(float width)" not in text:
        marker = "    @Override public @NonNull VertexConsumer setNormal(float x,float y,float z){delegate.setNormal(x,y,z);return this;}\n"
        addition = marker + "    @Override public @NonNull VertexConsumer setLineWidth(float width){delegate.setLineWidth(width);return this;}\n"
        if marker not in text:
            raise RuntimeError("Could not locate AnimatedUVVertexConsumer setNormal")
        text = text.replace(marker, addition, 1)
    if text != original:
        path.write_text(text, encoding="utf-8")
        return True
    return False


def main() -> None:
    changed = []
    for fn, name in ((patch_clam, "ClamItemLayer.java"), (patch_crab, "CrabItemLayer.java"), (patch_uv_consumer, "AnimatedUVVertexConsumer.java")):
        if fn():
            changed.append(str(LAYERS / name))
    print(f"26.2 render compile diagnostics pass changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()
