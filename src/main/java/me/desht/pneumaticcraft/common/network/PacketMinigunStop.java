package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.item.ItemMinigun;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server when player switches away from a held minigun which is active (i.e. spinning)
 */
public class PacketMinigunStop {
    private final ItemStack stack;

    public PacketMinigunStop(ItemStack stack) {
        this.stack = stack;
    }

    public PacketMinigunStop(PacketBuffer buf) {
        this.stack = buf.readItem();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeItem(stack);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ClientUtils.getClientPlayer();
            Minigun minigun = ((ItemMinigun) stack.getItem()).getMinigun(stack, player);
            minigun.setMinigunSpeed(0);
            minigun.setMinigunActivated(false);
            minigun.setMinigunTriggerTimeOut(0);
            player.playSound(ModSounds.MINIGUN_STOP.get(), 1f, 1f);
        });
        ctx.get().setPacketHandled(true);
    }
}
