package com.williambl.tantalum.gases.pipe.network;

import com.google.common.graph.ImmutableNetwork;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.williambl.tantalum.Tantalum;
import com.williambl.tantalum.Util;
import com.williambl.tantalum.gases.FluidPipeBlockEntity;
import com.williambl.tantalum.gases.FluidTank;
import com.williambl.tantalum.gases.HasTank;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.quiltmc.loader.api.QuiltLoader;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
public final class PipeNetworks {
    public static final boolean SYNC_TO_CLIENTS_FOR_DEBUGGING = QuiltLoader.isDevelopmentEnvironment();
    public static final Codec<MutableNetwork<Node, Edge>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Util.setCodec(Node.CODEC).fieldOf("nodes").forGetter(Network::nodes),
            Util.setCodec(Edge.CODEC).fieldOf("edges").forGetter(Network::edges)
    ).apply(instance, PipeNetworks::createPipeNetwork));

    public static MutableNetwork<Node, Edge> createPipeNetwork() {
        return NetworkBuilder.undirected().build();
    }

    public static MutableNetwork<Node, Edge> createPipeNetwork(Set<Node> nodes, Set<Edge> edges) {
        var network = createPipeNetwork();
        nodes.forEach(network::addNode);
        edges.forEach(edge -> {
            Node u = null;
            Node v = null;
            for (var node : nodes) {
                if (node.pos().equals(edge.posA())) {
                    u = node;
                } else if (node.pos().equals(edge.posB())) {
                    v = node;
                }
            }

            if (u != null && v != null) {
                network.addEdge(u, v, edge);
            } else {
                Tantalum.LOGGER.warn("Edge {} doesn't correspond to any nodes!", edge);
            }
        });

        return network;
    }

    public static void addPipe(MutableNetwork<Node, Edge> network, BlockPos pipePos, Iterable<FluidPipeBlockEntity> neighbours, Consumer<BlockPos> markDirty) {
        var node = new Node(pipePos);
        network.addNode(node);
        neighbours.forEach(neighbour -> {
            var neighbourNode = convertEdgePartToNode(network, neighbour.getBlockPos(), markDirty);
            var newEdge = new Edge(neighbour.getBlockPos(), pipePos);
            network.addEdge(neighbourNode, node, newEdge);
        });
    }

    public static void removePipe(MutableNetwork<Node, Edge> network, BlockPos pipePos, Iterable<FluidPipeBlockEntity> neighbours, Consumer<BlockPos> markDirty) {
        var node = convertEdgePartToNode(network, pipePos, markDirty);
        neighbours.forEach(neighbour -> {
            var neighbourNode = convertEdgePartToNode(network, neighbour.getBlockPos(), markDirty);
            var newEdge = new Edge(neighbour.getBlockPos(), pipePos);
            network.addEdge(neighbourNode, node, newEdge);
        });

        network.removeNode(node);
    }

    public static FluidTank getTank(MutableNetwork<Node, Edge> network, FluidPipeBlockEntity pipe) {
        return nodeIncluding(network, pipe.getBlockPos()).map(Node::tank).orElseGet(() -> edgeIncluding(network, pipe.getBlockPos()).map(Edge::tank).orElseThrow());
    }

    public static Node convertEdgePartToNode(MutableNetwork<Node, Edge> network, BlockPos pipePos, Consumer<BlockPos> markDirty) {
        var edgeOptional = edgeIncluding(network, pipePos);
        if (edgeOptional.isEmpty()) {
            return nodeIncluding(network, pipePos).orElseThrow();
        }

        var edge = edgeOptional.get();
        var edgeEndpoints = network.incidentNodes(edge);
        var newNode = new Node(pipePos);
        network.addNode(newNode);
        var edgeA = new Edge(edgeEndpoints.nodeU().pos(), pipePos);
        network.addEdge(edgeEndpoints.nodeU(), newNode, edgeA);
        var edgeB = new Edge(edgeEndpoints.nodeV().pos(), pipePos);
        network.addEdge(edgeEndpoints.nodeV(), newNode, edgeB);
        try (var trans = Transaction.openOuter()) {
            var resource = edge.tank().getResource();
            var amount = edge.tank().getAmount();
            if (amount > 0) {
                amount -= edgeA.tank().insert(resource, amount, trans);
                if (amount > 0) {
                    amount -= edgeB.tank().insert(resource, amount, trans);
                    if (amount > 0) {
                        amount -= newNode.tank().insert(resource, amount, trans);
                    }
                }
            }

            trans.commit();
        }

        return newNode;
    }

    public static Optional<Node> nodeIncluding(MutableNetwork<Node, Edge> network, BlockPos pipe) {
        return network.nodes().stream()
                .filter(n -> n.pos.equals(pipe))
                .findAny();
    }

    public static Optional<Edge> edgeIncluding(MutableNetwork<Node, Edge> network, BlockPos pipe) {
        return network.edges().stream()
                .filter(e -> e.contains(pipe))
                .findAny();
    }

    public static final class Node {
        public static final Codec<Node> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                FluidTank.CODEC.fieldOf("tank").forGetter(Node::tank),
                BlockPos.CODEC.fieldOf("pos").forGetter(Node::pos)
        ).apply(instance, Node::new));
        private final FluidTank tank;
        private final BlockPos pos;

        public Node(FluidTank tank, BlockPos pos) {
            this.tank = tank;
            this.pos = pos;
        }

        public Node(BlockPos pipePos) {
            this(new FluidTank(10 * FluidConstants.BUCKET), pipePos);
        }

        public FluidTank tank() {
            return this.tank;
        }

        public BlockPos pos() {
            return this.pos;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Node) obj;
            return Objects.equals(this.pos, that.pos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.pos);
        }

        @Override
        public String toString() {
            return "Node[" +
                    "tank=" + this.tank + ", " +
                    "pos=" + this.pos + ']';
        }

    }

    public static final class Edge {
        public static final Codec<Edge> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                FluidTank.CODEC.fieldOf("tank").forGetter(Edge::tank),
                BlockPos.CODEC.fieldOf("minPosExclusive").forGetter(Edge::posA),
                BlockPos.CODEC.fieldOf("maxPosExclusive").forGetter(Edge::posB),
                Codec.LONG.fieldOf("flowSpeed").forGetter(Edge::flowSpeed)
        ).apply(instance, Edge::new));

        private final FluidTank tank;
        private final BlockPos minPosExclusive;
        private final BlockPos maxPosExclusive;
        private final Direction.Axis axis;

        private long flowSpeed;

        public Edge(FluidTank tank, BlockPos posA, BlockPos posB, long flowSpeed) {
            this.tank = tank;
            this.flowSpeed = flowSpeed;

            if (posA.equals(posB)) {
                throw new IllegalArgumentException("Positions given are the same: %s".formatted(posA));
            }

            var dir = Util.getDirectionBetween(posA, posB);
            if (dir == null) {
                throw new IllegalArgumentException("Positions %s and %s do not lie on an axis.".formatted(posA, posB));
            }

            this.axis = dir.getAxis();
            var posAInAxis = posA.get(this.axis);
            var posBInAxis = posB.get(this.axis);

            if (posAInAxis < posBInAxis) {
                this.minPosExclusive = posA;
                this.maxPosExclusive = posB;
            } else {
                this.minPosExclusive = posB;
                this.maxPosExclusive = posA;
            }
        }

        public Edge(BlockPos posA, BlockPos posB) {
            this(new FluidTank(10 * FluidConstants.BUCKET * posA.distManhattan(posB)), posA, posB, 0);
        }

        public FluidTank tank() {
            return this.tank;
        }

        public BlockPos posA() {
            return this.minPosExclusive;
        }

        public BlockPos posB() {
            return this.maxPosExclusive;
        }

        public Direction.Axis axis() {
            return this.axis;
        }

        public boolean contains(BlockPos pos) {
            var dir = Util.getDirectionBetween(this.minPosExclusive, pos);

            if (dir == null) {
                return false;
            }

            var otherAxis = dir.getAxis();

            var posOnAxis = pos.get(this.axis);

            return this.axis == otherAxis && posOnAxis > this.minPosExclusive.get(this.axis) && posOnAxis < this.maxPosExclusive.get(otherAxis);
        }

        public long flowSpeed() {
            return this.flowSpeed;
        }

        public void setFlowSpeed(long flowSpeed) {
            this.flowSpeed = flowSpeed;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Edge) obj;
            return Objects.equals(this.minPosExclusive, that.minPosExclusive) &&
                    Objects.equals(this.maxPosExclusive, that.maxPosExclusive);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.minPosExclusive, this.maxPosExclusive);
        }

        @Override
        public String toString() {
            return "Edge[" +
                    "tank=" + this.tank + ", " +
                    "posA=" + this.minPosExclusive + ", " +
                    "posB=" + this.maxPosExclusive + ", " +
                    "flowSpeed=" + this.flowSpeed + ']';
        }
    }
}
