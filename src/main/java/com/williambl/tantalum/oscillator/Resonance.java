package com.williambl.tantalum.oscillator;

import com.williambl.tantalum.Tantalum;
import com.williambl.tantalum.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.quiltmc.loader.api.QuiltLoader;

import java.util.function.Function;

public class Resonance extends Entity {
    public static boolean ENABLE_DEBUG_RENDERING = QuiltLoader.isDevelopmentEnvironment();

    private static final EntityDataAccessor<CompoundTag> RESONANCE_DATA = SynchedEntityData.defineId(Resonance.class, EntityDataSerializers.COMPOUND_TAG);

    public Resonance(EntityType<? extends Resonance> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public Resonance(Level level, Vec3 pos, float pitch, float yaw, double power, double frequency, Shape shape) {
        this(Tantalum.RESONANCE, level);
        this.setPos(pos);
        this.setYRot(pitch);
        this.setXRot(yaw);
        this.setPower(power);
        this.setFrequency(frequency);
        this.setShape(shape);
    }

    public double getPower() {
        return this.getEntityData().get(RESONANCE_DATA).getDouble("power");
    }

    public double getFrequency() {
        return this.getEntityData().get(RESONANCE_DATA).getDouble("frequency");
    }

    public Shape getShape() {
        return Shape.byName(this.getEntityData().get(RESONANCE_DATA).getString("shape"));
    }

    public void setPower(double value) {
        var data = new CompoundTag().merge(this.getEntityData().get(RESONANCE_DATA));
        data.putDouble("power", value);
        this.getEntityData().set(RESONANCE_DATA, data);
    }

    public void setFrequency(double value) {
        var data = new CompoundTag().merge(this.getEntityData().get(RESONANCE_DATA));
        data.putDouble("frequency", value);
        this.getEntityData().set(RESONANCE_DATA, data);
    }

    public void setShape(Shape shape) {
        var data = new CompoundTag().merge(this.getEntityData().get(RESONANCE_DATA));
        data.putString("shape", shape.getSerializedName());
        this.getEntityData().set(RESONANCE_DATA, data);
        this.setBoundingBox(this.makeBoundingBox());
    }

    public double resonanceFactor(Vec3 pos) {
        return this.getShape().sdf(this, pos, this.getPower());
    }

    @Override
    protected AABB makeBoundingBox() {
        return this.getShape().makeBoundingBox(this);
    }

    @Override
    public void tick() {
    }

    @Override
    protected void defineSynchedData() {
        this.getEntityData().define(RESONANCE_DATA, new CompoundTag());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.setPower(compound.getDouble("power"));
        this.setFrequency(compound.getDouble("frequency"));
        this.setShape(Shape.byName(compound.getString("shape")));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putDouble("power", this.getPower());
        compound.putDouble("frequency", this.getFrequency());
        compound.putString("shape", this.getShape().getSerializedName());
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    protected void addPassenger(Entity passenger) {
        passenger.stopRiding();
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    public enum Shape implements StringRepresentable {
        SPHERE(
                "sphere",
                (source, point, power) -> source.position().distanceTo(point) - power*2.5,
                resonance -> AABB.ofSize(resonance.position(), resonance.getPower() * 2.5 * 2, resonance.getPower() * 2.5 * 2, resonance.getPower() * 2.5 * 2)
        ),
        CYLINDER(
                "cylinder",
                (source, point, power) -> {
            Vec3 a = source.position();
            Vec3 b = source.position().add(source.getLookAngle().scale(source.getPower() * 5));
            double r = power;
            Vec3 ba = b.subtract(a);
            Vec3 pa = point.subtract(a);
            double baba = ba.dot(ba);
            double paba = pa.dot(ba);
            double x = pa.scale(baba).subtract(ba.scale(paba)).length() - r*baba;
            double y = Math.abs(paba-baba*0.5)-baba*0.5;
            double x2 = x*x;
            double y2 = y*y*baba;

            double d = (Math.max(x,y)<0.0)?-Math.min(x2,y2):(((x>0.0)?x2:0.0)+((y>0.0)?y2:0.0));

            return Math.signum(d)*Math.sqrt(Math.abs(d))/baba;
        },
                resonance -> AABB.ofSize(resonance.position().add(resonance.getLookAngle().scale(resonance.getPower() * 2.5)), resonance.getPower() * 2.5 * 2, resonance.getPower() * 2.5 * 2, resonance.getPower() * 2.5 * 2)
        ),
        ;

        private final SDF sdf;
        private final Function<Resonance, AABB> aabbFunction;
        private final String serializedName;

        Shape(String serializedName, SDF sdf, Function<Resonance, AABB> aabbFunction) {
            this.serializedName = serializedName;
            this.sdf = sdf;
            this.aabbFunction = aabbFunction;
        }

        @Override
        public String getSerializedName() {
            return this.serializedName;
        }

        private double sdf(Resonance source, Vec3 point, double power) {
            return this.sdf.compute(source, point, power);
        }

        private AABB makeBoundingBox(Resonance resonance) {
            return this.aabbFunction.apply(resonance);
        }

        public static Shape byName(String name) {
            for (var shape : Shape.values()) {
                if (shape.getSerializedName().equals(name)) {
                    return shape;
                }
            }

            return Shape.SPHERE;
        }

        @FunctionalInterface
        private interface SDF {
            double compute(Resonance source, Vec3 point, double power);
        }
    }
}
