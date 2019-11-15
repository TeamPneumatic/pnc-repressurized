package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.item.ItemGPSAreaTool;
import me.desht.pneumaticcraft.common.item.ItemGPSTool;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Send when the GPS Tool GUI is closed, to update the held GPS tool settings
 */
public class PacketChangeGPSToolCoordinate extends LocationIntPacket {
    private Hand hand;
    private String variable;
    private int metadata;

    public PacketChangeGPSToolCoordinate() {
    }

    public PacketChangeGPSToolCoordinate(BlockPos pos, Hand hand, String variable, int metadata) {
        super(pos);
        this.hand = hand;
        this.variable = variable;
        this.metadata = metadata;
    }

    public PacketChangeGPSToolCoordinate(PacketBuffer buf) {
        super(buf);
        variable = buf.readString();
        metadata = buf.readInt();
        hand = buf.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);
        buf.writeString(variable);
        buf.writeInt(metadata);
        buf.writeBoolean(hand == Hand.MAIN_HAND);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            ItemStack playerStack = player.getHeldItem(hand);
            if (playerStack.getItem() == ModItems.GPS_TOOL) {
                ItemGPSTool.setVariable(playerStack, variable);
                if (pos.getY() >= 0) {
                    ItemGPSTool.setGPSLocation(playerStack, pos);
                }
            } else if (playerStack.getItem() == ModItems.GPS_AREA_TOOL) {
                ItemGPSAreaTool.setVariable(playerStack, variable, metadata);
                if (pos.getY() >= 0) {
                    ItemGPSAreaTool.setGPSPosAndNotify(player, pos, hand, metadata);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
