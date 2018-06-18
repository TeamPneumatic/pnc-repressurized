package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.UpgradeRenderHandlerList;
import me.desht.pneumaticcraft.common.CommonHUDHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;

public class PacketToggleArmorFeature extends AbstractPacket<PacketToggleArmorFeature> {
    private byte featureIndex;
    private boolean state;
    private EntityEquipmentSlot slot;

    public PacketToggleArmorFeature() {
    }

    public PacketToggleArmorFeature(byte featureIndex, boolean state, EntityEquipmentSlot slot) {
        this.featureIndex = featureIndex;
        this.state = state;
        this.slot = slot;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        featureIndex = buf.readByte();
        state = buf.readBoolean();
        slot = EntityEquipmentSlot.values()[buf.readByte()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(featureIndex);
        buf.writeBoolean(state);
        buf.writeByte(slot.ordinal());
    }

    @Override
    public void handleClientSide(PacketToggleArmorFeature message, EntityPlayer player) {
    }

    @Override
    public void handleServerSide(PacketToggleArmorFeature message, EntityPlayer player) {
        if (message.featureIndex >= 0 && message.featureIndex < UpgradeRenderHandlerList.instance().getHandlersForSlot(message.slot).size()) {
            CommonHUDHandler.getHandlerForPlayer(player).setUpgradeRenderEnabled(message.slot, message.featureIndex, message.state);
        }
    }

}
