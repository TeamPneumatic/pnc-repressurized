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
    private final int eyepiece;

    public PacketUpdateArmorColors() {
        for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
            ItemStack stack = ClientUtils.getClientPlayer().getItemBySlot(slot);
            if (stack.getItem() instanceof ItemPneumaticArmor) {
                cols[slot.getIndex()][0] = ((ItemPneumaticArmor) stack.getItem()).getColor(stack);
                cols[slot.getIndex()][1] = ((ItemPneumaticArmor) stack.getItem()).getSecondaryColor(stack);
            }
        }
        ItemStack stack = ClientUtils.getClientPlayer().getItemBySlot(EquipmentSlotType.HEAD);
        if (stack.getItem() instanceof ItemPneumaticArmor) {
            eyepiece = ((ItemPneumaticArmor) stack.getItem()).getEyepieceColor(stack);
        } else {
            eyepiece = 0;
        }
    }

    public PacketUpdateArmorColors(PacketBuffer buffer) {
        for (int i = 0; i < 4; i++) {
            cols[i][0] = buffer.readInt();
            cols[i][1] = buffer.readInt();
        }
        eyepiece = buffer.readInt();
    }

    public void toBytes(PacketBuffer buffer) {
        for (int i = 0; i < 4; i++) {
            buffer.writeInt(cols[i][0]);
            buffer.writeInt(cols[i][1]);
        }
        buffer.writeInt(eyepiece);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                for (EquipmentSlotType slot : ArmorUpgradeRegistry.ARMOR_SLOTS) {
                    ItemStack stack = player.getItemBySlot(slot);
                    if (stack.getItem() instanceof ItemPneumaticArmor) {
                        ((ItemPneumaticArmor) stack.getItem()).setColor(stack, cols[slot.getIndex()][0]);
                        ((ItemPneumaticArmor) stack.getItem()).setSecondaryColor(stack, cols[slot.getIndex()][1]);
                    }
                }
                ItemStack stack = player.getItemBySlot(EquipmentSlotType.HEAD);
                if (stack.getItem() instanceof ItemPneumaticArmor) {
                    ((ItemPneumaticArmor) stack.getItem()).setEyepieceColor(stack, eyepiece);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
