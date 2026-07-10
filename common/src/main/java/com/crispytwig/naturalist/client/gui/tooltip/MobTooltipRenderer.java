package com.crispytwig.naturalist.client.gui.tooltip;

import com.crispytwig.naturalist.server.entity.base.Catchable;
import com.crispytwig.naturalist.server.item.tooltip.MobTooltipData;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class MobTooltipRenderer implements ClientTooltipComponent {
    private static final String TOOLTIP_ANIM_NAME = "tooltip";
    private static final int CELL_SIZE = 16;
    private static final int MAX_PER_ROW = 4;
    private static final Map<CompoundTag, LivingEntity> ENTITY_CACHE = new HashMap<>();
    private static final Map<ResourceLocation, Bounds> BOUNDS_CACHE = new HashMap<>();

    private final ListTag mobs;
    private final int[] rowHeights;

    public MobTooltipRenderer(MobTooltipData data) {
        this.mobs = data.mobs();
        this.rowHeights = computeRowHeights();
    }

    @Override
    public int getWidth(@NotNull Font font) {
        return Math.min(mobs.size(), MAX_PER_ROW) * CELL_SIZE;
    }

    @Override
    public int getHeight() {
        int total = 0;
        for (int h : rowHeights) total += h;
        return total;
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        for (int i = 0; i < mobs.size(); i++) {
            LivingEntity living = getOrCreateEntity(mobs.getCompound(i), mc.level);
            if (living == null) continue;

            living.tickCount = (int) mc.level.getGameTime();

            Bounds bounds = modelBounds(living);
            if (bounds == null) {
                var dim = living.getDimensions(living.getPose());
                bounds = new Bounds(-dim.width() / 2f, dim.width() / 2f, 0, dim.height());
            }

            float sizeFactor = living instanceof AgeableMob ageable && ageable.isBaby() ? 0.5f : 1.0f;
            float scale = Math.min((CELL_SIZE * 0.8f * sizeFactor) / Math.max(bounds.width(), bounds.height()), 32.0F);

            int col = i % MAX_PER_ROW;
            int row = i / MAX_PER_ROW;
            int yOffset = 0;
            for (int r = 0; r < row; r++) yOffset += rowHeights[r];
            float cellCenterX = x + col * CELL_SIZE + (CELL_SIZE / 2f);
            float cellCenterY = y + yOffset + (rowHeights[row] / 2f);

            float time = (mc.level.getGameTime() + mc.getTimer().getGameTimeDeltaPartialTick(false)) / 20.0f;
            float bob = (float) Math.sin((time + i * 0.4f) * Math.PI * 0.5f) * 0.05f;

            float renderX = cellCenterX - bounds.centerX() * scale;
            float renderY = cellCenterY + bounds.centerY() * scale;

            PoseStack stack = graphics.pose();
            stack.pushPose();
            stack.translate(renderX, renderY + bob * scale, 50.0f + (i * 2));
            stack.scale(scale, -scale, scale);

            mc.getEntityRenderDispatcher().setRenderShadow(false);
            mc.getEntityRenderDispatcher().render(
                    living,
                    0.0, 0.0, 0.0,
                    0.0f,
                    1.0f,
                    stack,
                    graphics.bufferSource,
                    LightTexture.pack(15, 15)
            );
            mc.getEntityRenderDispatcher().setRenderShadow(true);

            if (living.hasCustomName()) {
                String name = living.getName().getString();
                int width = font.width(name);
                float nameScale = Math.min(CELL_SIZE * 0.8f / width, 0.03f);
                float nameY = bounds.maxY() + 0.15F;

                stack.pushPose();
                stack.translate(0, nameY, 1);
                stack.scale(-nameScale, -nameScale, nameScale);

                graphics.fill(-width / 2 - 2, -1, width / 2 + 2, font.lineHeight + 1, 0xAA000000);
                stack.translate(0, nameY, 1);
                graphics.drawString(font, name, -width / 2, 0, 0xFFFFFF, false);
                stack.popPose();
            }

            stack.popPose();
        }
    }

    private record Bounds(float minX, float maxX, float minY, float maxY) {
        float width() { return maxX - minX; }
        float height() { return maxY - minY; }
        float centerX() { return (minX + maxX) * 0.5f; }
        float centerY() { return (minY + maxY) * 0.5f; }
    }

    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Bounds modelBounds(LivingEntity living) {
        if (!(living instanceof GeoEntity geoEntity)) return null;
        if (!(Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(living) instanceof GeoEntityRenderer geoRenderer)) return null;

        ResourceLocation modelRes = ((GeoModel) geoRenderer.getGeoModel()).getModelResource(geoEntity);
        if (BOUNDS_CACHE.containsKey(modelRes)) return BOUNDS_CACHE.get(modelRes);

        Bounds bounds = null;
        BakedGeoModel baked = GeckoLibCache.getBakedModels().get(modelRes);
        if (baked != null) {
            float[] mm = {Float.MAX_VALUE, -Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE};
            for (GeoBone bone : baked.topLevelBones()) accumulateBounds(bone, mm);
            if (mm[1] > mm[0]) bounds = new Bounds(mm[0], mm[1], mm[2], mm[3]);
        }
        BOUNDS_CACHE.put(modelRes, bounds);
        return bounds;
    }

    private static void accumulateBounds(GeoBone bone, float[] mm) {
        if (Boolean.TRUE.equals(bone.shouldNeverRender())) return;
        for (GeoCube cube : bone.getCubes()) {
            for (GeoQuad quad : cube.quads()) {
                if (quad == null) continue;
                for (GeoVertex vertex : quad.vertices()) {
                    Vector3f p = vertex.position();
                    mm[0] = Math.min(mm[0], p.x);
                    mm[1] = Math.max(mm[1], p.x);
                    mm[2] = Math.min(mm[2], p.y);
                    mm[3] = Math.max(mm[3], p.y);
                }
            }
        }
        for (GeoBone child : bone.getChildBones()) accumulateBounds(child, mm);
    }

    @Nullable
    private static LivingEntity getOrCreateEntity(CompoundTag tag, Level level) {
        LivingEntity cached = ENTITY_CACHE.get(tag);
        if (cached != null && cached.level() == level) {
            return cached;
        }

        EntityType<?> type = EntityType.byString(tag.getString("id")).orElse(null);
        if (type == null) return null;
        Entity entity = type.create(level);
        if (!(entity instanceof LivingEntity living)) return null;

        CompoundTag dataTag = tag.copy();
        dataTag.remove("id");
        dataTag.remove("CustomName");
        if (living instanceof Bucketable bucketable) bucketable.loadFromBucketTag(dataTag);
        if (living instanceof Catchable catchable) catchable.loadFromHandTag(dataTag);
        if (living instanceof AgeableMob ageableMob) ageableMob.setBaby(dataTag.getInt("Age") < 0);
        if (tag.contains("CustomName")) living.setCustomName(Component.literal(tag.getString("CustomName")));
        if (living instanceof WaterAnimal) living.wasTouchingWater = true;
        living.setOnGround(true);
        living.setYHeadRot(0);

        layerTooltipAnimation(living);

        if (ENTITY_CACHE.size() > 32) ENTITY_CACHE.clear();
        ENTITY_CACHE.put(tag.copy(), living);
        return living;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void layerTooltipAnimation(LivingEntity living) {
        if (!(living instanceof GeoEntity geoEntity)) return;
        if (!(Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(living) instanceof GeoEntityRenderer geoRenderer)) return;

        ResourceLocation animFile = ((GeoModel) geoRenderer.getGeoModel()).getAnimationResource(geoEntity);
        BakedAnimations baked = GeckoLibCache.getBakedAnimations().get(animFile);
        if (baked == null) return;

        String animName = null;
        for (String key : baked.animations().keySet()) {
            if (key.equals(TOOLTIP_ANIM_NAME) || key.endsWith("." + TOOLTIP_ANIM_NAME)) {
                animName = key;
                break;
            }
        }
        if (animName == null) return;

        AnimatableManager manager = geoEntity.getAnimatableInstanceCache().getManagerForId(living.getId());
        RawAnimation anim = RawAnimation.begin().thenPlay(animName);
        manager.addController(new AnimationController<>(geoEntity, "naturalist_tooltip", 0, state -> state.setAndContinue(anim)));
    }

    private int[] computeRowHeights() {
        int rows = (mobs.size() + MAX_PER_ROW - 1) / MAX_PER_ROW;
        int[] heights = new int[rows];
        for (int row = 0; row < rows; row++) heights[row] = rowHasName(row) ? 32 : 16;
        return heights;
    }

    private boolean rowHasName(int row) {
        for (int col = 0; col < MAX_PER_ROW; col++) {
            int index = row * MAX_PER_ROW + col;
            if (index >= mobs.size()) return false;
            if (mobs.getCompound(index).contains("CustomName")) return true;
        }
        return false;
    }
}
