package com.crispytwig.naturalist.registry;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.block.entity.AntHillBlockEntity;
import com.crispytwig.naturalist.server.block.entity.SnailShellBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.crispytwig.naturalist.platform.registry.DeferredHolder;
import com.crispytwig.naturalist.platform.registry.DeferredRegister;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

public class NaturalistBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Naturalist.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AntHillBlockEntity>> ANT_HILL = BLOCK_ENTITY_TYPES.register("ant_hill", () -> FabricBlockEntityTypeBuilder.create(AntHillBlockEntity::new, NaturalistRegistry.ANT_HILL.get()).build());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SnailShellBlockEntity>> SNAIL_SHELL = BLOCK_ENTITY_TYPES.register("snail_shell", () -> FabricBlockEntityTypeBuilder.create(SnailShellBlockEntity::new, NaturalistRegistry.SNAIL_SHELL_BLOCK.get()).build());
}
