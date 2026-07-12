package com.crispytwig.naturalist.server.entity.climbing;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

public class SurfaceCrawlerMoveControl extends MoveControl {
    private final SurfaceCrawler crawler;
    private Vec3 lastPos = Vec3.ZERO;
    private Vec3 lastWanted = Vec3.ZERO;
    private int stuckTicks;
    private int climbTicks;
    private int stuckCycles;

    public <T extends Mob & SurfaceCrawler> SurfaceCrawlerMoveControl(T mob) {
        super(mob);
        this.crawler = mob;
    }

    public void giveUp() {
        this.operation = Operation.WAIT;
        this.stuckTicks = 0;
        this.climbTicks = 0;
        this.stuckCycles = 0;
        this.mob.getNavigation().stop();
        this.crawler.getClimbing().halt();
    }

    @Override
    public void tick() {
        SurfaceClimbing climbing = this.crawler.getClimbing();
        if (this.operation != Operation.MOVE_TO || !climbing.isAttached()) {
            this.stuckTicks = 0;
            climbing.halt();
            return;
        }

        Vec3 wanted = new Vec3(this.wantedX, this.wantedY, this.wantedZ);
        if (!wanted.equals(this.lastWanted)) {
            this.lastWanted = wanted;
            this.stuckTicks = 0;
            this.climbTicks = 0;
            this.stuckCycles = 0;
        }

        Vec3 delta = wanted.subtract(this.mob.position());
        double distance = delta.length();
        if (distance < 0.1D) {
            this.operation = Operation.WAIT;
            this.stuckTicks = 0;
            climbing.halt();
            return;
        }

        this.trackProgress();

        Vec3 normal = climbing.getNormal();
        Vec3 tangent = SurfaceClimbing.projectOntoPlane(delta, normal);
        if (tangent.lengthSqr() < distance * distance * 0.04D) {
            tangent = SurfaceClimbing.projectOntoPlane(new Vec3(0.0D, 1.0D, 0.0D), normal);
            if (tangent.lengthSqr() < 0.01D) {
                tangent = new Vec3(delta.x, 0.0D, delta.z);
            }
        }
        if (this.climbTicks > 0) {
            this.climbTicks--;
            Vec3 climb = SurfaceClimbing.projectOntoPlane(new Vec3(0.0D, 1.0D, 0.0D), normal);
            if (climb.lengthSqr() > 0.01D) {
                tangent = climb;
            } else {
                Vec3 slide = normal.cross(tangent);
                if (slide.lengthSqr() > 1.0E-6D) {
                    tangent = slide;
                }
            }
        }
        if (tangent.lengthSqr() < 1.0E-7D) {
            climbing.halt();
            return;
        }

        tangent = tangent.normalize();
        double speed = this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
        this.mob.setSpeed((float) speed);
        climbing.setDesired(tangent, speed);

        if (tangent.horizontalDistanceSqr() > 1.0E-4D) {
            float yaw = (float) (Mth.atan2(tangent.z, tangent.x) * Mth.RAD_TO_DEG) - 90.0F;
            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), yaw, 20.0F));
            this.mob.yBodyRot = this.mob.getYRot();
        }
    }

    private void trackProgress() {
        Vec3 pos = this.mob.position();
        if (pos.distanceToSqr(this.lastPos) < 1.0E-4D) {
            if (++this.stuckTicks > 12 && this.climbTicks <= 0) {
                this.stuckTicks = 0;
                if (++this.stuckCycles >= 2) {
                    this.giveUp();
                } else {
                    this.climbTicks = 24;
                }
            }
        } else {
            this.stuckTicks = 0;
        }
        this.lastPos = pos;
    }
}
