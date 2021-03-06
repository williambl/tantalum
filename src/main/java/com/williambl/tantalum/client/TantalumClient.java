package com.williambl.tantalum.client;

import com.williambl.tantalum.gases.FluidPipeBlockEntity;
import com.williambl.tantalum.laser.LaserEmitterBlockEntity;
import com.williambl.tantalum.Tantalum;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class TantalumClient implements ClientModInitializer {
    @Override
    public void onInitializeClient(ModContainer mod) {
        BlockEntityRendererRegistry.register(Tantalum.LASER_EMITTER_BLOCK_ENTITY, LaserEmitterRenderer::new);
        BlockEntityRendererRegistry.register(Tantalum.FLUID_PIPE_BLOCK_ENTITY, PipeDebugRenderer::new);
        LaserEmitterBlockEntity.initClientNetworking();
        FluidPipeBlockEntity.initClientNetworking();
    }
}
