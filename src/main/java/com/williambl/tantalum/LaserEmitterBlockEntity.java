package com.williambl.tantalum;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import reborncore.common.blockentity.MachineBaseBlockEntity;
import reborncore.common.powerSystem.PowerAcceptorBlockEntity;

import java.util.ArrayList;
import java.util.List;

public class LaserEmitterBlockEntity extends PowerAcceptorBlockEntity {
    private final List<LaserData> lasers = new ArrayList<>();

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
        this.recalculateLasers();
        this.lasers.forEach(laser -> laser.type().tick(laser.aabb(), this.level,  laser.direction()));
    }

    public List<LaserData> lasers() {
        return new ArrayList<>(this.lasers);
    }

    private void recalculateLasers() {
        this.lasers.clear();
        LaserData lastLaser = null;
        var dir = this.getBlockState().getValue(BlockStateProperties.FACING);
        for (int i = 0; i < 16; i++) {
            var from = lastLaser == null ? this.getBlockPos() : lastLaser.end();
            lastLaser = this.calculateLaserData(from, dir);
            if (lastLaser == null) {
                break;
            }

            this.lasers.add(lastLaser);
        }
    }

    private @Nullable LaserData calculateLaserData(BlockPos from, Direction dir) {
        var type = from == this.getBlockPos() ? Tantalum.REGULAR_LASER : LaserType.getTypeForBlock(this.level.getBlockState(from));
        if (type == null) {
            return null;
        }

        var rayStart = Vec3.atCenterOf(from).add(Vec3.atLowerCornerOf(dir.getNormal()).scale(0.5));
        //noinspection ConstantConditions
        var raycast = this.level.clip(new ClipContext(
                rayStart,
                rayStart.add(Vec3.atLowerCornerOf(dir.getNormal()).scale(LaserEmitterBlock.LASER_LENGTH)),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.ANY,
                null
        ));

        var rayEnd = Vec3.atCenterOf(raycast.getBlockPos()).add(Vec3.atLowerCornerOf(dir.getNormal()).scale(0.5));

        return new LaserData(new AABB(rayStart.x - 0.25, rayStart.y - 0.25, rayStart.z - 0.25, rayEnd.x + 0.25, rayEnd.y + 0.25, rayEnd.z + 0.25), dir, type, raycast.getBlockPos());
    }
}
