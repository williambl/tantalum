package com.williambl.tantalum;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public record LaserData(AABB aabb, Direction direction, LaserType type, BlockPos end) {
    public static Codec<LaserData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TantalumCodecs.AABB_CODEC.fieldOf("aabb").forGetter(LaserData::aabb),
            Direction.CODEC.fieldOf("direction").forGetter(LaserData::direction),
            ResourceLocation.CODEC.xmap(Tantalum.LASER_REGISTRY::get, Tantalum.LASER_REGISTRY::getKey).fieldOf("type").forGetter(LaserData::type),
            BlockPos.CODEC.fieldOf("end").forGetter(LaserData::end)
    ).apply(instance, LaserData::new));
}
