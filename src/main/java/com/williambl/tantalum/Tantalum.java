package com.williambl.tantalum;

import com.williambl.tantalum.gases.*;
import com.williambl.tantalum.laser.LaserEmitterBlock;
import com.williambl.tantalum.laser.LaserEmitterBlockEntity;
import com.williambl.tantalum.laser.LaserType;
import com.williambl.tantalum.laser.lasers.FireLaser;
import com.williambl.tantalum.laser.lasers.RegularLaser;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldTickEvents;
import org.quiltmc.qsl.registry.attachment.api.DefaultValueProvider;
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tantalum implements ModInitializer {
    public static Logger LOGGER = LoggerFactory.getLogger(Tantalum.class);

    public static Registry<LaserType> LASER_REGISTRY = FabricRegistryBuilder.createSimple(LaserType.class, id("laser")).attribute(RegistryAttribute.SYNCED).buildAndRegister();
    public static LaserType REGULAR_LASER = Registry.register(LASER_REGISTRY, id("regular"), new RegularLaser());
    public static LaserType FIRE_LASER = Registry.register(LASER_REGISTRY, id("fire"), new FireLaser());

    public static Block LASER_EMITTER_BLOCK = Registry.register(Registry.BLOCK, id("laser_emitter"), new LaserEmitterBlock(BlockBehaviour.Properties.of(Material.METAL)));
    public static Item LASER_EMITTER_ITEM = Registry.register(Registry.ITEM, id("laser_emitter"), new BlockItem(LASER_EMITTER_BLOCK, new Item.Properties()));
    public static BlockEntityType<LaserEmitterBlockEntity> LASER_EMITTER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("laser_emitter"), FabricBlockEntityTypeBuilder.create(LaserEmitterBlockEntity::new, LASER_EMITTER_BLOCK).build());

    public static Block AIR_COLLECTOR_BLOCK = Registry.register(Registry.BLOCK, id("air_collector"), new AirCollectorBlock(BlockBehaviour.Properties.of(Material.METAL)));
    public static Item AIR_COLLECTOR_ITEM = Registry.register(Registry.ITEM, id("air_collector"), new BlockItem(AIR_COLLECTOR_BLOCK, new Item.Properties()));
    public static BlockEntityType<AirCollectorBlockEntity> AIR_COLLECTOR_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("air_collector"), FabricBlockEntityTypeBuilder.create(AirCollectorBlockEntity::new, AIR_COLLECTOR_BLOCK).build());

    public static Block FLUID_PIPE_BLOCK = Registry.register(Registry.BLOCK, id("fluid_pipe"), new FluidPipeBlock(BlockBehaviour.Properties.of(Material.METAL)));
    public static Item FLUID_PIPE_ITEM = Registry.register(Registry.ITEM, id("fluid_pipe"), new BlockItem(FLUID_PIPE_BLOCK, new Item.Properties()));
    public static BlockEntityType<FluidPipeBlockEntity> FLUID_PIPE_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("fluid_pipe"), FabricBlockEntityTypeBuilder.create(FluidPipeBlockEntity::new, FLUID_PIPE_BLOCK).build());

    public static final RegistryEntryAttachment<Fluid, FluidComposition> FLUID_COMPOSITION = RegistryEntryAttachment.builder(Registry.FLUID, id("composition"), FluidComposition.class, FluidComposition.CODEC)
            .defaultValueProvider(fluid -> DefaultValueProvider.Result.of(new FluidComposition(fluid)))
            .build();

    public static ResourceLocation id(String path) {
        return new ResourceLocation("tantalum", path);
    }

    @Override
    public void onInitialize(ModContainer mod) {
        FluidStorage.SIDED.registerForBlockEntities((blockEntity, context) -> {
            if (blockEntity instanceof HasTank hasTank) {
                return hasTank.getTank(context);
            } else {
                return null;
            }
        }, AIR_COLLECTOR_BLOCK_ENTITY, FLUID_PIPE_BLOCK_ENTITY);

        ServerWorldTickEvents.END.register((server, world) -> PipeManager.tick(world));
    }
}
