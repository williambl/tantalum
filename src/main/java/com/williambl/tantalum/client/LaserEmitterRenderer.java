package com.williambl.tantalum.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.williambl.tantalum.laser.LaserEmitterBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class LaserEmitterRenderer implements BlockEntityRenderer<LaserEmitterBlockEntity> {
    public LaserEmitterRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(LaserEmitterBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        blockEntity.lasers().forEach(laserData -> {
            var colour = getFloatColour(laserData.type().color());
            LevelRenderer.renderLineBox(poseStack, bufferSource.getBuffer(RenderType.lines()), laserData.aabb().move(blockEntity.getBlockPos().multiply(-1)), colour[0], colour[1], colour[2], 0.5f);
        });
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(LaserEmitterBlockEntity blockEntity) {
        return true;
    }

    public static float[] getFloatColour(int colour) {
        return new float[]{(colour >> 16 & 255) / 255F, (colour >> 8 & 255) / 255F, (colour & 255) / 255F};
    }
}
