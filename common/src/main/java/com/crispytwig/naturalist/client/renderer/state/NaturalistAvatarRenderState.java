package com.crispytwig.naturalist.client.renderer.state;

public interface NaturalistAvatarRenderState {
    boolean naturalist$shouldShoulderParrotsFlap();

    void naturalist$setShoulderParrotsFlap(boolean flap);

    boolean naturalist$skipNormalIKMountRender();

    void naturalist$setSkipNormalIKMountRender(boolean skip);
}
