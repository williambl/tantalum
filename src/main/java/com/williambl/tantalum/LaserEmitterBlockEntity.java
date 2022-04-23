package com.williambl.tantalum;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import reborncore.common.blockentity.MachineBaseBlockEntity;
import reborncore.common.powerSystem.PowerAcceptorBlockEntity;

public class LaserEmitterBlockEntity extends PowerAcceptorBlockEntity {
    private AABB laserAABB = null;

    public LaserEmitterBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(Tantalum.LASER_EMITTER_BLOCK_ENTITY, blockPos, blockState);
    }

    @Override
    public long getBaseMaxPower() {
        return 80;
    }

    @Override
    public long getBaseMaxOutput() {
        return 0;
    }

    @Override
    public long getBaseMaxInput() {
        return 80;
    }

    @Override
    protected boolean canProvideEnergy(@Nullable Direction side) {
        return false;
    }

    @Override
    public void tick(Level world, BlockPos pos, BlockState state, MachineBaseBlockEntity blockEntity2) {
        super.tick(world, pos, state, blockEntity2);
        this.recalculateLaserAABB();

        world.getEntities(null, this.getLaserAABB()).forEach(it -> it.setSecondsOnFire(8));
    }

    public AABB getLaserAABB() {
        if (this.laserAABB == null) {
            this.laserAABB = this.calculateLaserAABB();
        }

        return this.laserAABB;
    }

    private void recalculateLaserAABB() {
        this.laserAABB = this.calculateLaserAABB();
    }

    private AABB calculateLaserAABB() {
        var dir = this.getBlockState().getValue(BlockStateProperties.FACING);
        var rayStart = Vec3.atCenterOf(this.getBlockPos()).add(Vec3.atLowerCornerOf(dir.getNormal()).scale(0.5));
        //noinspection ConstantConditions
        var raycast = this.level.clip(new ClipContext(
                rayStart,
                rayStart.add(Vec3.atLowerCornerOf(dir.getNormal()).scale(LaserEmitterBlock.LASER_LENGTH)),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.ANY,
                null
        ));

        var rayEnd = Vec3.atCenterOf(raycast.getBlockPos()).add(Vec3.atLowerCornerOf(dir.getNormal()).scale(0.5));

        return new AABB(rayStart.x - 0.25, rayStart.y - 0.25, rayStart.z - 0.25, rayEnd.x + 0.25, rayEnd.y + 0.25, rayEnd.z + 0.25);
    }
}
