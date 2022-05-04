package com.williambl.tantalum.gases;

import com.williambl.tantalum.Tantalum;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import reborncore.common.blockentity.MachineBaseBlockEntity;
import reborncore.common.powerSystem.PowerAcceptorBlockEntity;
import techreborn.init.ModFluids;

import java.util.ArrayList;
import java.util.List;

import static net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants.*;

@SuppressWarnings("UnstableApiUsage")
public class SplitterBlockEntity extends PowerAcceptorBlockEntity implements HasTank {
    private final class SplitterTank extends FluidTank {
        SplitterTank() {
            super(BUCKET * 10);
        }

        @Override
        public void onCloseExtra(TransactionContext transaction, TransactionContext.Result result) {
            SplitterBlockEntity.this.setChanged();
        }
    };

    List<SplitterTank> tanks = new ArrayList<>();
    {
        for (int i = 0; i < 10; i++) {
            tanks.add(new SplitterTank());
        }
    }

    CombinedStorage<FluidVariant, SplitterTank> combinedTanks = new CombinedStorage<>(tanks);

    public SplitterBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(Tantalum.SPLITTER_BLOCK_ENTITY, blockPos, blockState);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        var fluidTag = this.combinedTanks.parts.stream().map(SplitterTank::toNbt).reduce(new ListTag(), (list, compound) -> { list.add(compound); return list; }, (a, b) -> { a.addAll(b); return a; });
        tag.put("tank", fluidTag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        var tankNbts = tag.getList("tank", NbtType.LIST);
        for (int i = 0; i < this.tanks.size(); i++) {
            this.tanks.get(i).fromNbt(tankNbts.getCompound(i));
        }
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
        return this.tanks.get(0);
    }

    @Nullable
    public FluidTank getFluidTank() {
        return this.tanks.get(0);
    }

    @Override
    public void tick(Level world, BlockPos pos, BlockState state, MachineBaseBlockEntity blockEntity2) {
        super.tick(world, pos, state, blockEntity2);

        for (var dir : Direction.values()) {
            try (var transaction = Transaction.openOuter()) {
                var other = world.getBlockEntity(pos.relative(dir));
                if (other instanceof FluidPipeBlockEntity fluidPipeBlockEntity && !fluidPipeBlockEntity.getFluidTank().isResourceBlank()) {
                    var amount = this.getFluidTank().insert(fluidPipeBlockEntity.getFluidTank().getResource(), BOTTLE, transaction);
                    fluidPipeBlockEntity.getFluidTank().extract(fluidPipeBlockEntity.getFluidTank().getResource(), amount, transaction);
                    transaction.commit();
                } else {
                    transaction.abort();
                }
            }
        }

        trans:
        try(var transaction = Transaction.openOuter()) {
            var mainTank = this.tanks.get(0);
            if (mainTank.amount > 0) {
                var amountExtractedForSplitting = mainTank.extract(mainTank.variant, BOTTLE, transaction);
                var splittingResult = Tantalum.FLUID_COMPOSITION.getValue(mainTank.variant.getFluid()).get().split(amountExtractedForSplitting);
                for (var entry : splittingResult.entrySet()) {
                    if (entry.getKey().isSame(Fluids.EMPTY) || this.combinedTanks.insert(FluidVariant.of(entry.getKey()), entry.getValue(), transaction) != entry.getValue()) {
                        transaction.abort();
                        break trans;
                    }
                }
            }
            transaction.commit();
        }
    }

    public static void tickEntity(Level level, BlockPos blockPos, BlockState blockState, SplitterBlockEntity e) {
        e.tick(level, blockPos, blockState, e);
    }
}
