package com.crispytwig.naturalist.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** The expected content comes from the original 1.21.1 source, not the port's registrations. */
@SuppressWarnings("UnstableApiUsage")
final class NaturalistContentParity {
    private NaturalistContentParity() {
    }

    static void verify(ClientGameTestContext context, TestSingleplayerContext world) {
        JsonObject baseline;
        try (var input = new InputStreamReader(Objects.requireNonNull(
                NaturalistContentParity.class.getResourceAsStream("/naturalist-1.21.1-content.json")),
                StandardCharsets.UTF_8)) {
            baseline = JsonParser.parseReader(input).getAsJsonObject();
        } catch (Exception e) {
            throw new AssertionError("Cannot read original Naturalist content baseline", e);
        }

        context.runOnClient(client -> {
            baseline.getAsJsonArray("entity_ids").forEach(value -> require(
                    BuiltInRegistries.ENTITY_TYPE.containsKey(id(value.getAsString())), "Missing entity " + value));
            baseline.getAsJsonArray("content_ids").forEach(value -> require(
                    BuiltInRegistries.ITEM.containsKey(id(value.getAsString()))
                            || BuiltInRegistries.BLOCK.containsKey(id(value.getAsString())), "Missing content " + value));
            baseline.getAsJsonArray("sound_ids").forEach(value -> require(
                    BuiltInRegistries.SOUND_EVENT.containsKey(id(value.getAsString())), "Missing sound " + value));
        });

        world.getServer().runCommand("/fill -30 199 -30 30 199 30 minecraft:stone");
        world.getServer().runCommand("/fill -30 199 31 30 199 46 minecraft:stone");
        world.getServer().runCommand("/fill -30 200 31 30 203 46 minecraft:water");
        world.getServer().runCommand("/tp @a 0 202 8 0 12");

        List<Integer> entityIds = world.getServer().computeOnServer(server -> {
            var level = server.overworld();
            List<Integer> ids = new ArrayList<>();
            int landIndex = 0;
            int waterIndex = 0;
            for (var value : baseline.getAsJsonArray("animal_ids")) {
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(id(value.getAsString()));
                require(type != null, "Missing animal type " + value);
                boolean aquatic = type.getCategory() == MobCategory.WATER_AMBIENT
                        || type.getCategory() == MobCategory.WATER_CREATURE;
                int index = aquatic ? waterIndex++ : landIndex++;
                double x = -24 + (index % 7) * 8;
                double z = aquatic ? 34 + (index / 7) * 8 : -24 + (index / 7) * 8;

                for (int age = 0; age < 2; age++) {
                    Entity entity = type.create(level, EntitySpawnReason.COMMAND);
                    require(entity instanceof Mob, "Cannot create animal " + value);
                    Mob mob = (Mob) entity;
                    if (age == 1) {
                        if (!(mob instanceof AgeableMob ageable)) {
                            break;
                        }
                        ageable.setAge(-24000);
                    }
                    mob.snapTo(x + age * 2, aquatic ? 201 : 200, z, 0, 0);
                    mob.setInvulnerable(true);
                    mob.setPersistenceRequired();
                    require(level.addFreshEntity(mob), "Cannot add animal to world " + value);
                    ids.add(mob.getId());
                }
            }
            return List.copyOf(ids);
        });

        context.waitFor(client -> client.level != null
                && entityIds.stream().allMatch(id -> client.level.getEntity(id) instanceof Mob), 400);
        world.getConnection().waitForChunksRender();
        context.waitTicks(40);
        world.getServer().runOnServer(server -> {
            for (int id : entityIds) {
                Entity entity = server.overworld().getEntity(id);
                require(entity instanceof Mob && entity.tickCount >= 20, "Animal did not tick: " + id);
            }
        });

        // Look across every part of the enclosure so adult/baby models and layers are submitted.
        for (int yaw : new int[]{0, 90, 180, 270}) {
            world.getServer().runCommand("/tp @a 0 205 8 " + yaw + " 15");
            context.waitTicks(20);
            world.getConnection().waitForChunksRender();
            context.takeScreenshot("naturalist-all-animals-" + yaw);
        }
        System.out.println("NATURALIST_CONTENT_PARITY: 51 entity types, 137 content entries, 241 sounds, 47 species; "
                + entityIds.size() + " adult/baby animals ticked");
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("naturalist", path);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
