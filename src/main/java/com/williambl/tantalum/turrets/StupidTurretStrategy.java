package com.williambl.tantalum.turrets;

import net.minecraft.world.phys.Vec3;

public class StupidTurretStrategy implements TurretStrategy {
    @Override
    public double maxDistance(int ticks, double maxProjectileSpeed) {
        return ticks * maxProjectileSpeed;
    }

    @Override
    public Vec3 predictedTargetPosition(Vec3 targetPos, Vec3 targetVel, int ticks) {
        return targetPos;
    }

    @Override
    public Vec3 calculateAim(Vec3 predictedTargetPosition, Vec3 initialPosition, double drag, Vec3 gravity, int ticks, double maxProjectileSpeed) {
        return predictedTargetPosition.subtract(initialPosition).normalize().scale(maxProjectileSpeed);
    }
}
