package com.crispytwig.naturalist.mixin;

import com.crispytwig.naturalist.server.entity.mob.WhalePart;
import com.crispytwig.naturalist.server.entity.util.MultipartLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Mixin(Level.class)
public abstract class LevelMixin implements MultipartLevel {
    @Unique
    private final Set<WhalePart> naturalist$whaleParts = new HashSet<>();

    @Override
    public void naturalist$addWhalePart(WhalePart part) {
        this.naturalist$whaleParts.add(part);
    }

    @Override
    public void naturalist$removeWhalePart(WhalePart part) {
        this.naturalist$whaleParts.remove(part);
    }

    @Override
    public WhalePart naturalist$getWhalePart(int id) {
        for (WhalePart part : this.naturalist$whaleParts) {
            if (part.getId() == id) {
                return part;
            }
        }
        return null;
    }

    @Inject(method = "getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;",
            at = @At("RETURN"))
    private void naturalist$includeWhaleParts(Entity except, AABB box, Predicate<? super Entity> predicate, CallbackInfoReturnable<List<Entity>> cir) {
        if (this.naturalist$whaleParts.isEmpty()) {
            return;
        }
        List<Entity> list = cir.getReturnValue();
        for (WhalePart part : this.naturalist$whaleParts) {
            if (part != except && part.getParent() != except && part.getBoundingBox().intersects(box) && predicate.test(part)) {
                list.add(part);
            }
        }
    }
}
