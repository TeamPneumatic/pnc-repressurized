package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.item.ItemPneumaticArmor;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.util.GlobalPosHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
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
            PlayerEntity player = ctx.get().getSender();
            if (player != null) {
                CommonArmorHandler handler = CommonArmorHandler.getHandlerForPlayer(player);
                if (handler.upgradeUsable(ArmorUpgradeRegistry.getInstance().coordTrackerHandler, false)) {
                    ItemStack stack = player.getItemStackFromSlot(EquipmentSlotType.HEAD);
                    ItemPneumaticArmor.setCoordTrackerPos(stack, GlobalPosHelper.makeGlobalPos(player.world, pos));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
