package com.williambl.tantalum.gases.pipe.network;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.MutableNetwork;
import com.williambl.tantalum.Tantalum;
import com.williambl.tantalum.gases.FluidPipeBlockEntity;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
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

    private void tickNetwork(MutableNetwork<PipeNetworks.Node, PipeNetworks.Edge> network) {
        try (var outerTrans = Transaction.openOuter()) {
            //TODO: friction, damping for infinite waves
            for (var edge : network.edges()) {
                var nodes = network.incidentNodes(edge);
                var u = nodes.nodeU();
                var v = nodes.nodeV();
                var d = u.tank().getAmount() - v.tank().getAmount();
                edge.setFlowSpeed(Mth.clamp(edge.flowSpeed() + d, -(v.tank().getAmount() / 6), u.tank().getAmount() / 6)); // 6 = Direction.values().length
            }

            for (var edge : network.edges()) {
                try (var innerTrans = Transaction.openNested(outerTrans)) {
                    var nodes = network.incidentNodes(edge);

                    PipeNetworks.Node u;
                    PipeNetworks.Node v;
                    long flowSpeed;
                    if (edge.flowSpeed() > 0) {
                        u = nodes.nodeU();
                        v = nodes.nodeV();
                        flowSpeed = edge.flowSpeed();
                    } else {
                        u = nodes.nodeV();
                        v = nodes.nodeU();
                        flowSpeed = -edge.flowSpeed();
                    }

                    if (u.tank().isResourceBlank()) {
                        innerTrans.abort();
                        continue;
                    }

                    var amountExtracted = edge.tank().extract(edge.tank().getResource(), flowSpeed, innerTrans);
                    amountExtracted += u.tank().extract(u.tank().getResource(), Math.max(0, flowSpeed - amountExtracted), innerTrans);
                    var amountInserted = v.tank().insert(u.tank().getResource(), amountExtracted, innerTrans);
                    var amountInsertedIntoPipe = edge.tank().insert(edge.tank().getResource(), amountExtracted - amountInserted, innerTrans);
                    if (amountInsertedIntoPipe + amountInserted == amountExtracted) {
                        innerTrans.commit();
                    } else {
                        Tantalum.LOGGER.warn("Violation of conservation of mass @ {} -> [{}, {}] -> {} (expected {} got {})", u.pos(), edge.posA(), edge.posB(), v.pos(), amountExtracted, amountInsertedIntoPipe + amountInserted);
                        innerTrans.abort();
                    }
                }
            }

            outerTrans.commit();
        }
    }

    @Override
    public void serverTick() {
        this.level.getProfiler().push("pipes");
        this.networks.values().forEach(this::tickNetwork);
        this.level.getProfiler().pop();
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
