package com.williambl.tantalum.turrets;

import com.williambl.tantalum.Tantalum;
import net.minecraft.world.phys.Vec3;

import java.util.function.*;

public class PredictiveTurretStrategy implements TurretStrategy {
    @Override
    public double maxDistance(int ticks, double maxProjectileSpeed) {
        return ticks * maxProjectileSpeed;
    }

    @Override
    public Vec3 predictedTargetPosition(Vec3 targetPos, Vec3 targetVel, int ticks) {
        return targetPos.add(targetVel.scale(ticks));
    }

    @Override
    public Vec3 calculateAim(Vec3 predictedTargetPosition, Vec3 initialPosition, double drag, Vec3 gravity, int ticks, double maxProjectileSpeed) {
        final Vec3 straightAim = predictedTargetPosition.subtract(initialPosition).normalize().scale(maxProjectileSpeed);
        int iterations = 20;
        double a = 0.0;
        double b = 1.0;
        Vec3 lastAim = getAim(straightAim, 0.5);
        double lastErrSqr = predictedTargetPosition.subtract(simpsonsThirdRule(createVelocityFunction(drag, lastAim, gravity), 0, ticks).add(initialPosition)).lengthSqr();
        double lastFactor = 0.5;
        for (int i = 1; i <= iterations; i++) {
            double factorLeft = lastFactor - 1.0/(1 << i);
            var aimLeft = getAim(straightAim, factorLeft);
            var errSqrLeft = factorLeft >= a ? calcErrSqr(predictedTargetPosition, drag, aimLeft, gravity, ticks, initialPosition) : Double.POSITIVE_INFINITY;

            double factorRight = lastFactor + 1.0/(1 << i);
            var aimRight = getAim(straightAim, factorRight);
            var errSqrRight = factorRight <= b ? calcErrSqr(predictedTargetPosition, drag, aimRight, gravity, ticks, initialPosition) : Double.POSITIVE_INFINITY;

            Tantalum.LOGGER.info("t: {} err: {} {} {} f: {} {} {}", ticks, errSqrLeft, lastErrSqr, errSqrRight, factorLeft, lastFactor, factorRight);
            if (errSqrLeft < lastErrSqr && errSqrLeft <= errSqrRight) {
                lastErrSqr = errSqrLeft;
                lastFactor = factorLeft;
                lastAim = aimLeft;
            } else if (errSqrRight < lastErrSqr && errSqrRight < errSqrLeft) {
                lastErrSqr = errSqrRight;
                lastFactor = factorRight;
                lastAim = aimRight;
            }

            if (lastErrSqr < 0.4) {
                return lastAim;
            }
        }

        return null;
    }

    private double calcErrSqr(Vec3 predictedTargetPosition, double drag, Vec3 aim, Vec3 gravity, double ticks, Vec3 initialPosition) {
        return predictedTargetPosition.subtract(simpsonsThirdRule(createVelocityFunction(drag, aim, gravity), 0, ticks).add(initialPosition)).lengthSqr();
    }

    private Vec3 getAim(Vec3 straightAimVec, double factor) {
        return straightAimVec.scale(1.0-factor).add(new Vec3(0.0, straightAimVec.length() * factor, 0.0));
    }

    private DoubleFunction<Vec3> createVelocityFunction(double drag, Vec3 u, Vec3 gravity) {
        return ticks -> u.add(gravity.scale(ticks)).scale(Math.pow(drag, ticks));
    }

    private Vec3 simpsonsThirdRule(DoubleFunction<Vec3> function, double a, double b) {
        double h = (b-a)/2.0;

        return function.apply(a).add(function.apply((a + b) / 2.0).scale(4)).add(function.apply(b)).scale(h / 3.0);
    }
}
