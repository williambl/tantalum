package com.williambl.tantalum.gases.pipe.network;

import com.google.common.graph.MutableNetwork;
import com.williambl.tantalum.gases.FluidPipeBlockEntity;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;

@SuppressWarnings("UnstableApiUsage")
public interface PipeNetworkManager extends Component, AutoSyncedComponent, ServerTickingComponent {
    MutableNetwork<PipeNetworks.Node, PipeNetworks.Edge> getNetwork(int id);
}
