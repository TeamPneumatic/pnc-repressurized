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
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Sent to: CLIENT
 *
 * This is the primary mechanism for syncing tile entity data to clients when it changes.
 */
public class PacketDescription extends LocationIntPacket {
    private final boolean fullSync;
    private final List<IndexedField> fields = new ArrayList<>();
    private final CompoundTag extraData;

    public PacketDescription(IDescSynced te, boolean fullSync) {
        super(te.getPosition());

        this.fullSync = fullSync;
        List<SyncedField<?>> descFields = te.getDescriptionFields();
        for (int i = 0; i < descFields.size(); i++) {
            if (fullSync || te.shouldSyncField(i)) {
                fields.add(new IndexedField(i, SyncedField.getType(descFields.get(i)), descFields.get(i).getValue()));
            }
        }
        extraData = new CompoundTag();
        te.writeToPacket(extraData);
    }

    public PacketDescription(FriendlyByteBuf buf) {
        super(buf);

        fullSync = buf.readBoolean();
        int fieldCount = buf.readVarInt();
        for (int i = 0; i < fieldCount; i++) {
            int idx = fullSync ? i : buf.readVarInt();
            byte type = buf.readByte();
            fields.add(new IndexedField(idx, type, SyncedField.fromBytes(buf, type)));
        }
        extraData = buf.readNbt();
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        super.toBytes(buf);

        buf.writeBoolean(fullSync);
        buf.writeVarInt(fields.size());
        for (IndexedField indexedField : fields) {
            if (!fullSync) buf.writeVarInt(indexedField.idx);
            buf.writeByte(indexedField.type);
            SyncedField.toBytes(buf, indexedField.value, indexedField.type);
        }
        buf.writeNbt(extraData);
    }

    public void process(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> processPacket(null));
        ctx.get().setPacketHandled(true);
    }

    public void processPacket(BlockEntity blockEntity) {
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
            descSynced.readFromPacket(extraData);
            descSynced.onDescUpdate();
        }
    }

    /********************
     * These two methods are only used for initial chunk sending (getUpdateTag() and handleUpdateTag())
     */

    public CompoundTag writeNBT(CompoundTag compound) {
        CompoundTag subTag = new CompoundTag();

        subTag.putInt("Length", fields.size());
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
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

    public PacketDescription(CompoundTag compound) {
        super(new BlockPos(compound.getInt("x"), compound.getInt("y"), compound.getInt("z")));

        fullSync = true;
        CompoundTag subTag = compound.getCompound(Names.MOD_ID);
        int fieldCount = subTag.getInt("Length");
        ListTag list = subTag.getList("Data", Tag.TAG_COMPOUND);
        for (int i = 0; i < fieldCount; i++) {
            CompoundTag element = list.getCompound(i);
            byte type = element.getByte("Type");
            byte[] b = element.getByteArray("Value");
            fields.add(new IndexedField(i, type, SyncedField.fromBytes(new FriendlyByteBuf(Unpooled.wrappedBuffer(b)), type)));
        }
        extraData = subTag.getCompound("Extra");
    }

    public boolean hasData() {
        return !fields.isEmpty() || !extraData.isEmpty();
    }

    private static class IndexedField {
        final int idx;
        final byte type;
        final Object value;

        IndexedField(int idx, byte type, Object value) {
            this.idx = idx;
            this.type = type;
            this.value = value;
        }
    }
}
