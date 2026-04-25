package com.starfish_studios.naturalist.client.model;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.server.entity.mob.Snake;
import com.starfish_studios.naturalist.registry.NaturalistEntityTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

@OnlyIn(Dist.CLIENT)
public class SnakeModel extends GeoModel<Snake> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Snake snake) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/snake.geo.json");
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(@NotNull Snake snake) {
        if (snake.getType().equals(NaturalistEntityTypes.CORAL_SNAKE.get())) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/snake/coral_snake.png");
        } else if (snake.getType().equals(NaturalistEntityTypes.RATTLESNAKE.get())) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/snake/rattle_snake.png");
        } else {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/snake/green_snake.png");
        }
    }

    @Override
    public ResourceLocation getAnimationResource(Snake snake) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/snake.animation.json");
    }

    @Override
    public void setCustomAnimations(Snake entity, long instanceId, AnimationState<Snake> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        if (animationState == null) return;

        EntityModelData extraDataOfType = animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        this.getBone("skull").ifPresent(head -> {
            if (!entity.isSleeping()) {
                head.setRotX(head.getRotX() + extraDataOfType.headPitch() * Mth.DEG_TO_RAD);
                head.setRotY(head.getRotY() + extraDataOfType.netHeadYaw() * Mth.DEG_TO_RAD);
                head.resetStateChanges();
            }
        });

        this.getBone("tail2").ifPresent(tail2 -> {
            if (!entity.getMainHandItem().isEmpty()) {
                tail2.setScaleX(1.5F);
                tail2.setScaleY(1.5F);
            } else {
                tail2.setScaleX(1.0F);
                tail2.setScaleY(1.0F);
            }
            tail2.resetStateChanges();
        });

        this.getBone("tail4").ifPresent(tail4 -> tail4.setHidden(!entity.getType().equals(NaturalistEntityTypes.RATTLESNAKE.get())));

        boolean sleeping = entity.isSleeping();
        this.getBone("awake").ifPresent(bone -> bone.setHidden(sleeping));
        this.getBone("asleep").ifPresent(bone -> bone.setHidden(!sleeping));
        this.getBone("sleep").ifPresent(bone -> bone.setHidden(!sleeping));
    }
}
