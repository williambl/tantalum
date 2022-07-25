package com.williambl.tantalum.gases.pipe.network;

import com.google.common.graph.MutableNetwork;
import com.williambl.tantalum.gases.FluidPipeBlockEntity;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import static com.williambl.tantalum.Tantalum.id;

@SuppressWarnings("UnstableApiUsage")
public interface PipeNetworkManager extends Component, AutoSyncedComponent, ServerTickingComponent {
    MutableNetwork<PipeNetworks.Node, PipeNetworks.Edge> getNetwork(int id);
    Int2ObjectMap<MutableNetwork<PipeNetworks.Node, PipeNetworks.Edge>> getNetworks();

    ComponentKey<PipeNetworkManager> KEY = ComponentRegistry.getOrCreate(id("pipe_network_manager"), PipeNetworkManager.class);
}
