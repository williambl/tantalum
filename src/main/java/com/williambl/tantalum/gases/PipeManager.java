package com.williambl.tantalum.gases;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public final class PipeManager {
    private static final Map<ResourceKey<Level>, PipeManager> PIPE_MANAGERS = new HashMap<>();

    private final Object2LongMap<EndpointPair<FluidPipeBlockEntity>> flowSpeeds = new Object2LongOpenHashMap<>();
    private final Map<BlockPos, FluidPipeBlockEntity> pipes = new HashMap<>();

    public static void addPipe(ServerLevel level, BlockPos pos, FluidPipeBlockEntity be) {
        var manager = PIPE_MANAGERS.computeIfAbsent(level.dimension(), (dim) -> new PipeManager());
        manager.pipes.put(pos, be);
    }

    private void tick() {
        try (var outerTrans = Transaction.openOuter()) {
            MutableGraph<FluidPipeBlockEntity> pipeGraph = GraphBuilder.undirected()
                    .allowsSelfLoops(false)
                    .build();

            for (var be : this.pipes.values()) {
                pipeGraph.addNode(be);
            }

            for (var entry : this.pipes.entrySet()) {
                for (var dir : Direction.values()) {
                    var other = this.pipes.get(entry.getKey().relative(dir));
                    if (other != null && (other.getFluidTank().isResourceBlank() || other.getFluidTank().getResource().equals(entry.getValue().getFluidTank().getResource()))) {
                        pipeGraph.putEdge(entry.getValue(), other);
                    }
                }
            }


            for (var edge : pipeGraph.edges()) {
                var u = edge.nodeU();
                var v = edge.nodeV();
                var d = u.getFluidTank().getAmount() - v.getFluidTank().getAmount();
                this.flowSpeeds.computeLong(edge, (e, l) -> Mth.clamp(l == null ? d : l + d, -(v.getFluidTank().getAmount() / 6), u.getFluidTank().getAmount() / 6)); // 6 = Direction.values().length
            }

            for (var entry : flowSpeeds.object2LongEntrySet()) {
                try (var innerTrans = Transaction.openNested(outerTrans)) {
                    if (!pipeGraph.hasEdgeConnecting(entry.getKey())) {
                        this.flowSpeeds.removeLong(entry.getKey());
                        continue;
                    }

                    var u = entry.getKey().nodeU();
                    var v = entry.getKey().nodeV();

                    if (entry.getLongValue() > 0) {
                        var amount = v.getFluidTank().insert(u.getFluidTank().getResource(), entry.getLongValue(), innerTrans);
                        u.getFluidTank().extract(u.getFluidTank().getResource(), amount, innerTrans);
                    } else if (entry.getLongValue() < 0) {
                        var amount = u.getFluidTank().insert(v.getFluidTank().getResource(), -entry.getLongValue(), innerTrans);
                        v.getFluidTank().extract(v.getFluidTank().getResource(), amount, innerTrans);
                    }

                    innerTrans.commit();
                }
            }

            outerTrans.commit();
        }
        this.pipes.clear();
    }

    public static void tick(ServerLevel level) {
        level.getProfiler().push("pipes");
        var manager = PIPE_MANAGERS.get(level.dimension());
        if (manager != null) {
            manager.tick();
        }
        level.getProfiler().pop();
    }
}
