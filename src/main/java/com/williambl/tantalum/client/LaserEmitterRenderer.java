package com.williambl.tantalum.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.williambl.tantalum.laser.LaserEmitterBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

public class LaserEmitterRenderer implements BlockEntityRenderer<LaserEmitterBlockEntity> {
    public LaserEmitterRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(LaserEmitterBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        blockEntity.lasers().forEach(laserData -> {
            var colour = getFloatColour(laserData.type().color());
            renderLaser(poseStack, bufferSource.getBuffer(RenderType.entityTranslucent(new ResourceLocation("textures/misc/white.png"))), laserData.aabb().move(blockEntity.getBlockPos().multiply(-1)), colour[0], colour[1], colour[2], 0.2f);
        });
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(LaserEmitterBlockEntity blockEntity) {
        return true;
    }

    public static void renderLaser(
            PoseStack poseStack,
            VertexConsumer consumer,
            AABB aabb,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        float minX = (float)aabb.minX;
        float minY = (float)aabb.minY;
        float minZ = (float)aabb.minZ;
        float maxX = (float)aabb.maxX;
        float maxY = (float)aabb.maxY;
        float maxZ = (float)aabb.maxZ;

        Direction normalDir = Direction.DOWN;
        addVertex(pose, normal, consumer, red, green, blue, alpha, minX, minY, maxZ, 0f, 0f, normalDir);
        addVertex(pose, normal, consumer, red, green, blue, alpha, maxX, minY, maxZ, 1f, 0f, normalDir);
        addVertex(pose, normal, consumer, red, green, blue, alpha, maxX, minY, minZ, 1f, 1f, normalDir);
        addVertex(pose, normal, consumer, red, green, blue, alpha, minX, minY, minZ, 0f, 1f, normalDir);
        normalDir = Direction.UP;
        addVertex(pose, normal, consumer, red, green, blue, alpha, minX, maxY, minZ, 0f, 0f, normalDir);
        addVertex(pose, normal, consumer, red, green, blue, alpha, maxX, maxY, minZ, 1f, 0f, normalDir);
        addVertex(pose, normal, consumer, red, green, blue, alpha, maxX, maxY, maxZ, 1f, 1f, normalDir);
        addVertex(pose, normal, consumer, red, green, blue, alpha, minX, maxY, maxZ, 0f, 1f, normalDir);
        normalDir = Direction.NORTH;
        addVertex(pose, normal, consumer, red, green, blue, alpha, maxX, minY, maxZ, 0f, 0f, normalDir);
        addVertex(pose, normal, consumer, red, green, blue, alpha, minX, minY, maxZ, 1f, 0f, normalDir);
        addVertex(pose, normal, consumer, red, green, blue, alpha, minX, maxY, maxZ, 1f, 1f, normalDir);
        addVertex(pose, normal, consumer, red, green, blue, alpha, maxX, maxY, maxZ, 0f, 1f, normalDir);
        normalDir = Direction.SOUTH;
        addVertex(pose, normal, consumer, red, green, blue, alpha, minX, minY, minZ, 0f, 0f, normalDir);
        addVertex(pose, normal, consumer, red, green, blue, alpha, maxX, minY, minZ, 1f, 0f, normalDir);
        addVertex(pose, normal, consumer, red, green, blue, alpha, maxX, maxY, minZ, 1f, 1f, normalDir);
        addVertex(pose, normal, consumer, red, green, blue, alpha, minX, maxY, minZ, 0f, 1f, normalDir);
        normalDir = Direction.WEST;
        addVertex(pose, normal, consumer, red, green, blue, alpha, minX, minY, minZ, 0f, 0f, normalDir);
        addVertex(pose, normal, consumer, red, green, blue, alpha, minX, minY, maxZ, 1f, 0f, normalDir);
        addVertex(pose, normal, consumer, red, green, blue, alpha, minX, maxY, maxZ, 1f, 1f, normalDir);
        addVertex(pose, normal, consumer, red, green, blue, alpha, minX, maxY, minZ, 0f, 1f, normalDir);
        normalDir = Direction.EAST;
        addVertex(pose, normal, consumer, red, green, blue, alpha, maxX, minY, maxZ, 0f, 0f, normalDir);
        addVertex(pose, normal, consumer, red, green, blue, alpha, maxX, minY, minZ, 1f, 0f, normalDir);
        addVertex(pose, normal, consumer, red, green, blue, alpha, maxX, maxY, minZ, 1f, 1f, normalDir);
        addVertex(pose, normal, consumer, red, green, blue, alpha, maxX, maxY, maxZ, 0f, 1f, normalDir);
    }

    private static void addVertex(
            Matrix4f pose,
            Matrix3f normal,
            VertexConsumer consumer,
            float red,
            float green,
            float blue,
            float alpha,
            float x,
            float y,
            float z,
            float u,
            float v,
            Direction normalDir
    ) {
        consumer.vertex(pose, x, y, z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, normalDir.getNormal().getX(), normalDir.getNormal().getY(), normalDir.getNormal().getZ())
                .endVertex();
    }

    public static float[] getFloatColour(int colour) {
        return new float[]{(colour >> 16 & 255) / 255F, (colour >> 8 & 255) / 255F, (colour & 255) / 255F};
    }
}
