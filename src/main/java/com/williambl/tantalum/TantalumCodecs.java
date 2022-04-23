package com.williambl.tantalum;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.phys.AABB;

public final class TantalumCodecs {
    public static final Codec<AABB> AABB_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("minX").forGetter((AABB a) -> a.minX),
            Codec.DOUBLE.fieldOf("minY").forGetter((AABB a) -> a.minY),
            Codec.DOUBLE.fieldOf("minZ").forGetter((AABB a) -> a.minZ),
            Codec.DOUBLE.fieldOf("maxX").forGetter((AABB a) -> a.maxX),
            Codec.DOUBLE.fieldOf("maxY").forGetter((AABB a) -> a.maxY),
            Codec.DOUBLE.fieldOf("maxZ").forGetter((AABB a) -> a.maxZ)
    ).apply(instance, AABB::new));
}
