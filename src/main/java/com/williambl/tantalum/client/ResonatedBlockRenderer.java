package com.williambl.tantalum.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.williambl.tantalum.resonator.ResonatedBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class ResonatedBlockRenderer extends EntityRenderer<ResonatedBlockEntity> {
	public ResonatedBlockRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.5F;
	}

	public void render(ResonatedBlockEntity entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
		BlockState blockState = entity.getBlockState();
		if (blockState.getRenderShape() == RenderShape.MODEL) {
			Level level = entity.getLevel();
			if (blockState != level.getBlockState(entity.blockPosition()) && blockState.getRenderShape() != RenderShape.INVISIBLE) {
				matrixStack.pushPose();
				BlockPos blockPos = new BlockPos(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
				matrixStack.translate(-0.5, 0.0, -0.5);
				matrixStack.translate(level.random.nextDouble() * 0.2, level.random.nextDouble() * 0.2, level.random.nextDouble() * 0.2);
				BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
				blockRenderDispatcher.getModelRenderer()
					.tesselateBlock(
						level,
						blockRenderDispatcher.getBlockModel(blockState),
						blockState,
						blockPos,
						matrixStack,
						buffer.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(blockState)),
						false,
						level.random,
						blockState.getSeed(entity.getStartPos()),
						OverlayTexture.NO_OVERLAY
					);
				matrixStack.popPose();
				super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
			}
		}
	}

	/**
	 * Returns the location of an entity's texture.
	 */
	public ResourceLocation getTextureLocation(ResonatedBlockEntity entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
