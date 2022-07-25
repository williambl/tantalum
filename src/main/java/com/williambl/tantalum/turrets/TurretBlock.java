package com.williambl.tantalum.turrets;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class TurretBlock extends Block {
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
    private static final double MAX_PROJECTILE_SPEED = 0.5;
    private static final TurretStrategy STRATEGY = new StupidTurretStrategy();

    public TurretBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(TRIGGERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TRIGGERED);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        boolean bl = level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above());
        boolean bl2 = state.getValue(TRIGGERED);
        if (bl && !bl2) {
            level.scheduleTick(pos, this, Block.UPDATE_INVISIBLE);
            level.setBlock(pos, state.setValue(TRIGGERED, true), Block.UPDATE_INVISIBLE);
        } else if (!bl && bl2) {
            level.setBlock(pos, state.setValue(TRIGGERED, false), Block.UPDATE_INVISIBLE);
        }

    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        var targets = level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(50.0));
        if (targets.isEmpty()) {
            return;
        }

        var target = targets.get(level.random.nextInt(targets.size()));

        var targetPos = target.getEyePosition();
        var targetVel = target.getDeltaMovement();
        var launcherPos = Vec3.atCenterOf(pos).add(0.0, 1.0, 0.0);
        for (int ticks = 0; ticks < 60; ticks++) {
            var predictedTargetPosition = STRATEGY.predictedTargetPosition(targetPos, targetVel, ticks);
            if (STRATEGY.maxDistance(ticks, MAX_PROJECTILE_SPEED) > launcherPos.distanceTo(predictedTargetPosition)) {
                continue;
            }

            var aim = STRATEGY.calculateAim(predictedTargetPosition, launcherPos, 0.99, new Vec3(0.0, -0.05, 0.0), ticks, MAX_PROJECTILE_SPEED);
            if (aim != null) {
                var arrow = new Arrow(level, launcherPos.x, launcherPos.y, launcherPos.z);
                arrow.setDeltaMovement(aim.x(), aim.y(), aim.z());
                Vec3 vec3 = arrow.getDeltaMovement();
                double d = vec3.horizontalDistance();
                arrow.setXRot((float)(Mth.atan2(vec3.y, d) * 180.0F / (float)Math.PI));
                arrow.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * 180.0F / (float)Math.PI));
                arrow.xRotO = arrow.getXRot();
                arrow.yRotO = arrow.getYRot();
                level.addFreshEntity(arrow);
                break;
            }
        }
    }
}
