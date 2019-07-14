package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.progwidgets.ProgWidgetArea;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketUpdateGPSAreaTool {
    private CompoundNBT areaWidgetData;
    private Hand hand;

    public PacketUpdateGPSAreaTool() {
    }

    public PacketUpdateGPSAreaTool(ProgWidgetArea area, Hand hand) {
        this.hand = hand;
        areaWidgetData = new CompoundNBT();
        area.writeToNBT(areaWidgetData);
    }

    public PacketUpdateGPSAreaTool(PacketBuffer buffer) {
        try {
            areaWidgetData = buffer.readCompoundTag();
            hand = buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toBytes(ByteBuf buffer) {
        try {
            new PacketBuffer(buffer).writeCompoundTag(areaWidgetData);
            buffer.writeBoolean(hand == Hand.MAIN_HAND);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ItemStack stack = ctx.get().getSender().getHeldItem(hand);
            if (stack.getItem() == ModItems.GPS_AREA_TOOL) {
                stack.setTag(areaWidgetData);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
