package com.williambl.tantalum.client;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.blaze3d.vertex.PoseStack;
import com.williambl.tantalum.gases.FluidPipeBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;

public class PipeDebugRenderer implements BlockEntityRenderer<FluidPipeBlockEntity> {
    public PipeDebugRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FluidPipeBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (Minecraft.getInstance().options.renderDebug) {
            poseStack.pushPose();
            final double filledFraction = (double) blockEntity.getFluidTank().amount / (double) blockEntity.getFluidTank().getCapacity();
            var colour = getColourAsFloats(getColour(filledFraction));
            LevelRenderer.renderVoxelShape(
                    poseStack,
                    bufferSource.getBuffer(RenderType.lines()),
                    Shapes.box(-0.02, -0.01, -0.02, 1.02, filledFraction, 1.02),
                    0.0,
                    0.0,
                    0.0,
                    colour[0],
                    colour[1],
                    colour[2],
                    0.5f
            );
            poseStack.popPose();
        }
    }

    public static float[] getColourAsFloats(int colour) {
        return new float[]{(colour >> 16 & 255) / 255F, ((colour & 0x00ff00) >> 8 & 255) / 255F, (colour & 0x0000ff) / 255F};
    }

    private static int getColour(double filledFraction) {
        float f = (float) Math.max(0.0F, filledFraction);
        return Mth.hsvToRgb(f, 1.0F, 1.0F);
    }
}
