package com.crispytwig.naturalist.mixin;

import com.crispytwig.naturalist.server.entity.mob.Hedgehog;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EnchantedCountIncreaseFunction.class)
public class EnchantedCountIncreaseFunctionMixin {
    @Redirect(method = "run(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/storage/loot/LootContext;)Lnet/minecraft/world/item/ItemStack;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentLevel(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int naturalist$addHedgehogLooting(Holder<Enchantment> enchantment, LivingEntity attacker, ItemStack stack, LootContext context) {
        int level = EnchantmentHelper.getEnchantmentLevel(enchantment, attacker);
        if (enchantment.is(Enchantments.LOOTING) && context.getParamOrNull(LootContextParams.DIRECT_ATTACKING_ENTITY) instanceof Hedgehog hedgehog) {
            level += hedgehog.getThrowEnchantmentLevel(Enchantments.LOOTING);
        }
        return level;
    }
}
