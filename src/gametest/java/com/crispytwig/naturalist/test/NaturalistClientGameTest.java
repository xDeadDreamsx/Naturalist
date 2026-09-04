package com.crispytwig.naturalist.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerConnection;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

@SuppressWarnings("UnstableApiUsage")
public final class NaturalistClientGameTest implements FabricClientGameTest {
    public static final String WORLD_JOINED_MARKER = "NATURALIST_CLIENT_GAMETEST_WORLD_JOINED";
    public static final String MULTIPLAYER_JOINED_MARKER = "NATURALIST_CLIENT_GAMETEST_DEDICATED_JOINED";

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
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
