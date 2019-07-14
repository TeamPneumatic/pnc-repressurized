package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client to update the searched item (Pneumatic Helmet search upgrade)
 */
public class PacketUpdateSearchItem {

    private String itemId;

    public PacketUpdateSearchItem() {
    }

    public PacketUpdateSearchItem(Item item) {
        itemId = item.getRegistryName().toString();
    }

    public PacketUpdateSearchItem(PacketBuffer buffer) {
        itemId = PacketUtil.readUTF8String(buffer);
    }

    public void toBytes(ByteBuf buffer) {
        PacketUtil.writeUTF8String(buffer, itemId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ItemStack helmetStack = ctx.get().getSender().getItemStackFromSlot(EquipmentSlotType.HEAD);
            Item searchedItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
            if (!helmetStack.isEmpty() && searchedItem != null) {
                ItemPneumaticArmor.setSearchedItem(helmetStack, searchedItem);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
