package com.crispytwig.naturalist.test;

import com.crispytwig.naturalist.client.model.NaturalistEntityModel;
import com.crispytwig.naturalist.client.renderer.LionRenderer;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.mob.Lion;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CameraType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;

/** Render regression coverage for the two adult lion sleeping poses. */
@SuppressWarnings("UnstableApiUsage")
final class NaturalistLionSleepRenderTest {
    private NaturalistLionSleepRenderTest() {
    }

    static void verify(ClientGameTestContext context, TestSingleplayerContext world) {
        int[] lionIds = world.getServer().computeOnServer(server -> {
            var level = server.overworld();
            EntityType<?> lionType = BuiltInRegistries.ENTITY_TYPE.getValue(
                    Identifier.fromNamespaceAndPath("naturalist", "lion"));
            require(lionType != null, "Missing Naturalist lion type");

            Lion regularSleep = null;
            Lion alternateSleep = null;
            for (int attempt = 0; attempt < 64 && (regularSleep == null || alternateSleep == null); attempt++) {
                Entity entity = lionType.create(level, EntitySpawnReason.COMMAND);
                require(entity instanceof Lion, "Could not create Naturalist lion");
                Lion candidate = (Lion) entity;
                if (candidate.usesAltSleepPose()) {
                    if (alternateSleep == null) {
                        alternateSleep = candidate;
                    }
                } else if (regularSleep == null) {
                    regularSleep = candidate;
                }
            }

            require(regularSleep != null, "Could not create regular lion sleep variant");
            require(alternateSleep != null, "Could not create alternate lion sleep variant");
            prepareSleepingLion(regularSleep, -2.4D, 100.0D, 0.0D);
            prepareSleepingLion(alternateSleep, 2.4D, 100.0D, 0.0D);
            require(level.addFreshEntity(regularSleep), "Could not add regular sleeping lion");
            require(level.addFreshEntity(alternateSleep), "Could not add alternate sleeping lion");
            return new int[]{regularSleep.getId(), alternateSleep.getId()};
        });

        context.waitTicks(50);

        context.runOnClient(client -> {
            require(client.level != null, "Client level missing during lion sleep render test");
            for (int id : lionIds) {
                Entity entity = client.level.getEntity(id);
                require(entity instanceof Lion, "Sleeping lion did not synchronize to client");
                Lion lion = (Lion) entity;

                EntityRenderer<? super Lion, ?> renderer = client.getEntityRenderDispatcher().getRenderer(lion);
                require(renderer instanceof LionRenderer, "Lion did not resolve to Naturalist LionRenderer");
                LionRenderer lionRenderer = (LionRenderer) renderer;
                NaturalistRenderState<Lion> state = lionRenderer.createRenderState();
                lionRenderer.extractRenderState(lion, state, 1.0F);
                NaturalistEntityModel<Lion> model = lionRenderer.getModel();
                model.setupAnim(state);

                float rootY = model.root().y;
                float rootZRot = model.root().zRot;
                float blend = Math.max(lion.sleepAnimationState.factor(1.0F), lion.sleep2AnimationState.factor(1.0F));
                require(blend > 0.95F, "Lion sleeping pose never reached a stable animation blend");
                require(Math.abs(rootY - 24.0F) > 3.0F,
                        "Lion sleep animation did not apply its root translation");
                if (lion.usesAltSleepPose()) {
                    require(Math.abs(rootZRot) > 1.2F,
                            "Alternate lion sleeping pose lost its root rotation");
                }
                System.out.println("NATURALIST_LION_SLEEP_ROOT: alt=" + lion.usesAltSleepPose()
                        + " y=" + rootY + " zRot=" + rootZRot + " blend=" + blend);
            }
        });

        CameraType previousCamera = context.computeOnClient(client -> client.options.getCameraType());
        context.runOnClient(client -> client.options.setCameraType(CameraType.FIRST_PERSON));
        try {
            world.getServer().runCommand("/tp @a 0 101 -11 0 5");
            context.waitTicks(20);
            world.getConnection().waitForChunksRender();
            context.takeScreenshot("naturalist-lion-sleep-poses");
        } finally {
            context.runOnClient(client -> client.options.setCameraType(previousCamera));
        }
        System.out.println("NATURALIST_LION_SLEEP_RENDER: both sleeping poses rendered");
    }

    private static void prepareSleepingLion(Lion lion, double x, double y, double z) {
        lion.setAge(0);
        lion.setNoAi(true);
        lion.setSleeping(true);
        lion.setPersistenceRequired();
        lion.snapTo(x, y, z, 180.0F, 0.0F);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
