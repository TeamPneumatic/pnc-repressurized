package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.render.pneumatic_armor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client to switch an armor module on or off
 */
public class PacketToggleArmorFeature {
    private byte featureIndex;
    private boolean state;
    private EquipmentSlotType slot;

    public PacketToggleArmorFeature() {
    }

    public PacketToggleArmorFeature(byte featureIndex, boolean state, EquipmentSlotType slot) {
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
            if (featureIndex >= 0 && featureIndex < UpgradeRenderHandlerList.instance().getHandlersForSlot(slot).size()) {
                CommonArmorHandler.getHandlerForPlayer(ctx.get().getSender()).setUpgradeRenderEnabled(slot, featureIndex, state);
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
