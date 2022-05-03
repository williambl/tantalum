package com.williambl.tantalum.gases;

import com.williambl.tantalum.Tantalum;
import com.williambl.tantalum.laser.LaserEmitterBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.PlayerLookup;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import reborncore.common.blockentity.MachineBaseBlockEntity;
import reborncore.common.powerSystem.PowerAcceptorBlockEntity;
import techreborn.init.ModFluids;

import static com.williambl.tantalum.Tantalum.id;
import static net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants.BUCKET;
import static net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants.DROPLET;

@SuppressWarnings("UnstableApiUsage")
public class FluidPipeBlockEntity extends BlockEntity implements HasTank {
    private final FluidTank tank = new FluidTank(BUCKET * 10) {
        @Override
        public void onCloseExtra(TransactionContext transaction, TransactionContext.Result result) {
            FluidPipeBlockEntity.this.setChanged();
        }
    };

    public FluidPipeBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(Tantalum.FLUID_PIPE_BLOCK_ENTITY, blockPos, blockState);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        var fluidTag = this.tank.toNbt();
        tag.put("tank", fluidTag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.tank.fromNbt(tag.getCompound("tank"));
    }

    @Override
    public FluidTank getTank(Direction context) {
        return this.tank;
    }

    @Nullable
    public FluidTank getFluidTank() {
        return this.tank;
    }

    @Override
    public CompoundTag getUpdateTag() {
        var tag = super.getUpdateTag();
        this.saveAdditional(tag);
        return tag;
    }

    public static void tickEntity(Level level, BlockPos blockPos, BlockState blockState, FluidPipeBlockEntity e) {
        if (level instanceof ServerLevel sLevel) {
            PipeManager.addPipe(sLevel, blockPos, e);
        }
    }

    /**
     * Sync this block entity. Only really used when debugging.
     */
    public void sync() {
        var buf = PacketByteBufs.create();
        buf.writeBlockPos(this.getBlockPos());
        buf.writeNbt(this.getUpdateTag());
        PlayerLookup.tracking(this).forEach(p ->
                ServerPlayNetworking.send(p, SYNC_PACKET_ID, buf)
        );
    }

    @Environment(EnvType.CLIENT)
    public static void initClientNetworking() {
        ClientPlayNetworking.registerGlobalReceiver(SYNC_PACKET_ID, (client, handler, buf, responseSender) -> {
            var pos = buf.readBlockPos();
            var tag = buf.readNbt();
            client.execute(() -> {
                var be = client.level.getBlockEntity(pos);
                if (!(be instanceof FluidPipeBlockEntity fluidPipeBe)) {
                    return;
                }

                fluidPipeBe.load(tag);
            });
        });
    }

    private static final ResourceLocation SYNC_PACKET_ID = id("sync/block_entity/fluid_pipe");
}
