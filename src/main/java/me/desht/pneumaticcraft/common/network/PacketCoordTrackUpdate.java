package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
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

    public PacketCoordTrackUpdate(World world, BlockPos pos) {
        super(pos);
        dimensionID = DimensionType.getKey(world.getDimension().getType());
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        super.toBytes(buffer);
        PacketUtil.writeUTF8String(buffer, dimensionID.toString());
    }

    public PacketCoordTrackUpdate(PacketBuffer buffer) {
        super(buffer);
        dimensionID = new ResourceLocation(PacketUtil.readUTF8String(buffer));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ItemStack stack = ctx.get().getSender().getItemStackFromSlot(EquipmentSlotType.HEAD);
            if (stack.getItem() instanceof ItemPneumaticArmor) {
                ItemPneumaticArmor.setCoordTrackerPos(stack, dimensionID, pos);
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
