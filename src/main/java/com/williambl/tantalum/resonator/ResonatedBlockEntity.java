package com.williambl.tantalum.resonator;

import com.williambl.tantalum.Tantalum;
import com.williambl.tantalum.mixin.FallingBlockEntityAccessor;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Predicate;

public class ResonatedBlockEntity extends Entity {
    private BlockState blockState = Blocks.SAND.defaultBlockState();
    public int time;
    private boolean hurtEntities;
    private int fallDamageMax = 40;
    private float fallDamagePerDistance;
    private UUID resonance;
    @Nullable
    public CompoundTag blockData;
    protected static final EntityDataAccessor<BlockPos> DATA_START_POS = SynchedEntityData.defineId(ResonatedBlockEntity.class, EntityDataSerializers.BLOCK_POS);

    public ResonatedBlockEntity(EntityType<? extends ResonatedBlockEntity> entityType, Level level) {
        super(entityType, level);
    }

    public ResonatedBlockEntity(Level level, BlockPos startPos, Resonance resonance, BlockState blockState) {
        this(Tantalum.RESONATED_BLOCK, level);
        this.blockState = blockState;
        this.blocksBuilding = true;
        this.setPos(Vec3.atCenterOf(startPos));
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        this.setStartPos(this.blockPosition());
        this.setResonance(resonance);
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    public void setStartPos(BlockPos origin) {
        this.entityData.set(DATA_START_POS, origin);
    }

    public BlockPos getStartPos() {
        return this.entityData.get(DATA_START_POS);
    }

    public @Nullable Resonance getResonance() {
        if (this.level instanceof ServerLevel serverLevel) {
            var e = serverLevel.getEntity(this.resonance);
            if (e instanceof Resonance res && res.isAlive() && res.resonanceFactor(this.position()) >= 1.0) {
                return res;
            }
        }
        return null;
    }

    public void setResonance(Resonance resonance) {
        this.resonance = resonance.getUUID();
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_START_POS, BlockPos.ZERO);
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public void tick() {
        if (this.blockState.isAir()) {
            this.discard();
        } else {
            this.move(MoverType.SELF, this.getDeltaMovement());
            if (!this.level.isClientSide) {
                var resonance = this.getResonance();
                if (resonance != null) {
                    double f = Math.abs(resonance.resonanceFactor(this.position()));
                    if (f >= 0.03) {
                        if (this.getDeltaMovement().lengthSqr() < 0.02) {
                            this.setDeltaMovement(this.random.nextDouble() * 0.2, this.random.nextDouble() * 0.2, this.random.nextDouble() * 0.2);
                        }

                        double forwardF = Math.abs(resonance.resonanceFactor(this.position().add(this.getDeltaMovement())));
                        if (forwardF > f) {
                            double backwardF = Math.abs(resonance.resonanceFactor(this.position().subtract(this.getDeltaMovement())));
                            if (backwardF < forwardF) {
                                this.setDeltaMovement(this.getDeltaMovement().scale(-0.8));
                            } else {
                                this.setDeltaMovement(this.getDeltaMovement().scale(0.01));
                            }
                        }
                    } else {
                        this.setDeltaMovement(this.getDeltaMovement().scale(0.01));
                    }
                } else {
                    FallingBlockEntity fallingBlockEntity = FallingBlockEntityAccessor.createFallingBlockEntity(
                            this.level,
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            this.blockState.hasProperty(BlockStateProperties.WATERLOGGED) ? this.blockState.setValue(BlockStateProperties.WATERLOGGED, false) : this.blockState
                    );
                    this.level.addFreshEntity(fallingBlockEntity);
                    this.discard();
                }
            }
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        if (this.hurtEntities) {
            int i = Mth.ceil(fallDistance - 1.0F);
            if (i >= 0) {
                Predicate<Entity> predicate;
                DamageSource damageSource;
                if (this.blockState.getBlock() instanceof Fallable fallable) {
                    predicate = fallable.getHurtsEntitySelector();
                    damageSource = fallable.getFallDamageSource();
                } else {
                    predicate = EntitySelector.NO_SPECTATORS;
                    damageSource = DamageSource.FALLING_BLOCK;
                }

                float f = (float) Math.min(Mth.floor((float) i * this.fallDamagePerDistance), this.fallDamageMax);
                this.level.getEntities(this, this.getBoundingBox(), predicate).forEach(entity -> entity.hurt(damageSource, f));
                boolean bl = this.blockState.is(BlockTags.ANVIL);
                if (bl && f > 0.0F && this.random.nextFloat() < 0.05F + (float) i * 0.05F) {
                    BlockState blockState = AnvilBlock.damage(this.blockState);
                    if (blockState == null) {
                        this.blockState = Blocks.AIR.defaultBlockState();
                    } else {
                        this.blockState = blockState;
                    }
                }

            }
        }
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.put("BlockState", NbtUtils.writeBlockState(this.blockState));
        compound.putInt("Time", this.time);
        compound.putBoolean("HurtEntities", this.hurtEntities);
        compound.putFloat("FallHurtAmount", this.fallDamagePerDistance);
        compound.putInt("FallHurtMax", this.fallDamageMax);
        if (this.blockData != null) {
            compound.put("TileEntityData", this.blockData);
        }
        compound.putUUID("Resonance", this.resonance);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        this.blockState = NbtUtils.readBlockState(compound.getCompound("BlockState"));
        this.time = compound.getInt("Time");
        if (compound.contains("HurtEntities", 99)) {
            this.hurtEntities = compound.getBoolean("HurtEntities");
            this.fallDamagePerDistance = compound.getFloat("FallHurtAmount");
            this.fallDamageMax = compound.getInt("FallHurtMax");
        } else if (this.blockState.is(BlockTags.ANVIL)) {
            this.hurtEntities = true;
        }

        if (compound.contains("TileEntityData", 10)) {
            this.blockData = compound.getCompound("TileEntityData");
        }

        if (compound.contains("Resonance", Tag.TAG_INT_ARRAY)) {
            this.resonance = compound.getUUID("Resonance");
        }

        if (this.blockState.isAir()) {
            this.blockState = Blocks.SAND.defaultBlockState();
        }

    }

    public void setHurtsEntities(float f, int i) {
        this.hurtEntities = true;
        this.fallDamagePerDistance = f;
        this.fallDamageMax = i;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public void fillCrashReportCategory(CrashReportCategory category) {
        super.fillCrashReportCategory(category);
        category.setDetail("Imitating BlockState", this.blockState.toString());
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this, Block.getId(this.getBlockState()));
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        this.blockState = Block.stateById(packet.getData());
        this.blocksBuilding = true;
        double d = packet.getX();
        double e = packet.getY();
        double f = packet.getZ();
        this.setPos(d, e, f);
        this.setStartPos(this.blockPosition());
    }
}
