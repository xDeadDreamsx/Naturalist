package com.crispytwig.naturalist.compat;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.*;
import com.crispytwig.naturalist.server.entity.variant.DataDrivenVariantAnimal;
import com.crispytwig.naturalist.server.entity.variant.MobVariant;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.fish.WaterAnimal;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Optional Field Guide integration for the 26.2 Fabric port.
 *
 * <p>Field Guide is deliberately not a hard dependency of Naturalist. The 1.21.1 build
 * registered a VariantProvider when Field Guide was present; this adapter restores that
 * behaviour without linking Field Guide classes when the mod is absent.</p>
 */
public final class FieldGuideCompat {
    private static final List<Class<? extends Mob>> VARIANT_MOBS = List.of(
            Alligator.class, Anglerfish.class, Ant.class, Bass.class, Bear.class,
            Bird.class, BlackBear.class, Blobfish.class, Boar.class, Butterfly.class,
            Capybara.class, Caterpillar.class, Catfish.class, Clam.class, Crab.class, Deer.class,
            DesertScorpion.class, Dragonfly.class, Duck.class, Elephant.class, Firefly.class,
            GiantIsopod.class, Giraffe.class, GreatWhiteShark.class, Hedgehog.class, Hippo.class, Jellyfish.class,
            JungleScorpion.class, KomodoDragon.class, Lion.class, Lizard.class, Mammoth.class,
            Mole.class, Ostrich.class, Piranha.class, Rat.class, Ray.class,
            Rhino.class, Snail.class, Snake.class, Starfish.class, Tiger.class,
            Tortoise.class, Turkey.class, Vulture.class, Whale.class, Zebra.class
    );

    private FieldGuideCompat() {
    }

    public static void register() {
        try {
            ClassLoader loader = FieldGuideCompat.class.getClassLoader();
            Class<?> providerType = Class.forName("com.evandev.fieldguide.api.variant.VariantProvider", true, loader);
            Class<?> variantDefType = Class.forName("com.evandev.fieldguide.api.variant.VariantDef", true, loader);
            Class<?> managerType = Class.forName("com.evandev.fieldguide.variant.FieldGuideVariantManager", true, loader);

            Constructor<?> variantDefConstructor = variantDefType.getConstructor(String.class, Object.class);
            Method variantValue = variantDefType.getMethod("value");
            Method registerProvider = managerType.getMethod("registerProvider", Class.class, providerType);

            Object provider = Proxy.newProxyInstance(loader, new Class<?>[]{providerType}, (proxy, method, args) -> {
                String name = method.getName();
                if ("getVariants".equals(name)) {
                    return variants(args != null && args.length > 0 ? args[0] : null, variantDefConstructor);
                }
                if ("apply".equals(name)) {
                    if (args != null && args.length >= 2 && args[0] instanceof Mob mob && mob instanceof DataDrivenVariantAnimal animal) {
                        Object value = variantValue.invoke(args[1]);
                        if (value instanceof Identifier id) {
                            animal.setVariantString(id.toString());
                            keepAquaticPreviewInWater(mob);
                        }
                    }
                    return null;
                }
                if ("getCurrent".equals(name)) {
                    if (args != null && args.length > 0 && args[0] instanceof DataDrivenVariantAnimal animal) {
                        Identifier id = animal.getVariantLocation();
                        return variantDefConstructor.newInstance(id.getPath(), id);
                    }
                    return null;
                }
                if ("getCacheKey".equals(name)) {
                    return args != null && args.length > 0 && args[0] != null
                            ? args[0].getClass().getName()
                            : FieldGuideCompat.class.getName();
                }
                if ("applyToRenderState".equals(name)) {
                    return null;
                }
                if ("toString".equals(name)) {
                    return "NaturalistFieldGuideVariantProvider";
                }
                if ("hashCode".equals(name)) {
                    return System.identityHashCode(proxy);
                }
                if ("equals".equals(name)) {
                    return args != null && args.length > 0 && proxy == args[0];
                }
                return method.getDefaultValue();
            });

            for (Class<? extends Mob> entityClass : VARIANT_MOBS) {
                registerProvider.invoke(null, entityClass, provider);
            }
            Naturalist.LOGGER.info("Enabled Naturalist Field Guide variant integration for Minecraft 26.2");
        } catch (ClassNotFoundException ignored) {
            // Field Guide is optional.
        } catch (ReflectiveOperationException | LinkageError e) {
            Naturalist.LOGGER.warn("Could not initialize Naturalist Field Guide 26.2 compatibility", e);
        }
    }

    private static List<Object> variants(Object entityObject, Constructor<?> variantDefConstructor) throws ReflectiveOperationException {
        if (!(entityObject instanceof Mob mob) || !(mob instanceof DataDrivenVariantAnimal animal)) {
            return List.of();
        }

        Optional<Registry<MobVariant>> registry = mob.level().registryAccess().lookup(animal.getVariantRegistryKey());
        if (registry.isEmpty() || registry.get().size() <= 1) {
            return List.of();
        }

        Identifier defaultId = animal.getDefaultVariant().identifier();
        List<Identifier> ids = registry.get().listElements()
                .map(Holder.Reference::unwrapKey)
                .flatMap(Optional::stream)
                .map(key -> key.identifier())
                .toList();

        List<Object> result = new ArrayList<>(ids.size());
        if (ids.contains(defaultId)) {
            result.add(variantDefConstructor.newInstance(defaultId.getPath(), defaultId));
        }
        for (Identifier id : ids) {
            if (!id.equals(defaultId)) {
                result.add(variantDefConstructor.newInstance(id.getPath(), id));
            }
        }
        return result;
    }

    private static void keepAquaticPreviewInWater(Mob mob) {
        if (mob instanceof WaterAnimal || mob instanceof Whale || mob instanceof GreatWhiteShark) {
            mob.wasTouchingWater = true;
        }
    }
}
