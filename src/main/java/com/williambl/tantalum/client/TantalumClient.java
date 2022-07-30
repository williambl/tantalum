package com.williambl.tantalum.client;

import com.williambl.tantalum.gases.FluidPipeBlockEntity;
import com.williambl.tantalum.gases.pipe.network.PipeNetworks;
import com.williambl.tantalum.laser.LaserEmitterBlockEntity;
import com.williambl.tantalum.Tantalum;
import com.williambl.tantalum.oscillator.Resonance;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.NoopRenderer;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class TantalumClient implements ClientModInitializer {
    @Override
    public void onInitializeClient(ModContainer mod) {
        BlockEntityRendererRegistry.register(Tantalum.LASER_EMITTER_BLOCK_ENTITY, LaserEmitterRenderer::new);
        LaserEmitterBlockEntity.initClientNetworking();
        FluidPipeBlockEntity.initClientNetworking();
        if (PipeNetworks.SYNC_TO_CLIENTS_FOR_DEBUGGING) {
            var pipeDebugRenderer = new PipeDebugRenderer();
            WorldRenderEvents.LAST.register(pipeDebugRenderer::render);
        }
        if (Resonance.ENABLE_DEBUG_RENDERING) {
            var resonanceDebugRenderer = new ResonanceDebugRenderer();
            WorldRenderEvents.LAST.register(resonanceDebugRenderer::render);
        }
        EntityRendererRegistry.register(Tantalum.RESONANCE, NoopRenderer::new);
        EntityRendererRegistry.register(Tantalum.RESONATED_BLOCK, ResonatedBlockRenderer::new);
    }
}
