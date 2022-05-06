package com.williambl.tantalum;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

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
}
