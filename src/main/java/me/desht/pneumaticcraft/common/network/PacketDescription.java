/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.Unpooled;
import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Sent to: CLIENT
 * This is the primary mechanism for syncing block entity data to clients when it changes.
 */
public record PacketDescription(BlockPos pos, boolean fullSync, List<IndexedField> fields, CompoundTag extraData) implements CustomPacketPayload {
    public static final Type<PacketDescription> TYPE = new Type<>(RL("description"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketDescription> STREAM_CODEC = StreamCodec.of(
            PacketDescription::toNetwork,
            PacketDescription::fromNetwork
    );

    public static PacketDescription create(IDescSynced te, boolean fullSync, HolderLookup.Provider provider) {
        List<IndexedField> fields = new ArrayList<>();
        List<SyncedField<?>> descFields = te.getDescriptionFields();
        for (int i = 0; i < descFields.size(); i++) {
            if (fullSync || te.shouldSyncField(i)) {
                fields.add(new IndexedField(i, SyncedField.getType(descFields.get(i)), descFields.get(i).getValue()));
            }
        }

        CompoundTag extraData = Util.make(new CompoundTag(), tag -> te.writeToPacket(tag, provider));

        return new PacketDescription(te.getPosition(), fullSync, fields, extraData);
    }

    public static void toNetwork(RegistryFriendlyByteBuf buf, PacketDescription packet) {
        buf.writeBlockPos(packet.pos);
        buf.writeBoolean(packet.fullSync);
        buf.writeVarInt(packet.fields.size());
        for (IndexedField indexedField : packet.fields) {
            if (!packet.fullSync) buf.writeVarInt(indexedField.idx);
            buf.writeByte(indexedField.type);
            SyncedField.toBytes(buf, indexedField.value, indexedField.type);
        }
        buf.writeNbt(packet.extraData);
    }

    public static PacketDescription fromNetwork(RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        boolean fullSync = buf.readBoolean();
        int fieldCount = buf.readVarInt();
        List<IndexedField> fields = new ArrayList<>();
        for (int i = 0; i < fieldCount; i++) {
            int idx = fullSync ? i : buf.readVarInt();
            byte type = buf.readByte();
            fields.add(new IndexedField(idx, type, SyncedField.fromBytes(buf, type)));
        }
        CompoundTag extraData = buf.readNbt();

        return new PacketDescription(pos, fullSync, fields, extraData);
    }

    @Override
    public Type<PacketDescription> type() {
        return TYPE;
    }

    public static void handle(PacketDescription message, IPayloadContext ctx) {
        message.processPacket(null, ctx.player().registryAccess());
    }

    public void processPacket(BlockEntity blockEntity, HolderLookup.Provider provider) {
        if (blockEntity == null) {
            // - null blockentity: coming from processing our PacketDescription packet, expect pos to be in the packet
            // - non-null blockentity: coming from TileEntityBase#handleUpdateTag(), which has already received the BE from the vanilla packet
            if (!ClientUtils.getClientLevel().isLoaded(pos)) {
                return;
            }
            blockEntity = ClientUtils.getBlockEntity(pos);
        }

        if (blockEntity instanceof IDescSynced descSynced) {
            List<SyncedField<?>> descFields = descSynced.getDescriptionFields();
            if (descFields != null) {
                for (IndexedField indexedField : fields) {
                    if (indexedField.idx < descFields.size()) {
                        descFields.get(indexedField.idx).setValue(indexedField.value);
                    }
                }
            }
            descSynced.readFromPacket(extraData, provider);
            descSynced.onDescUpdate();
        }
    }

    /********************
     * These two methods are only used for initial chunk sending (getUpdateTag() and handleUpdateTag())
     */

    public CompoundTag writeNBT(CompoundTag compound, RegistryAccess registryAccess) {
        CompoundTag subTag = new CompoundTag();

        subTag.putInt("Length", fields.size());
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess);
        ListTag list = new ListTag();
        for (IndexedField field : fields) {
            CompoundTag element = new CompoundTag();
            element.putByte("Type", field.type);
            buf.clear();
            SyncedField.toBytes(buf, field.value, field.type);
            element.putByteArray("Value", Arrays.copyOf(buf.array(), buf.writerIndex()));
            list.add(list.size(), element);
        }
        buf.release();
        subTag.put("Data", list);
        subTag.put("Extra", extraData);
        compound.put(Names.MOD_ID, subTag);

        return compound;
    }

    public static PacketDescription fromNBT(CompoundTag compound, RegistryAccess registryAccess) {
        BlockPos pos = new BlockPos(compound.getInt("x"), compound.getInt("y"), compound.getInt("z"));
        boolean fullSync = true;
        CompoundTag subTag = compound.getCompound(Names.MOD_ID);
        int fieldCount = subTag.getInt("Length");
        List<IndexedField> fields = new ArrayList<>();
        ListTag list = subTag.getList("Data", Tag.TAG_COMPOUND);
        for (int i = 0; i < fieldCount; i++) {
            CompoundTag element = list.getCompound(i);
            byte type = element.getByte("Type");
            byte[] b = element.getByteArray("Value");
            fields.add(new IndexedField(i, type, SyncedField.fromBytes(new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(b), registryAccess, ConnectionType.OTHER), type)));
        }
        CompoundTag extraData = subTag.getCompound("Extra");

        return new PacketDescription(pos, fullSync, fields, extraData);
    }

    public boolean hasData() {
        return !fields.isEmpty() || !extraData.isEmpty();
    }

    private record IndexedField(int idx, byte type, Object value) {
    }
}
