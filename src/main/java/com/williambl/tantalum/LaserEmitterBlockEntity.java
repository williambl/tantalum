package com.williambl.tantalum;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import reborncore.common.powerSystem.PowerAcceptorBlockEntity;

public class LaserEmitterBlockEntity extends PowerAcceptorBlockEntity {
    public LaserEmitterBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(Tantalum.LASER_EMITTER_BLOCK_ENTITY, blockPos, blockState);
    }

    @Override
    public long getBaseMaxPower() {
        return 80;
    }

    @Override
    public long getBaseMaxOutput() {
        return 0;
    }

    @Override
    public long getBaseMaxInput() {
        return 80;
    }

    @Override
    protected boolean canProvideEnergy(@Nullable Direction side) {
        return false;
    }

    public BlockPos getPosPastEndOfLaser() {
        var dir = this.getBlockState().getValue(BlockStateProperties.FACING);
        var pos = this.getBlockPos().relative(dir);
        while (this.level.getBlockState(pos).is(Tantalum.LASER_BLOCK)) {
            pos = pos.relative(dir);
        }

        return pos;
    }

    public boolean shouldPlaceLaserAt(BlockPos queryPos) {
        return this.level.getBlockState(queryPos).isAir();
    }
}
