package com.williambl.tantalum.client;

import com.google.common.graph.MutableNetwork;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.williambl.tantalum.gases.FluidPipeBlockEntity;
import com.williambl.tantalum.gases.pipe.network.PipeNetworkManager;
import com.williambl.tantalum.gases.pipe.network.PipeNetworks;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

public class PipeDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    public static float[] getColourAsFloats(int colour) {
        return new float[]{(colour >> 16 & 255) / 255F, ((colour & 0x00ff00) >> 8 & 255) / 255F, (colour & 0x0000ff) / 255F};
    }

    private static int getColour(double filledFraction) {
        float f = (float) Math.max(0.0F, filledFraction);
        return Mth.hsvToRgb(f, 1.0F, 1.0F);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, double camX, double camY, double camZ) {
        var level = Minecraft.getInstance().level;
        var networks = PipeNetworkManager.KEY.get(level).getNetworks();
        networks.forEach((index, network) -> {
            renderNetwork(poseStack, bufferSource, camX, camY, camZ, index, network);
        });
    }

    private static void renderNetwork(PoseStack poseStack, MultiBufferSource bufferSource, double camX, double camY, double camZ, int index, MutableNetwork<PipeNetworks.Node, PipeNetworks.Edge> network) {
        network.nodes().forEach(node -> {
            final double filledFraction = (double) node.tank().getAmount() / (double) node.tank().getCapacity();
            var colour = getColourAsFloats(getColour(filledFraction));
            LevelRenderer.renderVoxelShape(
                    poseStack,
                    bufferSource.getBuffer(RenderType.lines()),
                    Shapes.box(-0.02, -0.01, -0.02, 1.02, filledFraction, 1.02),
                    node.pos().getX(),
                    node.pos().getY(),
                    node.pos().getZ(),
                    colour[0],
                    colour[1],
                    colour[2],
                    0.5f
            );
        });

        network.edges().forEach(edge -> {
            final double filledFraction = (double) edge.tank().getAmount() / (double) edge.tank().getCapacity();
            var colour = getColourAsFloats(getColour(filledFraction));
            renderLine(poseStack, Vec3.atCenterOf(edge.posA()), Vec3.atCenterOf(edge.posB()), colour[0], colour[1], colour[2]);
        });
    }

    public static void renderLine(PoseStack stack, Vec3 pos1, Vec3 pos2, float r, float g, float b) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.Mode.LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        bufferBuilder.vertex(stack.last().pose(), (float) pos1.x + 0.5f, (float) pos1.y + 0.5f, (float) pos1.z + 0.5f).color(r, g, b, 1f).endVertex();
        bufferBuilder.vertex(stack.last().pose(), (float) pos2.x + 0.5f, (float) pos2.y + 0.5f, (float) pos2.z + 0.5f).color(r, g, b, 1f).endVertex();

        tesselator.end();
    }

    public void render(WorldRenderContext ctx) {
        this.render(
                ctx.matrixStack(),
                Minecraft.getInstance().renderBuffers().bufferSource(),
                ctx.camera().getPosition().x(),
                ctx.camera().getPosition().y(),
                ctx.camera().getPosition().z()
        );
    }
}
