package com.williambl.tantalum.gases;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;

@SuppressWarnings("UnstableApiUsage")
public class FluidTank extends SingleVariantStorage<FluidVariant> {
    private final long capacity;

    public FluidTank(long capacity) {
        this.capacity = capacity;
    }

    @Override
    protected FluidVariant getBlankVariant() {
        return FluidVariant.blank();
    }

    @Override
    protected long getCapacity(FluidVariant variant) {
        return this.capacity;
    }

    @Override
    public void onClose(TransactionContext transaction, TransactionContext.Result result) {
        super.onClose(transaction, result);
        this.onCloseExtra(transaction, result);
    }

    public void onCloseExtra(TransactionContext transaction, TransactionContext.Result result) {}

    public CompoundTag toNbt() {
        var tag = new CompoundTag();
        tag.put("fluidVariant", this.variant.toNbt());
        tag.putLong("amount", this.amount);
        return tag;
    }

    public void fromNbt(CompoundTag tag) {
        if (tag.contains("fluidVariant", NbtType.COMPOUND)) {
            this.variant = FluidVariant.fromNbt(tag.getCompound("fluidVariant"));
        }
        this.amount = tag.getLong("amount");
    }
}
