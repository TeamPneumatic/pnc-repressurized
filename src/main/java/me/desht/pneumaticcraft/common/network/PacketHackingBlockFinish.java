package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.PneumaticCraftRepressurized;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IHackableBlock;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.hacking.HackableHandler;
import me.desht.pneumaticcraft.common.CommonHUDHandler;
import me.desht.pneumaticcraft.common.util.WorldAndCoord;
import me.desht.pneumaticcraft.lib.Sounds;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class PacketHackingBlockFinish extends LocationIntPacket<PacketHackingBlockFinish> {

    public PacketHackingBlockFinish() {
    }

    public PacketHackingBlockFinish(BlockPos pos) {
        super(pos);
    }

    public PacketHackingBlockFinish(WorldAndCoord coord) {
        super(coord.pos);
    }

    @Override
    public void handleClientSide(PacketHackingBlockFinish message, EntityPlayer player) {
        IHackableBlock hackableBlock = HackableHandler.getHackableForCoord(player.world, message.pos, player);
        if (hackableBlock != null) {
            hackableBlock.onHackFinished(player.world, message.pos, player);
            PneumaticCraftRepressurized.proxy.getHackTickHandler().trackBlock(new WorldAndCoord(player.world, message.pos), hackableBlock);
            CommonHUDHandler.getHandlerForPlayer(player).setHackedBlock(null);
            player.playSound(Sounds.HELMET_HACK_FINISH, 1.0F, 1.0F);
        }
    }

    @Override
    public void handleServerSide(PacketHackingBlockFinish message, EntityPlayer player) {
    }

}
