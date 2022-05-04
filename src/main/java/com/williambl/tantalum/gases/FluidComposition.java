package com.williambl.tantalum.gases;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.world.level.material.Fluid;

import java.util.HashMap;
import java.util.Map;

public class FluidComposition {
    private final Object2IntMap<Fluid> compositionMap = new Object2IntOpenHashMap<>();

    public FluidComposition(Map<Fluid, Integer> values) {
        this.compositionMap.putAll(values);
    }

    public FluidComposition(Fluid fluid) {
        this.compositionMap.put(fluid, 1);
    }

    public Map<Fluid, Integer> getValues() {
        return new HashMap<>(compositionMap);
    }

    public static Codec<FluidComposition> CODEC = Codec.unboundedMap(Registry.FLUID.byNameCodec(), Codec.INT)
            .xmap(FluidComposition::new, FluidComposition::getValues);
}
