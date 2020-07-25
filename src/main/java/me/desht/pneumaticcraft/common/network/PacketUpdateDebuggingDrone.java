package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.entity.living.EntityDrone;
import me.desht.pneumaticcraft.common.util.NBTUtils;
import me.desht.pneumaticcraft.lib.NBTKeys;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client when the drone debug key is pressed (for a valid target)
 */
public class PacketUpdateDebuggingDrone {

    private int entityId;

    public PacketUpdateDebuggingDrone() {
    }

    public PacketUpdateDebuggingDrone(int entityId) {
        this.entityId = entityId;
    }

    public PacketUpdateDebuggingDrone(PacketBuffer buffer) {
        this.entityId = buffer.readInt();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(entityId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            ItemStack stack = player.getItemStackFromSlot(EquipmentSlotType.HEAD);
            if (!stack.isEmpty()) {
                NBTUtils.setInteger(stack, NBTKeys.PNEUMATIC_HELMET_DEBUGGING_DRONE, entityId);
                if (entityId > 0) {
                    Entity entity = player.world.getEntityByID(entityId);
                    if (entity instanceof EntityDrone) {
                        ((EntityDrone) entity).trackAsDebugged(player);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
