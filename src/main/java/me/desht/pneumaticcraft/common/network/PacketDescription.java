package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.Unpooled;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import me.desht.pneumaticcraft.common.semiblock.ISemiBlock;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Sent to: CLIENT
 * This is the primary mechanism for syncing TE and Semiblock data to clients when it changes.
 */
public class PacketDescription extends LocationIntPacket {
    private byte[] types;
    private Object[] values;
    private CompoundNBT extraData;
    private IDescSynced.Type type;

    public PacketDescription() {
    }

    public PacketDescription(IDescSynced te) {
        super(te.getPosition());
        type = te.getSyncType();
        values = new Object[te.getDescriptionFields().size()];
        types = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            values[i] = te.getDescriptionFields().get(i).getValue();
            types[i] = SyncedField.getType(te.getDescriptionFields().get(i));
        }
        extraData = new CompoundNBT();
        te.writeToPacket(extraData);
    }

    public PacketDescription(PacketBuffer buf) {
        super(buf);
        type = IDescSynced.Type.values()[buf.readByte()];
        int dataAmount = buf.readInt();
        types = new byte[dataAmount];
        values = new Object[dataAmount];
        for (int i = 0; i < dataAmount; i++) {
            types[i] = buf.readByte();
            values[i] = SyncedField.fromBytes(buf, types[i]);
        }
        extraData = buf.readCompoundTag();
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);
        buf.writeByte(type.ordinal());
        buf.writeInt(values.length);
        for (int i = 0; i < types.length; i++) {
            buf.writeByte(types[i]);
            SyncedField.toBytes(buf, values[i], types[i]);
        }
        buf.writeCompoundTag(extraData);
    }

    public void process(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(this::process);
        ctx.get().setPacketHandled(true);
    }

    public void process() {
        PlayerEntity player = ClientUtils.getClientPlayer();
        if (player.world.isAreaLoaded(pos, 0)) {
            Object syncable = getSyncableForType(player, type);
            if (syncable instanceof IDescSynced) {
                IDescSynced descSynced = (IDescSynced) syncable;
                List<SyncedField> descFields = descSynced.getDescriptionFields();
                if (descFields != null && descFields.size() == types.length) {
                    for (int i = 0; i < descFields.size(); i++) {
                        descFields.get(i).setValue(values[i]);
                    }
                }
                descSynced.readFromPacket(extraData);
                descSynced.onDescUpdate();
            }
        }
    }

    private Object getSyncableForType(PlayerEntity player, IDescSynced.Type type) {
        switch (type) {
            case TILE_ENTITY:
                return player.world.getTileEntity(pos);
            case SEMI_BLOCK:
                if (pos.equals(BlockPos.ZERO)) {
                    Container container = player.openContainer;
                    if (container instanceof ContainerLogistics) {
                        return ((ContainerLogistics) container).logistics;
                    }
                } else {
                    List<ISemiBlock> semiBlocks = SemiBlockManager.getInstance(player.world).getSemiBlocksAsList(player.world, pos);
                    int index = extraData.getByte("index");
                    return index < semiBlocks.size() ? semiBlocks.get(index) : null;
                }
        }
        return null;
    }

    /********************
     * These two methods are only used for initial chunk sending (getUpdateTag() and handleUpdateTag())
     */

    public CompoundNBT writeNBT(CompoundNBT compound) {
        compound.put("Pos", NBTUtil.writeBlockPos(pos));
        compound.putInt("SyncType", type.ordinal());
        compound.putInt("Length", values.length);
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        ListNBT list = new ListNBT();
        for (int i = 0; i < types.length; i++) {
            CompoundNBT element = new CompoundNBT();
            element.putByte("Type", types[i]);
            buf.clear();
            SyncedField.toBytes(buf, values[i], types[i]);
            element.putByteArray("Value", Arrays.copyOf(buf.array(), buf.writerIndex()));
            list.add(list.size(), element);
        }
        buf.release();
        compound.put("Data", list);
        compound.put("Extra", extraData);

        return compound;
    }

    public PacketDescription(CompoundNBT compound) {
        super(NBTUtil.readBlockPos(compound.getCompound("Pos")));
        type = IDescSynced.Type.values()[compound.getInt("SyncType")];
        values = new Object[compound.getInt("Length")];
        types = new byte[values.length];
        ListNBT list = compound.getList("Data", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < values.length; i++) {
            CompoundNBT element = list.getCompound(i);
            types[i] = element.getByte("Type");
            byte[] b = element.getByteArray("Value");
            values[i] = SyncedField.fromBytes(new PacketBuffer(Unpooled.wrappedBuffer(b)), types[i]);
        }
        extraData = compound.getCompound("Extra");
    }
}
