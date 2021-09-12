package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client from area tool GUI to update stored settings
 */
public class PacketUpdateGPSAreaTool {
    private CompoundNBT areaWidgetData;
    private Hand hand;

    public PacketUpdateGPSAreaTool(ProgWidgetArea area, Hand hand) {
        this.hand = hand;
        this.areaWidgetData = new CompoundNBT();
        area.writeToNBT(areaWidgetData);
    }

    public PacketUpdateGPSAreaTool(PacketBuffer buffer) {
        try {
            areaWidgetData = buffer.readNbt();
            hand = buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toBytes(PacketBuffer buffer) {
        try {
            buffer.writeNbt(areaWidgetData);
            buffer.writeBoolean(hand == Hand.MAIN_HAND);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() != null) {
                ItemStack stack = ctx.get().getSender().getItemInHand(hand);
                if (stack.getItem() == ModItems.GPS_AREA_TOOL.get()) {
                    stack.setTag(areaWidgetData);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
