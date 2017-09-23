package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.BlockTrackUpgradeHandler;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.RenderBlockTarget;
import me.desht.pneumaticcraft.common.CommonHUDHandler;
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
        CommonHUDHandler.getHandlerForPlayer(player).setHackedBlock(new WorldAndCoord(player.world, message.pos));
        RenderBlockTarget target = HUDHandler.instance().getSpecificRenderer(BlockTrackUpgradeHandler.class).getTargetForCoord(message.pos);
        if (target != null) target.onHackConfirmServer();
    }

    @Override
    public void handleServerSide(PacketHackingBlockStart message, EntityPlayer player) {
        CommonHUDHandler.getHandlerForPlayer(player).setHackedBlock(new WorldAndCoord(player.world, message.pos));
        NetworkHandler.sendToAllAround(message, player.world);
    }

}
