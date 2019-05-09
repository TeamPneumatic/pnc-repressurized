package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.RenderBlockTarget;
import me.desht.pneumaticcraft.client.render.pneumatic_armor.upgrade_handler.BlockTrackUpgradeHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.util.WorldAndCoord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class PacketHackingBlockStart extends LocationIntPacket<PacketHackingBlockStart> {

    public PacketHackingBlockStart() {
    }

    public PacketHackingBlockStart(BlockPos pos) {
        super(pos);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
    }

    @Override
    public void handleClientSide(PacketHackingBlockStart message, EntityPlayer player) {
        CommonArmorHandler.getHandlerForPlayer(player).setHackedBlock(new WorldAndCoord(player.world, message.pos));
        RenderBlockTarget target = HUDHandler.instance().getSpecificRenderer(BlockTrackUpgradeHandler.class).getTargetForCoord(message.pos);
        if (target != null) target.onHackConfirmServer();
    }

    @Override
    public void handleServerSide(PacketHackingBlockStart message, EntityPlayer player) {
        CommonArmorHandler.getHandlerForPlayer(player).setHackedBlock(new WorldAndCoord(player.world, message.pos));
        NetworkHandler.sendToAllAround(message, player.world);
    }

}
