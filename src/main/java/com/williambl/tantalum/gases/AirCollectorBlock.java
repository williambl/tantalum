package com.williambl.tantalum.gases;

import com.williambl.tantalum.Tantalum;
import com.williambl.tantalum.laser.LaserEmitterBlock;
import com.williambl.tantalum.laser.LaserEmitterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventListener;
import org.jetbrains.annotations.Nullable;

public class AirCollectorBlock extends BaseEntityBlock {
    public AirCollectorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return Tantalum.AIR_COLLECTOR_BLOCK_ENTITY.create(pos, state);
    }
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide() ? null : createTickerHelper(blockEntityType, Tantalum.AIR_COLLECTOR_BLOCK_ENTITY, AirCollectorBlockEntity::tickEntity);
    }
}
