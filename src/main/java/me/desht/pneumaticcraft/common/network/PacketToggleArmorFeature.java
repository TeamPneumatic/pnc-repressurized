package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client to switch an armor module on or off
 */
public class PacketToggleArmorFeature {
    private final byte featureIndex;
    private final boolean state;
    private final EquipmentSlotType slot;

    public PacketToggleArmorFeature(EquipmentSlotType slot, byte featureIndex, boolean state) {
        this.featureIndex = featureIndex;
        this.state = state;
        this.slot = slot;
    }

    PacketToggleArmorFeature(PacketBuffer buffer) {
        featureIndex = buffer.readByte();
        state = buffer.readBoolean();
        slot = EquipmentSlotType.values()[buffer.readByte()];
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeByte(featureIndex);
        buf.writeBoolean(state);
        buf.writeByte(slot.ordinal());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ctx.get().getSender();
            if (player != null && featureIndex >= 0
                    && featureIndex < ArmorUpgradeRegistry.getInstance().getHandlersForSlot(slot).size()
                    && (ItemPneumaticArmor.isPneumaticArmorPiece(player, slot) || slot == EquipmentSlotType.HEAD && featureIndex == 0))
            {
                CommonArmorHandler.getHandlerForPlayer(player).setUpgradeEnabled(slot, featureIndex, state);
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
