package com.williambl.tantalum.mixin;

import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FallingBlockEntity.class)
public interface FallingBlockEntityAccessor {
    @Invoker
    static FallingBlockEntity createFallingBlockEntity(Level level, double d, double e, double f, BlockState blockState) {
        throw new UnsupportedOperationException();
    }
}
