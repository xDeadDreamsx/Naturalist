package com.crispytwig.naturalist.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

@SuppressWarnings("UnstableApiUsage")
public final class NaturalistClientGameTest implements FabricClientGameTest {
    public static final String WORLD_JOINED_MARKER = "NATURALIST_CLIENT_GAMETEST_WORLD_JOINED";
    public static final String BEHAVIOR_MOBS_TICKED_MARKER = "NATURALIST_CLIENT_GAMETEST_BEHAVIOR_MOBS_TICKED";
    public static final String MULTIPLAYER_JOINED_MARKER = "NATURALIST_CLIENT_GAMETEST_DEDICATED_JOINED";

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getServer().runCommand("/fill -12 99 -12 12 99 12 minecraft:stone");
            singleplayer.getServer().runCommand("/tp @a 0 100 0");
            singleplayer.getServer().runCommand("/time set 1000");
            singleplayer.getServer().runCommand("/summon naturalist:lion 3 100 0");
            singleplayer.getServer().runCommand("/summon naturalist:komodo_dragon -3 100 0");
            singleplayer.getServer().runCommand("/summon naturalist:snake 0 100 4");

            singleplayer.getConnection().waitForChunksRender();
            context.waitTicks(200);
            singleplayer.getConnection().waitForChunksRender();
            System.out.println(BEHAVIOR_MOBS_TICKED_MARKER);
            System.out.println(WORLD_JOINED_MARKER);
        }

        try (TestDedicatedServerContext server = context.worldBuilder().createServer()) {
            try (TestDedicatedServerConnection connection = server.connect()) {
                connection.waitForChunksRender();
                System.out.println(MULTIPLAYER_JOINED_MARKER);
            }
        }
    }
}
