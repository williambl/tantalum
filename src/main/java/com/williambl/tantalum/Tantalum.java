package com.williambl.tantalum;

import com.williambl.tantalum.gases.*;
import com.williambl.tantalum.gases.pipe.network.LevelPipeNetworkManager;
import com.williambl.tantalum.gases.pipe.network.PipeNetworkManager;
import com.williambl.tantalum.laser.LaserEmitterBlock;
import com.williambl.tantalum.laser.LaserEmitterBlockEntity;
import com.williambl.tantalum.laser.LaserType;
import com.williambl.tantalum.laser.lasers.FireLaser;
import com.williambl.tantalum.laser.lasers.RegularLaser;
import com.williambl.tantalum.nethershift.EntityNetherShiftingComponent;
import com.williambl.tantalum.nethershift.NetherShifterItem;
import com.williambl.tantalum.nethershift.NetherShiftingComponent;
import com.williambl.tantalum.resonator.Resonance;
import com.williambl.tantalum.resonator.ResonatedBlockEntity;
import com.williambl.tantalum.resonator.ResonatorBlock;
import com.williambl.tantalum.resonator.ResonatorBlockEntity;
import com.williambl.tantalum.turrets.TurretBlock;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.registry.attachment.api.DefaultValueProvider;
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tantalum implements ModInitializer, WorldComponentInitializer, EntityComponentInitializer {
    public static Logger LOGGER = LoggerFactory.getLogger(Tantalum.class);

    public static Block TURRET_BLOCK = Registry.register(Registry.BLOCK, id("turret"), new TurretBlock(BlockBehaviour.Properties.of(Material.METAL)));
    public static Item TURRET_ITEM = Registry.register(Registry.ITEM, id("turret"), new BlockItem(TURRET_BLOCK, new Item.Properties()));

    public static Registry<LaserType> LASER_REGISTRY = FabricRegistryBuilder.createSimple(LaserType.class, id("laser")).attribute(RegistryAttribute.SYNCED).buildAndRegister();
    public static LaserType REGULAR_LASER = Registry.register(LASER_REGISTRY, id("regular"), new RegularLaser());
    public static LaserType FIRE_LASER = Registry.register(LASER_REGISTRY, id("fire"), new FireLaser());

    public static EntityType<Resonance> RESONANCE = Registry.register(Registry.ENTITY_TYPE, id("resonance"), FabricEntityTypeBuilder.<Resonance>create().entityFactory(Resonance::new).dimensions(EntityDimensions.scalable(1f, 1f)).build());
    public static EntityType<ResonatedBlockEntity> RESONATED_BLOCK = Registry.register(Registry.ENTITY_TYPE, id("resonated_block"), FabricEntityTypeBuilder.<ResonatedBlockEntity>create().entityFactory(ResonatedBlockEntity::new).dimensions(EntityDimensions.fixed(0.98f, 0.98f)).build());
    public static Block RESONATOR_BLOCK = Registry.register(Registry.BLOCK, id("resonator"), new ResonatorBlock(BlockBehaviour.Properties.of(Material.METAL)));
    public static Item RESONATOR_ITEM = Registry.register(Registry.ITEM, id("resonator"), new BlockItem(RESONATOR_BLOCK, new Item.Properties()));
    public static BlockEntityType<ResonatorBlockEntity> RESONATOR_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("resonator"), FabricBlockEntityTypeBuilder.create(ResonatorBlockEntity::new, RESONATOR_BLOCK).build());

    public static Block LASER_EMITTER_BLOCK = Registry.register(Registry.BLOCK, id("laser_emitter"), new LaserEmitterBlock(BlockBehaviour.Properties.of(Material.METAL)));
    public static Item LASER_EMITTER_ITEM = Registry.register(Registry.ITEM, id("laser_emitter"), new BlockItem(LASER_EMITTER_BLOCK, new Item.Properties()));
    public static BlockEntityType<LaserEmitterBlockEntity> LASER_EMITTER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("laser_emitter"), FabricBlockEntityTypeBuilder.create(LaserEmitterBlockEntity::new, LASER_EMITTER_BLOCK).build());

    public static Item NETHER_SHIFTER = Registry.register(Registry.ITEM, id("nether_shifter"), new NetherShifterItem(new Item.Properties().stacksTo(1)));

    public static Block AIR_COLLECTOR_BLOCK = Registry.register(Registry.BLOCK, id("air_collector"), new AirCollectorBlock(BlockBehaviour.Properties.of(Material.METAL)));
    public static Item AIR_COLLECTOR_ITEM = Registry.register(Registry.ITEM, id("air_collector"), new BlockItem(AIR_COLLECTOR_BLOCK, new Item.Properties()));
    public static BlockEntityType<AirCollectorBlockEntity> AIR_COLLECTOR_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("air_collector"), FabricBlockEntityTypeBuilder.create(AirCollectorBlockEntity::new, AIR_COLLECTOR_BLOCK).build());

    public static Block SPLITTER_BLOCK = Registry.register(Registry.BLOCK, id("splitter"), new SplitterBlock(BlockBehaviour.Properties.of(Material.METAL)));
    public static Item SPLITTER_ITEM = Registry.register(Registry.ITEM, id("splitter"), new BlockItem(SPLITTER_BLOCK, new Item.Properties()));
    public static BlockEntityType<SplitterBlockEntity> SPLITTER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("splitter"), FabricBlockEntityTypeBuilder.create(SplitterBlockEntity::new, SPLITTER_BLOCK).build());

    public static Block FLUID_PIPE_BLOCK = Registry.register(Registry.BLOCK, id("fluid_pipe"), new FluidPipeBlock(BlockBehaviour.Properties.of(Material.METAL)));
    public static Item FLUID_PIPE_ITEM = Registry.register(Registry.ITEM, id("fluid_pipe"), new BlockItem(FLUID_PIPE_BLOCK, new Item.Properties()));
    public static BlockEntityType<FluidPipeBlockEntity> FLUID_PIPE_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, id("fluid_pipe"), FabricBlockEntityTypeBuilder.create(FluidPipeBlockEntity::new, FLUID_PIPE_BLOCK).build());

    public static final RegistryEntryAttachment<Fluid, FluidComposition> FLUID_COMPOSITION = RegistryEntryAttachment.builder(Registry.FLUID, id("composition"), FluidComposition.class, FluidComposition.CODEC)
            .defaultValueProvider(fluid -> DefaultValueProvider.Result.of(new FluidComposition(fluid)))
            .build();

    public static final Fluid AIR_FLUID = Registry.register(Registry.FLUID, id("air"), new GasFluid());
    public static final Fluid OXYGEN_FLUID = Registry.register(Registry.FLUID, id("oxygen"), new GasFluid());
    public static final Fluid NITROGEN_FLUID = Registry.register(Registry.FLUID, id("nitrogen"), new GasFluid());
    public static final Fluid ARGON_FLUID = Registry.register(Registry.FLUID, id("argon"), new GasFluid());
    public static final Fluid CARBON_DIOXIDE_FLUID = Registry.register(Registry.FLUID, id("carbon_dioxide"), new GasFluid());
    public static final Fluid HELIUM_FLUID = Registry.register(Registry.FLUID, id("helium"), new GasFluid());
    public static final Fluid XENON_FLUID = Registry.register(Registry.FLUID, id("xenon"), new GasFluid());

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
    }

    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        registry.register(PipeNetworkManager.KEY, LevelPipeNetworkManager::new);
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(NetherShiftingComponent.KEY, EntityNetherShiftingComponent::new, RespawnCopyStrategy.NEVER_COPY);
    }
}
