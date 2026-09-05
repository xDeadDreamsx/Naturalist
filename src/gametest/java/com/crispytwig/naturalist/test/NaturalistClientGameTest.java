package com.crispytwig.naturalist.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CameraType;

@SuppressWarnings("UnstableApiUsage")
public final class NaturalistClientGameTest implements FabricClientGameTest {
    public static final String WORLD_JOINED_MARKER = "NATURALIST_CLIENT_GAMETEST_WORLD_JOINED";
    public static final String BEHAVIOR_MOBS_TICKED_MARKER = "NATURALIST_CLIENT_GAMETEST_BEHAVIOR_MOBS_TICKED";
    public static final String RENDER_PARITY_MOBS_RENDERED_MARKER = "NATURALIST_CLIENT_GAMETEST_RENDER_PARITY_MOBS_RENDERED";
    public static final String MULTIPLAYER_JOINED_MARKER = "NATURALIST_CLIENT_GAMETEST_DEDICATED_JOINED";

    @Override
    public void runTest(ClientGameTestContext context) {
        CameraType previousCamera = context.computeOnClient(client -> client.options.getCameraType());
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_BACK));

            singleplayer.getServer().runCommand("/fill -16 99 -16 16 99 16 minecraft:stone");
            singleplayer.getServer().runCommand("/tp @a 0 100 0");
            singleplayer.getServer().runCommand("/time set 1000");

            // Existing behavior/animation regression coverage.
            singleplayer.getServer().runCommand("/summon naturalist:lion 3 100 0");
            singleplayer.getServer().runCommand("/summon naturalist:komodo_dragon -3 100 0");
            singleplayer.getServer().runCommand("/summon naturalist:snake 0 100 4");

            // 26.2 render-layer parity coverage. Elephant/Mammoth carry real banner inventory NBT,
            // which is synchronized to their renderer by customServerAiStep.
            singleplayer.getServer().runCommand("/summon naturalist:alligator -7 100 8");
            singleplayer.getServer().runCommand("/summon naturalist:elephant 0 100 7 {Items:[{Slot:26,id:\"minecraft:red_banner\",count:1}]} ");
            singleplayer.getServer().runCommand("/summon naturalist:mammoth 5 100 9 {Items:[{Slot:26,id:\"minecraft:blue_banner\",count:1}]} ");
            singleplayer.getServer().runCommand("/summon naturalist:giraffe -5 100 10");
            singleplayer.getServer().runCommand("/summon naturalist:ostrich 7 100 6");

            singleplayer.getConnection().waitForChunksRender();
            context.waitTicks(80);

            // Exercise the custom animated-seat render path in third person rather than merely
            // instantiating the layer. The rider is dismounted again before the rest of the test.
            singleplayer.getServer().runCommand("/ride @a[limit=1] mount @e[type=naturalist:elephant,limit=1,sort=nearest]");
            context.waitTicks(60);
            singleplayer.getConnection().waitForChunksRender();
            System.out.println(RENDER_PARITY_MOBS_RENDERED_MARKER);
            singleplayer.getServer().runCommand("/ride @a[limit=1] dismount");

            context.waitTicks(60);
            singleplayer.getConnection().waitForChunksRender();
            System.out.println(BEHAVIOR_MOBS_TICKED_MARKER);
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
