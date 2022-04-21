package com.williambl.tantalum;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import java.util.Random;

public class LaserBlock extends DirectionalBlock {
    protected LaserBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if (!this.isValid(state, level, pos)) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        this.tick(state, level, pos, random);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        level.scheduleTick(pos.relative(state.getValue(FACING)), this, 1);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite().getOpposite());
    }

    private boolean isValid(BlockState state, Level level, BlockPos pos) {
        var checkingPos = pos.mutable();
        var dir = state.getValue(FACING);

        for (int i = 0; i < 32; i++) {
            checkingPos.move(dir);
            var checkingState = level.getBlockState(checkingPos);

            if (checkingState.is(Tantalum.LASER_EMITTER_BLOCK) && checkingState.getValue(FACING) == dir && ((LaserEmitterBlockEntity)level.getBlockEntity(checkingPos)).getEnergy() > 0) {
                return true;
            }

            if (!(checkingState.is(this))) {
                return false;
            }
        }

        return false;
    }
}
