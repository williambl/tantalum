package com.williambl.tantalum.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.williambl.tantalum.resonator.Resonance;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ResonanceDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, double camX, double camY, double camZ) {
        var level = Minecraft.getInstance().level;
        level.getEntitiesOfClass(Resonance.class, AABB.ofSize(new Vec3(camX, camY, camZ), 64.0, 64.0, 64.0)).forEach(r ->
                renderResonance(poseStack, bufferSource, camX, camY, camZ, level, r)
        );
    }

    private static void renderResonance(PoseStack poseStack, MultiBufferSource bufferSource, double camX, double camY, double camZ, Level level, Resonance resonance) {
        DebugRenderer.renderFloatingText(""+resonance.resonanceFactor(new Vec3(camX, camY, camZ)), resonance.getX(), resonance.getY(), resonance.getZ(), 0xffffff);
    }

    public static void renderLine(Vec3 pos1, Vec3 pos2, float r, float g, float b) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(6f);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        bufferBuilder.begin(VertexFormat.Mode.LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        bufferBuilder.vertex((float) pos1.x + 0.5f, (float) pos1.y + 1.25f, (float) pos1.z + 0.5f).color(r, g, b, 1f).endVertex();
        bufferBuilder.vertex((float) pos2.x + 0.5f, (float) pos2.y + 1.25f, (float) pos2.z + 0.5f).color(r, g, b, 1f).endVertex();

        tesselator.end();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
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
