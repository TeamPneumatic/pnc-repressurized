package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.item.Itemss;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public class PacketUpdateGPSAreaTool extends AbstractPacket<PacketUpdateGPSAreaTool> {
    private NBTTagCompound areaWidgetData;

    public PacketUpdateGPSAreaTool() {
    }

    public PacketUpdateGPSAreaTool(ProgWidgetArea area) {
        areaWidgetData = new NBTTagCompound();
        area.writeToNBT(areaWidgetData);
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        try {
            new PacketBuffer(buffer).writeCompoundTag(areaWidgetData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        try {
            areaWidgetData = new PacketBuffer(buffer).readCompoundTag();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleClientSide(PacketUpdateGPSAreaTool message, EntityPlayer player) {
        handleServerSide(null, player);
    }

    @Override
    public void handleServerSide(PacketUpdateGPSAreaTool message, EntityPlayer player) {
        ItemStack stack = player.getHeldItemMainhand();
        if (stack.getItem() == Itemss.GPS_AREA_TOOL){
            stack.setTagCompound(message.areaWidgetData);
        }
    }
}
