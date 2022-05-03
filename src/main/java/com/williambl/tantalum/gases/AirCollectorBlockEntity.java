package com.williambl.tantalum.gases;

import com.williambl.tantalum.Tantalum;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.lwjgl.system.CallbackI;
import reborncore.common.blockentity.MachineBaseBlockEntity;
import reborncore.common.powerSystem.PowerAcceptorBlockEntity;
import techreborn.init.ModFluids;

import static net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants.*;

@SuppressWarnings("UnstableApiUsage")
public class AirCollectorBlockEntity extends PowerAcceptorBlockEntity implements HasTank {
    private final FluidTank tank = new FluidTank(BUCKET * 10) {
        @Override
        public void onCloseExtra(TransactionContext transaction, TransactionContext.Result result) {
            AirCollectorBlockEntity.this.setChanged();
        }
    };

    public AirCollectorBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(Tantalum.AIR_COLLECTOR_BLOCK_ENTITY, blockPos, blockState);
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
    public long getBaseMaxPower() {
        return 40;
    }

    @Override
    public long getBaseMaxOutput() {
        return 0;
    }

    @Override
    public long getBaseMaxInput() {
        return 40;
    }

    @Override
    public FluidTank getTank(Direction context) {
        return this.tank;
    }

    @Override
    public void tick(Level world, BlockPos pos, BlockState state, MachineBaseBlockEntity blockEntity2) {
        super.tick(world, pos, state, blockEntity2);
        try(var transaction = Transaction.openOuter()) {
            this.tank.insert(FluidVariant.of(ModFluids.NITROGEN.getFluid()), DROPLET, transaction);
            transaction.commit();
        }
    }

    public static void tickEntity(Level level, BlockPos blockPos, BlockState blockState, AirCollectorBlockEntity e) {
        e.tick(level, blockPos, blockState, e);
    }
}
