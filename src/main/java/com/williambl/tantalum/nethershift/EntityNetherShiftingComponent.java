package com.williambl.tantalum.nethershift;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class EntityNetherShiftingComponent implements NetherShiftingComponent {
    private final Entity entity;

    private ShiftingState shiftingState = new ShiftingState.NotShifting();

    public EntityNetherShiftingComponent(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void startShift(int ticksToShift) {
        this.shiftingState = new ShiftingState.Charging(ticksToShift, this.entity.level.getGameTime()+ticksToShift, this.entity.position());
        NetherShiftingComponent.KEY.sync(this.entity);
    }

    @Override
    public boolean isCharging() {
        return this.shiftingState instanceof ShiftingState.Charging;
    }

    @Override
    public boolean isShifting() {
        return this.shiftingState instanceof ShiftingState.Shifting;
    }

    @Override
    public void readFromNbt(CompoundTag tag) {

    }

    @Override
    public void writeToNbt(CompoundTag tag) {

    }

    @Override
    public boolean shouldSyncWith(ServerPlayer player) {
        return player == this.entity;
    }

    @Override
    public void writeSyncPacket(FriendlyByteBuf buf, ServerPlayer recipient) {
        this.shiftingState.toBytes(buf);
    }

    @Override
    public void applySyncPacket(FriendlyByteBuf buf) {
        this.shiftingState = ShiftingState.fromBuf(buf);
    }

    @Override
    public void clientTick() {
    }

    @Override
    public void serverTick() {
        var oldState = this.shiftingState;
        this.shiftingState = this.shiftingState.nextState(this.entity);

        if (oldState instanceof ShiftingState.Charging && this.shiftingState instanceof ShiftingState.Shifting shifting) {
            var destination = shifting.destination();
            this.entity.teleportToWithTicket(destination.x(), destination.y(), destination.z());
            this.entity.setDeltaMovement(Vec3.ZERO);
            if (this.entity instanceof ServerPlayer player) {
                player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), player.getDeltaMovement()));
            }
        } else if (oldState instanceof ShiftingState.Shifting shifting) {
            this.entity.setDeltaMovement(shifting.velocity());
            if (this.entity instanceof ServerPlayer player) {
                player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), player.getDeltaMovement()));
            }
        }

        if (this.shiftingState instanceof ShiftingState.Shifting shifting) {
            var destination = shifting.destination();
            this.entity.teleportToWithTicket(destination.x(), destination.y(), destination.z());
        }

        if (oldState != this.shiftingState) {
            NetherShiftingComponent.KEY.sync(this.entity);
        }
    }

    private sealed interface ShiftingState {
        ShiftingState nextState(Entity entity);
        void writeCustomToBytes(FriendlyByteBuf buf);

        default void toBytes(FriendlyByteBuf buf) {
            buf.writeVarInt(
                    this instanceof NotShifting ? 0 : this instanceof Charging ? 1 : 2
            ); // change me to a pattern-matching switch

            this.writeCustomToBytes(buf);
        }

        static ShiftingState fromBuf(FriendlyByteBuf buf) {
            return switch (buf.readVarInt()) {
                case 0 -> NotShifting.fromBuf(buf);
                case 1 -> Charging.fromBuf(buf);
                default -> Shifting.fromBuf(buf);
            };
        }

        final class NotShifting implements ShiftingState {
            @Override
            public ShiftingState nextState(Entity entity) {
                return this;
            }

            @Override
            public void writeCustomToBytes(FriendlyByteBuf buf) {
                // no-op
            }

            static NotShifting fromBuf(FriendlyByteBuf buf) {
                return new NotShifting();
            }
        }

        record Charging(int ticksToShift, long timeChargingComplete, Vec3 posAtChargeStart) implements ShiftingState {
            @Override
            public ShiftingState nextState(Entity entity) {
                return this.timeChargingComplete() - entity.getLevel().getGameTime() <= 0 ?
                        new Shifting(
                                this.ticksToShift(),
                                entity.getLevel().getGameTime() + this.ticksToShift(),
                                entity.position().add(entity.position().subtract(this.posAtChargeStart()).scale(8.0)),
                                entity.position().subtract(entity.xOld, entity.yOld, entity.zOld)
                        )
                        : this;
            }

            @Override
            public void writeCustomToBytes(FriendlyByteBuf buf) {
                buf.writeVarInt(this.ticksToShift());
                buf.writeVarLong(this.timeChargingComplete());
                buf.writeDouble(this.posAtChargeStart().x());
                buf.writeDouble(this.posAtChargeStart().y());
                buf.writeDouble(this.posAtChargeStart().z());
            }

            static Charging fromBuf(FriendlyByteBuf buf) {
                return new Charging(buf.readVarInt(), buf.readVarLong(), new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
            }
        }

        record Shifting(int ticksToShift, long timeShiftingComplete, Vec3 destination, Vec3 velocity) implements ShiftingState {
            @Override
            public ShiftingState nextState(Entity entity) {
                return this.timeShiftingComplete() - entity.getLevel().getGameTime() <= 0 ?
                        new NotShifting()
                        : this;
            }

            @Override
            public void writeCustomToBytes(FriendlyByteBuf buf) {
                buf.writeVarInt(this.ticksToShift());
                buf.writeVarLong(this.timeShiftingComplete());
                buf.writeDouble(this.destination().x());
                buf.writeDouble(this.destination().y());
                buf.writeDouble(this.destination().z());
                buf.writeDouble(this.velocity().x());
                buf.writeDouble(this.velocity().y());
                buf.writeDouble(this.velocity().z());
            }

            static Shifting fromBuf(FriendlyByteBuf buf) {
                return new Shifting(buf.readVarInt(), buf.readVarLong(), new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()), new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
            }
        }
    }
}
