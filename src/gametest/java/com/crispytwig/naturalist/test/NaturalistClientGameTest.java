package com.crispytwig.naturalist.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

@SuppressWarnings("UnstableApiUsage")
public final class NaturalistClientGameTest implements FabricClientGameTest {
    public static final String WORLD_JOINED_MARKER = "NATURALIST_CLIENT_GAMETEST_WORLD_JOINED";

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getConnection().waitForChunksRender();
            System.out.println(WORLD_JOINED_MARKER);
        }
    }
}
