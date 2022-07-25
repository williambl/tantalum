package com.williambl.tantalum;

import com.google.common.graph.EndpointPair;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.math.Vector3d;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public final class Util {
    public static final Codec<AABB> AABB_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("minX").forGetter(a -> a.minX),
            Codec.DOUBLE.fieldOf("minY").forGetter(a -> a.minY),
            Codec.DOUBLE.fieldOf("minZ").forGetter(a -> a.minZ),
            Codec.DOUBLE.fieldOf("maxX").forGetter(a -> a.maxX),
            Codec.DOUBLE.fieldOf("maxY").forGetter(a -> a.maxY),
            Codec.DOUBLE.fieldOf("maxZ").forGetter(a -> a.maxZ)
    ).apply(instance, AABB::new));

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
}
