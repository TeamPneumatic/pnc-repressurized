package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.desht.pneumaticcraft.common.inventory.ContainerLogistics;
import me.desht.pneumaticcraft.common.inventory.SyncedField;
import me.desht.pneumaticcraft.common.semiblock.SemiBlockManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.Arrays;
import java.util.List;

public class PacketDescription extends LocationIntPacket<PacketDescription> {
    private byte[] types;
    private Object[] values;
    private NBTTagCompound extraData;
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
            types[i] = PacketUpdateGui.getType(te.getDescriptionFields().get(i));
        }
        extraData = new NBTTagCompound();
        te.writeToPacket(extraData);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeByte(type.ordinal());
        buf.writeInt(values.length);
        for (int i = 0; i < types.length; i++) {
            buf.writeByte(types[i]);
            PacketUpdateGui.writeField(buf, values[i], types[i]);
        }
        ByteBufUtils.writeTag(buf, extraData);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        type = IDescSynced.Type.values()[buf.readByte()];
        int dataAmount = buf.readInt();
        types = new byte[dataAmount];
        values = new Object[dataAmount];
        for (int i = 0; i < dataAmount; i++) {
            types[i] = buf.readByte();
            values[i] = PacketUpdateGui.readField(buf, types[i]);
        }
        extraData = ByteBufUtils.readTag(buf);
    }

    public static Object getSyncableForType(LocationIntPacket message, EntityPlayer player, IDescSynced.Type type) {
        switch (type) {
            case TILE_ENTITY:
                return message.getTileEntity(player.world);
            case SEMI_BLOCK:
                if (message.pos.equals(new BlockPos(0, 0, 0))) {
                    Container container = player.openContainer;
                    if (container instanceof ContainerLogistics) {
                        return ((ContainerLogistics) container).logistics;
                    }
                } else {
                    return SemiBlockManager.getInstance(player.world).getSemiBlock(player.world, message.pos);
                }
        }
        return null;
    }

    @Override
    public void handleClientSide(PacketDescription message, EntityPlayer player) {
        if (player.world.isBlockLoaded(message.pos)) {
            Object syncable = getSyncableForType(message, player, message.type);
            if (syncable instanceof IDescSynced) {
                IDescSynced descSynced = (IDescSynced) syncable;
                List<SyncedField> descFields = descSynced.getDescriptionFields();
                if (descFields != null && descFields.size() == message.types.length) {
                    for (int i = 0; i < descFields.size(); i++) {
                        descFields.get(i).setValue(message.values[i]);
                    }
                }
                descSynced.readFromPacket(message.extraData);
                descSynced.onDescUpdate();
            }
        }
    }

    @Override
    public void handleServerSide(PacketDescription message, EntityPlayer player) {
    }

    public NBTTagCompound writeNBT(NBTTagCompound compound) {
        compound.setTag("Pos", NBTUtil.createPosTag(pos));
        compound.setInteger("SyncType", type.ordinal());
        compound.setInteger("Length", values.length);
        ByteBuf buf = Unpooled.buffer();
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < types.length; i++) {
            NBTTagCompound element = new NBTTagCompound();
            element.setByte("Type", types[i]);
            buf.clear();
            PacketUpdateGui.writeField(buf, values[i], types[i]);
            element.setByteArray("Value", Arrays.copyOf(buf.array(), buf.writerIndex()));
            list.appendTag(element);
        }
        compound.setTag("Data", list);
        compound.setTag("Extra", extraData);

        return compound;
    }

    public PacketDescription(NBTTagCompound compound) {
        super(NBTUtil.getPosFromTag(compound.getCompoundTag("Pos")));
        type = IDescSynced.Type.values()[compound.getInteger("SyncType")];
        values = new Object[compound.getInteger("Length")];
        types = new byte[values.length];
        NBTTagList list = compound.getTagList("Data", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < values.length; i++) {
            NBTTagCompound element = list.getCompoundTagAt(i);
            types[i] = element.getByte("Type");
            byte[] b = element.getByteArray("Value");
            values[i] = PacketUpdateGui.readField(Unpooled.wrappedBuffer(b), types[i]);
        }
        extraData = compound.getCompoundTag("Extra");
    }
}
