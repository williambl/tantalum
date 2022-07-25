package com.williambl.tantalum.gases.pipe.network;

import com.google.common.graph.MutableNetwork;
import com.williambl.tantalum.Tantalum;
import com.williambl.tantalum.gases.FluidPipeBlockEntity;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.Optional;

public class LevelPipeNetworkManager implements PipeNetworkManager {
    private final Level level;
    private final Int2ObjectMap<MutableNetwork<PipeNetworks.Node, PipeNetworks.Edge>> networks = new Int2ObjectOpenHashMap<>();

    public LevelPipeNetworkManager(Level level) {
        this.level = level;
    }

    @Override
    public MutableNetwork<PipeNetworks.Node, PipeNetworks.Edge> getNetwork(int id) {
        return this.networks.get(id);
    }

    public int addNetwork(MutableNetwork<PipeNetworks.Node, PipeNetworks.Edge> network) {
        int i = this.networks.size()-1;
        while (!this.networks.containsKey(i)) {
            i++;
        }

        this.networks.put(i, network);
        return i;
    }

    public int joinNetwork(FluidPipeBlockEntity pipe) {
        var neighbours = Arrays.stream(Direction.values())
                .map(dir -> pipe.getBlockPos().relative(dir))
                .map(pos -> this.level.getBlockEntity(pos, Tantalum.FLUID_PIPE_BLOCK_ENTITY))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        var networks = neighbours.stream()
                .map(FluidPipeBlockEntity::getPipeNetworkId)
                .toList();

        if (networks.size() == 0) {
            var network = PipeNetworks.createPipeNetwork();
            PipeNetworks.addPipe(network, pipe, neighbours, this::updatePipeBlock);

            return this.addNetwork(network);
        } else if (networks.size() == 1) {
            int networkId = networks.get(0);
            var network = this.getNetwork(networkId);
            PipeNetworks.addPipe(network, pipe, neighbours, this::updatePipeBlock);
            return networkId;
        } else {
            //TODO merge networks
            return 0;
        }
    }

    @Override
    public void serverTick() {

    }

    @Override
    public void readFromNbt(CompoundTag tag) {

    }

    @Override
    public void writeToNbt(CompoundTag tag) {

    }

    private void updatePipeBlock(BlockPos pos) {
        //TODO
    }
}
