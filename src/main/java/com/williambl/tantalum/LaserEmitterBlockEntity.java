package com.williambl.tantalum;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.PlayerLookup;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import reborncore.common.blockentity.MachineBaseBlockEntity;
import reborncore.common.powerSystem.PowerAcceptorBlockEntity;

import java.util.ArrayList;
import java.util.List;

import static com.williambl.tantalum.Tantalum.id;

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
        this.sync();
    }

    private @Nullable LaserData calculateLaserData(BlockPos from, Direction dir) {
        var type = from == this.getBlockPos() ? Tantalum.REGULAR_LASER : LaserType.getTypeForBlock(this.level.getBlockState(from));
        if (type == null) {
            return null;
        }

        var offset = Vec3.atLowerCornerOf(dir.getNormal());
        var rayStart = Vec3.atCenterOf(from).add(offset.scale(0.5));
        //noinspection ConstantConditions
        var raycast = this.level.clip(new ClipContext(
                rayStart,
                rayStart.add(Vec3.atLowerCornerOf(dir.getNormal()).scale(LaserEmitterBlock.LASER_LENGTH)),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.ANY,
                null
        ));

        var rayEnd = Vec3.atCenterOf(raycast.getBlockPos()).add(offset.scale(-0.5));

        return new LaserData(
                new AABB(
                        rayStart.x - 0.25 * (offset.x != 0 ? 0 : 1),
                        rayStart.y - 0.25 * (offset.y != 0 ? 0 : 1),
                        rayStart.z - 0.25 * (offset.z != 0 ? 0 : 1),
                        rayEnd.x + 0.25 * (offset.x != 0 ? 0 : 1),
                        rayEnd.y + 0.25 * (offset.y != 0 ? 0 : 1),
                        rayEnd.z + 0.25 * (offset.z != 0 ? 0 : 1)
                ),
                dir,
                type,
                raycast.getBlockPos()
        );
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        var tag = super.getUpdateTag();
        tag.put("lasers", LaserData.CODEC.listOf().encode(this.lasers, NbtOps.INSTANCE, new ListTag()).getOrThrow(true, Tantalum.LOGGER::error));
        return tag;
    }

    @Override
    public void load(CompoundTag tag) {
        System.out.println(tag.toString());
        if (tag.getTagType("lasers") == Tag.TAG_LIST) {
            this.lasers.clear();
            this.lasers.addAll(LaserData.CODEC.listOf().decode(NbtOps.INSTANCE, tag.get("lasers")).getOrThrow(true, Tantalum.LOGGER::error).getFirst());
        }
    }

    public void sync() {
        var buf = PacketByteBufs.create();
        buf.writeBlockPos(this.getBlockPos());
        buf.writeNbt(this.getUpdateTag());
        PlayerLookup.tracking(this).forEach(p ->
            ServerPlayNetworking.send(p, SYNC_PACKET_ID, buf)
        );
    }

    @Environment(EnvType.CLIENT)
    public static void initClientNetworking() {
        ClientPlayNetworking.registerGlobalReceiver(SYNC_PACKET_ID, (client, handler, buf, responseSender) -> {
            var pos = buf.readBlockPos();
            var tag = buf.readNbt();
            client.execute(() -> {
                var be = client.level.getBlockEntity(pos);
                if (!(be instanceof LaserEmitterBlockEntity laserEmitterBe)) {
                    return;
                }

                laserEmitterBe.load(tag);
            });
        });
    }

    private static final ResourceLocation SYNC_PACKET_ID = id("sync/block_entity/laser_emitter");
}
