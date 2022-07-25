package com.williambl.tantalum.turrets;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface TurretStrategy {
    double maxDistance(int ticks, double maxProjectileSpeed);
    Vec3 predictedTargetPosition(Vec3 targetPos, Vec3 targetVel, int ticks);
    @Nullable Vec3 calculateAim(Vec3 predictedTargetPosition, Vec3 initialPosition, double drag, Vec3 gravity, int ticks, double maxProjectileSpeed);
}
