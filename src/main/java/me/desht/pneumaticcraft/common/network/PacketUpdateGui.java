package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.inventory.ContainerPneumaticBase;
import me.desht.pneumaticcraft.common.inventory.SyncedField;
import me.desht.pneumaticcraft.common.inventory.SyncedField.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import java.io.IOException;

public class PacketUpdateGui extends AbstractPacket<PacketUpdateGui> {
    private int syncId;
    private Object value;
    private byte type;

    public PacketUpdateGui() {
    }

    public PacketUpdateGui(int syncId, SyncedField syncField) {
        this.syncId = syncId;
        value = syncField.getValue();
        type = getType(syncField);
    }

    public static byte getType(SyncedField syncedField) {
        if (syncedField instanceof SyncedInt) return 0;
        else if (syncedField instanceof SyncedFloat) return 1;
        else if (syncedField instanceof SyncedDouble) return 2;
        else if (syncedField instanceof SyncedBoolean) return 3;
        else if (syncedField instanceof SyncedString) return 4;
        else if (syncedField instanceof SyncedEnum) return 5;
        else if (syncedField instanceof SyncedItemStack) return 6;
        else if (syncedField instanceof SyncedFluidTank) return 7;
        else if (syncedField instanceof SyncedItemStackHandler) return 8;
        else {
            throw new IllegalArgumentException("Invalid sync type! " + syncedField);
        }
    }

    public static Object readField(ByteBuf buf, int type) {
        switch (type) {
            case 0:
                return buf.readInt();
            case 1:
                return buf.readFloat();
            case 2:
                return buf.readDouble();
            case 3:
                return buf.readBoolean();
            case 4:
                return ByteBufUtils.readUTF8String(buf);
            case 5:
                return buf.readByte();
            case 6:
                return ByteBufUtils.readItemStack(buf);
            case 7:
                if (!buf.readBoolean()) return null;
                return new FluidStack(FluidRegistry.getFluid(ByteBufUtils.readUTF8String(buf)), buf.readInt(), ByteBufUtils.readTag(buf));
            case 8:
                try {
                    PacketBuffer packetBuffer = new PacketBuffer(buf);
                    NBTTagCompound tag = packetBuffer.readCompoundTag();
                    if (tag == null) return EmptyHandler.INSTANCE;
                    ItemStackHandler handler = new ItemStackHandler();
                    handler.deserializeNBT(tag);
                    return handler;
                } catch (IOException e) {
                    return EmptyHandler.INSTANCE;
                }
        }
        throw new IllegalArgumentException("Invalid sync type! " + type);
    }

    public static void writeField(ByteBuf buf, Object value, int type) {
        switch (type) {
            case 0:
                buf.writeInt((Integer) value);
                break;
            case 1:
                buf.writeFloat((Float) value);
                break;
            case 2:
                buf.writeDouble((Double) value);
                break;
            case 3:
                buf.writeBoolean((Boolean) value);
                break;
            case 4:
                ByteBufUtils.writeUTF8String(buf, (String) value);
                break;
            case 5:
                buf.writeByte((Byte) value);
                break;
            case 6:
                ByteBufUtils.writeItemStack(buf, (ItemStack) value);
                break;
            case 7:
                buf.writeBoolean(value != null);
                if (value != null) {
                    FluidStack stack = (FluidStack) value;
                    ByteBufUtils.writeUTF8String(buf, stack.getFluid().getName());
                    buf.writeInt(stack.amount);
                    ByteBufUtils.writeTag(buf, stack.tag);
                }
                break;
            case 8:
                NBTTagCompound tag = ((ItemStackHandler) value).serializeNBT();
                PacketBuffer packetBuffer = new PacketBuffer(buf);
                packetBuffer.writeCompoundTag(tag);
                break;
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        syncId = buf.readInt();
        type = buf.readByte();
        value = readField(buf, type);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(syncId);
        buf.writeByte(type);
        writeField(buf, value, type);
    }

    @Override
    public boolean canHandlePacketAlready(PacketUpdateGui message, EntityPlayer player) {
        return super.canHandlePacketAlready(message, player) && player.openContainer instanceof ContainerPneumaticBase;
    }

    @Override
    public void handleClientSide(PacketUpdateGui message, EntityPlayer player) {
        Container container = player.openContainer;
        if (container instanceof ContainerPneumaticBase) {
            ((ContainerPneumaticBase) container).updateField(message.syncId, message.value);
        }
    }

    @Override
    public void handleServerSide(PacketUpdateGui message, EntityPlayer player) {
    }

}
