package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server from CommonArmorHandler to keep equipped armor pressures up-to-date
 */
public class PacketSyncItemAir {
    private EquipmentSlotType slot;
    private int air;

    public PacketSyncItemAir(EquipmentSlotType slot, int air) {
        this.slot = slot;
        this.air = air;
    }

    public PacketSyncItemAir(PacketBuffer buffer) {
        slot = EquipmentSlotType.values()[buffer.readByte()];
        air = buffer.readInt();
    }

    public void toBytes(PacketBuffer buffer) {
        buffer.writeByte(slot.ordinal());
        buffer.writeInt(air);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ItemStack stack = ClientUtils.getClientPlayer().getItemStackFromSlot(slot);
            if (stack.getItem() instanceof ItemPneumaticArmor) {
                CommonArmorHandler.getHandlerForPlayer().setAir(slot, air);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
