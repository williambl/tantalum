package com.williambl.tantalum;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public final class Util {
    public static final Codec<AABB> AABB_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("minX").forGetter(a -> a.minX),
            Codec.DOUBLE.fieldOf("minY").forGetter(a -> a.minY),
            Codec.DOUBLE.fieldOf("minZ").forGetter(a -> a.minZ),
            Codec.DOUBLE.fieldOf("maxX").forGetter(a -> a.maxX),
            Codec.DOUBLE.fieldOf("maxY").forGetter(a -> a.maxY),
            Codec.DOUBLE.fieldOf("maxZ").forGetter(a -> a.maxZ)
    ).apply(instance, AABB::new));

    public static final EntityDataSerializer<Double> DOUBLE_ENTITY_DATA = new EntityDataSerializer<>() {
        public void write(FriendlyByteBuf buffer, Double value) {
            buffer.writeDouble(value);
        }

        public Double read(FriendlyByteBuf buffer) {
            return buffer.readDouble();
        }

        public Double copy(Double value) {
            return value;
        }
    };

    public static <T> Codec<Set<T>> setCodec(Codec<T> codec) {
        return codec.listOf().xmap(HashSet::new, ArrayList::new);
    }

    public static BlockPos normalise(BlockPos pos) {
        return new BlockPos(Vec3.atLowerCornerOf(pos).normalize());
    }

    public static Direction getDirectionBetween(BlockPos a, BlockPos b) {
        return Direction.fromNormal(normalise(b.subtract(a)));
    }

    public static BlockPos oneTowards(BlockPos start, BlockPos target) {
        return start.offset(normalise(target.subtract(start)));
    }

    public static <T extends Enum<T>> EntityDataSerializer<T> enumEntityData(Class<T> clazz) {
        return new EntityDataSerializer<>() {
            public void write(FriendlyByteBuf buffer, T value) {
                buffer.writeEnum(value);
            }

            public T read(FriendlyByteBuf buffer) {
                return buffer.readEnum(clazz);
            }

            public T copy(T value) {
                return value;
            }
        };
    }
}
