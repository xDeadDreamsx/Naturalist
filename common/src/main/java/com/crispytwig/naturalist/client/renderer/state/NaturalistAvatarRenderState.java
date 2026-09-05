package com.crispytwig.naturalist.client.renderer.state;

public interface NaturalistAvatarRenderState {
    boolean naturalist$shouldShoulderParrotsFlap();

    void naturalist$setShoulderParrotsFlap(boolean flap);

    boolean naturalist$skipNormalIKMountRender();

    void naturalist$setSkipNormalIKMountRender(boolean skip);

    float naturalist$ikMountPitch();

    void naturalist$setIKMountPitch(float pitch);

    float naturalist$ikMountRoll();

    void naturalist$setIKMountRoll(float roll);
}
