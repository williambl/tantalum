package com.williambl.tantalum;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class Tantalum implements ModInitializer {
    public static Block LASER_BLOCK = Registry.register(Registry.BLOCK, id("laser"), new LaserBlock(BlockBehaviour.Properties.of(Material.AIR).noCollission().air().randomTicks()));
    public static Block LASER_EMITTER_BLOCK = Registry.register(Registry.BLOCK, id("laser_emitter"), new LaserEmitterBlock(BlockBehaviour.Properties.of(Material.METAL)));
    public static Item LASER_EMITTER_ITEM = Registry.register(Registry.ITEM, id("laser_emitter"), new BlockItem(LASER_EMITTER_BLOCK, new Item.Properties()));
    public static BlockEntityType<LaserEmitterBlockEntity> LASER_EMITTER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("laser_emitter"), FabricBlockEntityTypeBuilder.create(LaserEmitterBlockEntity::new, LASER_EMITTER_BLOCK).build());

    public static ResourceLocation id(String path) {
        return new ResourceLocation("tantalum", path);
    }
    @Override
    public void onInitialize(ModContainer mod) {
        System.out.println("Initialised!!");
    }
}
