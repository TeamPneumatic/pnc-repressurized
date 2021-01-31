package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent from Pneumatic Armor colors GUI to re-color armor pieces
 */
public class PacketUpdateArmorColors {
    private final int[][] cols = new int[4][2];

    public PacketUpdateArmorColors() {
        for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            ItemStack stack = ClientUtils.getClientPlayer().getItemStackFromSlot(slot);
            if (stack.getItem() instanceof ItemPneumaticArmor) {
                cols[slot.getIndex()][0] = ((ItemPneumaticArmor) stack.getItem()).getColor(stack);
                cols[slot.getIndex()][1] = ((ItemPneumaticArmor) stack.getItem()).getSecondaryColor(stack);
            }
        }
    }

    public PacketUpdateArmorColors(PacketBuffer buffer) {
        for (int i = 0; i < 4; i++) {
            cols[i][0] = buffer.readInt();
            cols[i][1] = buffer.readInt();
        }
    }

    public void toBytes(PacketBuffer buffer) {
        for (int i = 0; i < 4; i++) {
            buffer.writeInt(cols[i][0]);
            buffer.writeInt(cols[i][1]);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
                    ItemStack stack = player.getItemStackFromSlot(slot);
                    if (stack.getItem() instanceof ItemPneumaticArmor) {
                        ((ItemPneumaticArmor) stack.getItem()).setColor(stack, cols[slot.getIndex()][0]);
                        ((ItemPneumaticArmor) stack.getItem()).setSecondaryColor(stack, cols[slot.getIndex()][1]);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
