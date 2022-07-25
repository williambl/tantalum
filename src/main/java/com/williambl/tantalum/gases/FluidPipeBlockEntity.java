package com.williambl.tantalum.gases;

import com.williambl.tantalum.Tantalum;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

import static com.williambl.tantalum.Tantalum.id;
import static net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants.BUCKET;

@SuppressWarnings("UnstableApiUsage")
public class FluidPipeBlockEntity extends BlockEntity implements HasTank {
    public int pipeNetworkId;

    public FluidPipeBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(Tantalum.FLUID_PIPE_BLOCK_ENTITY, blockPos, blockState);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("pipeNetwork", this.pipeNetworkId);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.pipeNetworkId = tag.getInt("pipeNetwork");
    }

    @Override
    public FluidTank getTank(Direction context) {
        return this.getFluidTank();
    }

    @Nullable
    public FluidTank getFluidTank() {
        return null; //TODO
    }

    public int getPipeNetworkId() {
        return this.pipeNetworkId;
    }

    @Override
    public CompoundTag getUpdateTag() {
        var tag = super.getUpdateTag();
        this.saveAdditional(tag);
        return tag;
    }

    public static void tickEntity(Level level, BlockPos blockPos, BlockState blockState, FluidPipeBlockEntity e) {
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
