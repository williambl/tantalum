package com.williambl.tantalum;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import reborncore.common.powerSystem.PowerAcceptorBlockEntity;

import javax.annotation.ParametersAreNonnullByDefault;

public class LaserEmitterBlockEntity extends PowerAcceptorBlockEntity {
    public LaserEmitterBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(Tantalum.LASER_EMITTER_BLOCK_ENTITY, blockPos, blockState);
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
    protected boolean canProvideEnergy(@Nullable Direction side) {
        return false;
    }
}
