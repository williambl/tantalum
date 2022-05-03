package com.williambl.tantalum.laser;

import com.williambl.tantalum.Tantalum;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public interface LaserType {
    void tick(AABB aabb, Level level, Direction direction);
    int color();
    TagKey<Block> conversionBlocks();

    static LaserType getTypeForBlock(BlockState state) {
        return Tantalum.LASER_REGISTRY.stream().filter(laserType -> state.is(laserType.conversionBlocks())).findAny().orElse(null);
    }
}
