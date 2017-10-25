package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.inventory.ContainerAmadron;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;

/**
 * Syncs the offer inventory on the tablet from client to server.
 */
public class PacketAmadronInvSync extends AbstractPacket<PacketAmadronInvSync> {
    private static final int INV_SIZE = ContainerAmadron.OFFERS_PER_PAGE * 2;

    private final List<ItemStack> items = new ArrayList<>(INV_SIZE);

    public PacketAmadronInvSync() {
    }

    public PacketAmadronInvSync(List<ItemStack> items) {
        Validate.isTrue(items.size() == INV_SIZE,
                "invalid list size: expected " + INV_SIZE + ", got " + items.size());
        this.items.addAll(items);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        for (int i = 0; i < INV_SIZE; i++) {
            items.add(ByteBufUtils.readItemStack(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        for (ItemStack stack : items) {
            ByteBufUtils.writeItemStack(buf, stack);
        }
    }

    @Override
    public void handleClientSide(PacketAmadronInvSync message, EntityPlayer player) {

    }

    @Override
    public void handleServerSide(PacketAmadronInvSync message, EntityPlayer player) {
        if (player.openContainer instanceof ContainerAmadron) {
            ContainerAmadron container = (ContainerAmadron) player.openContainer;
            for (int i = 0; i < items.size(); i++) {
                container.setStack(i, items.get(i));
            }
        }
    }

}
