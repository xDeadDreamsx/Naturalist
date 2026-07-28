package com.crispytwig.naturalist.platform;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.io.InputStream;
import java.util.*;

public class NaturalistMixinPlugin implements IMixinConfigPlugin {
    private static final String[] SHADERS = {
            "net/irisshaders/iris/Iris.class",
            "net/coderbot/iris/Iris.class",
            "net/optifine/shaders/Shaders.class"
    };

    private static final Set<String> INCOMPATIBLE_MIXINS = Set.of(
            "com.crispytwig.naturalist.mixin.LevelRendererMixin"
    );

    private boolean shaderModPresent;

    @Override
    public void onLoad(String mixinPackage) {
        for (String path : SHADERS) {
            try (InputStream stream = MixinService.getService().getResourceAsStream(path)) {
                if (stream != null) {
                    this.shaderModPresent = true;
                    return;
                }
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return !this.shaderModPresent || !INCOMPATIBLE_MIXINS.contains(mixinClassName);
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
