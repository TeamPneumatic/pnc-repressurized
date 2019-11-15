package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.inventory.ContainerAmadron;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Syncs the offer inventory on the tablet from client to server.
 */
public class PacketAmadronInvSync {
    private static final int INV_SIZE = ContainerAmadron.OFFERS_PER_PAGE * 2;

    private final List<ItemStack> items = new ArrayList<>(INV_SIZE);

    public PacketAmadronInvSync() {
    }

    public PacketAmadronInvSync(List<ItemStack> items) {
        Validate.isTrue(items.size() == INV_SIZE,
                "invalid list size: expected " + INV_SIZE + ", got " + items.size());
        this.items.addAll(items);
    }

    PacketAmadronInvSync(PacketBuffer buffer) {
        for (int i = 0; i < INV_SIZE; i++) {
            items.add(buffer.readItemStack());
        }
    }

    public void toBytes(PacketBuffer buf) {
        PacketBuffer pb = new PacketBuffer(buf);
        items.forEach(pb::writeItemStack);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player.openContainer instanceof ContainerAmadron) {
                ContainerAmadron container = (ContainerAmadron) player.openContainer;
                for (int i = 0; i < items.size(); i++) {
                    container.setStack(i, items.get(i));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
