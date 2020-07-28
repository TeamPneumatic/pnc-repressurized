package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.util.GlobalPosHelper;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client when player right-clicks a block to select a coord tracker target
 */
public class PacketCoordTrackUpdate extends LocationIntPacket {

    private ResourceLocation dimensionID;

    public PacketCoordTrackUpdate() {
    }

    public PacketCoordTrackUpdate(BlockPos pos) {
        super(pos);
    }

    public PacketCoordTrackUpdate(PacketBuffer buffer) {
        super(buffer);
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        super.toBytes(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() != null) {
                ItemStack stack = ctx.get().getSender().getItemStackFromSlot(EquipmentSlotType.HEAD);
                World world = ctx.get().getSender().getServerWorld();
                if (stack.getItem() instanceof ItemPneumaticArmor) {
                    ItemPneumaticArmor.setCoordTrackerPos(stack, GlobalPosHelper.makeGlobalPos(world, pos));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
