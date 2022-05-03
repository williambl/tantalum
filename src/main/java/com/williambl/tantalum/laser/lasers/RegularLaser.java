package com.williambl.tantalum.laser.lasers;

import com.williambl.tantalum.laser.LaserType;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

import static com.williambl.tantalum.Tantalum.id;

public class RegularLaser implements LaserType {
    private final TagKey<Block> conversionTag = TagKey.create(Registry.BLOCK_REGISTRY, id("laser_conversion/regular"));

    @Override
    public void tick(AABB aabb, Level level, Direction direction) {
    }

    @Override
    public int color() {
        return 0xffffff;
    }

    @Override
    public TagKey<Block> conversionBlocks() {
        return conversionTag;
    }
}
