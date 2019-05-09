package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;

/**
 * Sent from client to server to tell the server the player is activating/deactivating jet boots.  Toggled when
 * Jump key is pressed or released.
 */
public class PacketJetBootsActivate extends AbstractPacket<PacketJetBootsActivate> {
    private boolean state;

    public PacketJetBootsActivate() {
    }

    public PacketJetBootsActivate(boolean state) {
        this.state = state;
    }

    @Override
    public void handleClientSide(PacketJetBootsActivate message, EntityPlayer player) {

    }

    @Override
    public void handleServerSide(PacketJetBootsActivate message, EntityPlayer player) {
        if (player.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() instanceof ItemPneumaticArmor) {
            CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
            if (handler.getUpgradeCount(EntityEquipmentSlot.FEET, IItemRegistry.EnumUpgrade.JET_BOOTS) > 0
                    && (!message.state || handler.isJetBootsEnabled())) {
                handler.setJetBootsActive(message.state);
            }
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        state = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(state);
    }
}
