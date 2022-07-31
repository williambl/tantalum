package com.williambl.tantalum.resonator;

import com.williambl.tantalum.Tantalum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import reborncore.common.blockentity.MachineBaseBlockEntity;
import reborncore.common.powerSystem.PowerAcceptorBlockEntity;

import java.util.UUID;

public class ResonatorBlockEntity extends PowerAcceptorBlockEntity {
    private UUID resonanceId;

    public ResonatorBlockEntity(BlockPos pos, BlockState state) {
        super(Tantalum.RESONATOR_BLOCK_ENTITY, pos, state);
    }

    private Resonance getOrCreateResonance(ServerLevel serverLevel) {
        if (this.resonanceId == null || !(serverLevel.getEntity(this.resonanceId) instanceof Resonance resonance) || !resonance.isAlive()) {
            var direction = this.getDirection();
            Resonance newResonance = new Resonance(
                    serverLevel,
                    Vec3.atCenterOf(this.getBlockPos()),
                    direction.toYRot(),
                    direction.getAxis() == Direction.Axis.Y ? direction.getAxisDirection().getStep() * 90f : 0f,
                    1.0,
                    1.0,
                    Resonance.Shape.CYLINDER
            );
            serverLevel.addFreshEntity(newResonance);
            this.resonanceId = newResonance.getUUID();
            return newResonance;
        }

        return resonance;
    }

    private Direction getDirection() {
        return this.getBlockState().getValue(BlockStateProperties.FACING);
    }

    @Override
    public long getBaseMaxPower() {
        return 40;
    }

    @Override
    public long getBaseMaxOutput() {
        return 0;
    }

    @Override
    public long getBaseMaxInput() {
        return 40;
    }

    @Override
    public void tick(Level world, BlockPos pos, BlockState state, MachineBaseBlockEntity blockEntity2) {
        if (world instanceof ServerLevel serverLevel) {
            long energyToUse = this.getStored();
            this.getOrCreateResonance(serverLevel).setPower(energyToUse);
            this.useEnergy(energyToUse);
        }
        super.tick(world, pos, state, blockEntity2);
    }
}
