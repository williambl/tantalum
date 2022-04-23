package com.williambl.tantalum.lasers;

import com.williambl.tantalum.LaserType;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

import static com.williambl.tantalum.Tantalum.id;

public class FireLaser implements LaserType {
    private final TagKey<Block> conversionTag = TagKey.create(Registry.BLOCK_REGISTRY, id("laser_conversion/fire"));

    @Override
    public void tick(AABB aabb, Level level, Direction direction) {
        level.getEntities(null, aabb).forEach(e -> e.setSecondsOnFire(8));
    }

    @Override
    public int color() {
        return 0xff0000;
    }

    @Override
    public TagKey<Block> conversionBlocks() {
        return conversionTag;
    }
}
