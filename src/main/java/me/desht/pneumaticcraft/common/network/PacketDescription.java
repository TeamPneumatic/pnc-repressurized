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
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkEvent;

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
    private final CompoundNBT extraData;

    public PacketDescription(IDescSynced te, boolean fullSync) {
        super(te.getPosition());

        this.fullSync = fullSync;
        List<SyncedField<?>> descFields = te.getDescriptionFields();
        for (int i = 0; i < descFields.size(); i++) {
            if (fullSync || te.shouldSyncField(i)) {
                fields.add(new IndexedField(i, SyncedField.getType(descFields.get(i)), descFields.get(i).getValue()));
            }
        }
        extraData = new CompoundNBT();
        te.writeToPacket(extraData);
    }

    public PacketDescription(PacketBuffer buf) {
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
    public void toBytes(PacketBuffer buf) {
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
        ctx.get().enqueueWork(this::process);
        ctx.get().setPacketHandled(true);
    }

    public void process() {
        if (ClientUtils.getClientWorld().isAreaLoaded(pos, 0)) {
            TileEntity syncable = ClientUtils.getClientTE(pos);
            if (syncable instanceof IDescSynced) {
                IDescSynced descSynced = (IDescSynced) syncable;
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
    }

    /********************
     * These two methods are only used for initial chunk sending (getUpdateTag() and handleUpdateTag())
     */

    public CompoundNBT writeNBT(CompoundNBT compound) {
        CompoundNBT subTag = new CompoundNBT();

        subTag.putInt("Length", fields.size());
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        ListNBT list = new ListNBT();
        for (IndexedField field : fields) {
            CompoundNBT element = new CompoundNBT();
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

    public PacketDescription(CompoundNBT compound) {
        super(new BlockPos(compound.getInt("x"), compound.getInt("y"), compound.getInt("z")));

        fullSync = true;
        CompoundNBT subTag = compound.getCompound(Names.MOD_ID);
        int fieldCount = subTag.getInt("Length");
        ListNBT list = subTag.getList("Data", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < fieldCount; i++) {
            CompoundNBT element = list.getCompound(i);
            byte type = element.getByte("Type");
            byte[] b = element.getByteArray("Value");
            fields.add(new IndexedField(i, type, SyncedField.fromBytes(new PacketBuffer(Unpooled.wrappedBuffer(b)), type)));
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
