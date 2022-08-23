package com.williambl.tantalum.mixin;

import com.williambl.tantalum.nethershift.NetherShiftingComponent;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void dontRenderWhenShifting(double d, double e, double f, CallbackInfoReturnable<Boolean> cir) {
        var component = NetherShiftingComponent.KEY.getNullable(this);
        if (component != null && component.isShifting()) {
            cir.setReturnValue(false);
        }
    }
}
