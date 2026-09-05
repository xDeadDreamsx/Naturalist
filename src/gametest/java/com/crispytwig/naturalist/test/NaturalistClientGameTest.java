package com.crispytwig.naturalist.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CameraType;

/**
 * Real-client regression smoke test for the 26.2 Fabric port.
 *
 * <p>This intentionally exercises both an integrated singleplayer world and a dedicated-server
 * connection so CI verifies more than class loading.</p>
 */
@SuppressWarnings("UnstableApiUsage")
public final class NaturalistClientGameTest implements FabricClientGameTest {
    private static final String WORLD_JOINED_MARKER = "NATURALIST_CLIENT_GAMETEST_WORLD_JOINED";
    private static final String MULTIPLAYER_JOINED_MARKER = "NATURALIST_CLIENT_GAMETEST_DEDICATED_JOINED";
    private static final String RENDER_PARITY_MOBS_RENDERED_MARKER = "NATURALIST_CLIENT_GAMETEST_RENDER_PARITY_MOBS_RENDERED";
    private static final String BEHAVIOR_MOBS_TICKED_MARKER = "NATURALIST_CLIENT_GAMETEST_BEHAVIOR_MOBS_TICKED";

    @Override
    public void runTest(ClientGameTestContext context) {
        CameraType previousCamera = context.computeOnClient(client -> client.options.getCameraType());
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getServer().runCommand("/fill -16 99 -16 16 99 16 minecraft:stone");
            singleplayer.getServer().runCommand("/time set 1000");
            singleplayer.getServer().runCommand("/summon naturalist:lion -8 100 0");
            singleplayer.getServer().runCommand("/summon naturalist:komodo_dragon -4 100 0");
            singleplayer.getServer().runCommand("/summon naturalist:snake 0 100 0");
            singleplayer.getServer().runCommand("/summon naturalist:alligator 4 100 0");
            singleplayer.getServer().runCommand("/summon naturalist:elephant 8 100 0");
            singleplayer.getServer().runCommand("/summon naturalist:mammoth -8 100 5");
            singleplayer.getServer().runCommand("/summon naturalist:giraffe 0 100 5");
            singleplayer.getServer().runCommand("/summon naturalist:ostrich 8 100 5");
            singleplayer.getServer().runCommand("/tp @a 8 101 -8 0 5");

            context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_BACK));
            context.waitTicks(80);
            singleplayer.getConnection().waitForChunksRender();
            context.takeScreenshot("naturalist-render-parity-mobs");
            System.out.println(RENDER_PARITY_MOBS_RENDERED_MARKER);

            context.waitTicks(60);
            singleplayer.getConnection().waitForChunksRender();
            System.out.println(BEHAVIOR_MOBS_TICKED_MARKER);

            // Verify the actual Naturalist interaction path for tame, saddled elephant riding.
            NaturalistElephantRideTest.verify(context, singleplayer);

            // Reproduce the user-reported lion sleeping pose and inspect both root transforms.
            NaturalistLionSleepRenderTest.verify(context, singleplayer);

            // Exercise real target acquisition and attacks for the main land predators, not just
            // compilation of their goal setup.
            NaturalistPredatorBehaviorTest.verifyLandPredatorsHunt(context, singleplayer);

            // Repeat the same runtime check for the original aquatic predator/prey relationships.
            NaturalistPredatorBehaviorTest.verifyWaterPredatorsHunt(context, singleplayer);

            NaturalistVariantItemTest.verify(context);
            NaturalistContentParity.verify(context, singleplayer);
            System.out.println(WORLD_JOINED_MARKER);
        } finally {
            context.runOnClient(client -> client.options.setCameraType(previousCamera));
        }

        try (TestDedicatedServerContext server = context.worldBuilder().createServer()) {
            try (TestDedicatedServerConnection connection = server.connect()) {
                connection.waitForChunksRender();
                System.out.println(MULTIPLAYER_JOINED_MARKER);
            }
        }
    }
}
