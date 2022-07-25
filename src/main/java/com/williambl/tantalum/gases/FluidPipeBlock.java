package com.williambl.tantalum.gases;

import com.williambl.tantalum.Tantalum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public class FluidPipeBlock extends BaseEntityBlock {
    public static EnumProperty<PipeShape> PIPE_SHAPE_PROPERTY = EnumProperty.create("pipe_shape", PipeShape.class);
    public FluidPipeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return Tantalum.FLUID_PIPE_BLOCK_ENTITY.create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide() ? null : createTickerHelper(blockEntityType, Tantalum.FLUID_PIPE_BLOCK_ENTITY, FluidPipeBlockEntity::tickEntity);
    }

    private void update(Level level, BlockPos pos, BlockState original) {
        level.setBlock(pos, this.getUpdatedBlockState(level, pos, original), Block.UPDATE_NEIGHBORS);
    }

    private BlockState getUpdatedBlockState(Level level, BlockPos pos, BlockState original) {
        Direction.Axis connectedIn = null;
        for (var dir : Direction.values()) {
            var state = level.getBlockState(pos.relative(dir));
            if (state.getBlock() instanceof FluidPipeBlock) {
                if (connectedIn != null) {
                    if (connectedIn == dir.getAxis()) {
                        continue;
                    }
                    connectedIn = null;
                    break;
                } else {
                    connectedIn = dir.getAxis();
                }
            }
        }

        return original.setValue(PIPE_SHAPE_PROPERTY, PipeShape.byAxis(connectedIn));
    }

    public enum PipeShape implements StringRepresentable {
        CONNECTED_X("connected_x", Direction.Axis.X),
        CONNECTED_Y("connected_y", Direction.Axis.Y),
        CONNECTED_Z("connected_z", Direction.Axis.Z),
        NODE("node", null);

        private final String serializedName;
        private final @Nullable Direction.Axis axis;

        PipeShape(String serializedName, @Nullable Direction.Axis axis) {
            this.serializedName = serializedName;
            this.axis = axis;
        }

        @Override
        public String getSerializedName() {
            return this.serializedName;
        }

        public @Nullable Direction.Axis getAxis() {
            return this.axis;
        }

        public static PipeShape byName(String name) {
            for (PipeShape value : values()) {
                if (value.getSerializedName().equalsIgnoreCase(name)) {
                    return value;
                }
            }

            return PipeShape.NODE;
        }

        public static PipeShape byAxis(Direction.Axis axis) {
            for (PipeShape value : values()) {
                if (value.getAxis() == axis) {
                    return value;
                }
            }

            return PipeShape.NODE;
        }
    }
}
