package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.NBTUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class PacketUpdateSearchStack extends AbstractPacket<PacketUpdateSearchStack> {

    private int itemId, itemDamage;

    public PacketUpdateSearchStack() {
    }

    public PacketUpdateSearchStack(ItemStack stack) {
        if (stack != null) {
            itemId = Item.getIdFromItem(stack.getItem());
            itemDamage = stack.getItemDamage();
        } else {
            itemId = itemDamage = -1;
        }
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(itemId);
        buffer.writeInt(itemDamage);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        itemId = buffer.readInt();
        itemDamage = buffer.readInt();
    }

    @Override
    public void handleClientSide(PacketUpdateSearchStack message, EntityPlayer player) {
    }

    @Override
    public void handleServerSide(PacketUpdateSearchStack message, EntityPlayer player) {
        ItemStack helmetStack = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (!helmetStack.isEmpty()) {
            NBTTagCompound tag = NBTUtil.getCompoundTag(helmetStack, "SearchStack");
            tag.setInteger("itemID", message.itemId);
            tag.setInteger("itemDamage", message.itemDamage);
        }
    }

}
