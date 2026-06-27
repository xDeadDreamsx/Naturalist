package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.server.entity.util.TerrainLegSolver;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

public abstract class IKGeoModel<T extends GeoAnimatable> extends GeoModel<T> {
    protected abstract TerrainLegSolver getLegSolver(T entity);

    protected abstract String headBone();

    protected abstract String[] legBones(T entity);

    protected String bodyBone() {
        return "body";
    }

    protected float legStrength() {
        return 1.0F;
    }

    protected float pitchStrength() {
        return 1.0F;
    }

    protected float rollStrength() {
        return 0.5F;
    }

    protected float headCounterStrength() {
        return 1.0F;
    }

    protected void articulateLegs(T entity, float partialTick) {
        TerrainLegSolver solver = getLegSolver(entity);
        float hBackLeft = solver.backLeft.getHeight(partialTick);
        float hBackRight = solver.backRight.getHeight(partialTick);
        float hFrontLeft = solver.frontLeft.getHeight(partialTick);
        float hFrontRight = solver.frontRight.getHeight(partialTick);

        float backAvg = (hBackLeft + hBackRight) * 0.5F;
        float frontAvg = (hFrontLeft + hFrontRight) * 0.5F;
        float leftAvg = (hBackLeft + hFrontLeft) * 0.5F;
        float rightAvg = (hBackRight + hFrontRight) * 0.5F;
        float avg = (backAvg + frontAvg) * 0.5F;
        float pitch = (float) Math.atan2(frontAvg - backAvg, 2F * solver.frontLeft.forward) * pitchStrength();
        float roll = (float) Math.atan2(leftAvg - rightAvg, 2F * solver.frontLeft.side) * rollStrength();

        this.getBone(bodyBone()).ifPresent(b -> {
            b.setPosY(b.getPosY() - avg * 16F * legStrength());
            b.setRotX(b.getRotX() - pitch);
            b.setRotZ(b.getRotZ() + roll);
            b.resetStateChanges();
        });
        this.getBone(headBone()).ifPresent(h -> {
            h.setRotX(h.getRotX() + pitch * headCounterStrength());
            h.setRotZ(h.getRotZ() - roll * headCounterStrength());
            h.resetStateChanges();
        });

        String[] legs = legBones(entity);
        offsetFoot(legs[0], hBackLeft);
        offsetFoot(legs[1], hBackRight);
        offsetFoot(legs[2], hFrontLeft);
        offsetFoot(legs[3], hFrontRight);
    }

    private void offsetFoot(String bone, float height) {
        this.getBone(bone).ifPresent(b -> {
            b.setPosY(b.getPosY() - height * 16F * legStrength());
            b.resetStateChanges();
        });
    }
}
