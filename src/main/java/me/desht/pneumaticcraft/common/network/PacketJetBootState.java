package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.api.item.IItemRegistry;
import me.desht.pneumaticcraft.common.CommonHUDHandler;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;

public class PacketJetBootState extends AbstractPacket<PacketJetBootState> {
    private boolean state;

    public PacketJetBootState() {
    }

    public PacketJetBootState(boolean state) {
        this.state = state;
    }

    @Override
    public void handleClientSide(PacketJetBootState message, EntityPlayer player) {

    }

    @Override
    public void handleServerSide(PacketJetBootState message, EntityPlayer player) {
        if (player.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() instanceof ItemPneumaticArmor) {
            CommonHUDHandler handler = CommonHUDHandler.getHandlerForPlayer(player);
            if (handler.getUpgradeCount(EntityEquipmentSlot.FEET, IItemRegistry.EnumUpgrade.JET_BOOTS) > 0
                    && (!message.state || handler.isJetBootsEnabled())) {
                handler.setJetBootsActive(message.state, player);
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
