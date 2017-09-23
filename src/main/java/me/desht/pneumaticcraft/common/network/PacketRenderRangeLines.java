package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.common.tileentity.IRangeLineShower;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class PacketRenderRangeLines extends LocationIntPacket<PacketRenderRangeLines> {

    public PacketRenderRangeLines() {
    }

    public PacketRenderRangeLines(TileEntity te) {
        super(te.getPos());
    }

    @Override
    public void handleClientSide(PacketRenderRangeLines message, EntityPlayer player) {
        TileEntity te = message.getTileEntity(player.getEntityWorld());
        if (te instanceof IRangeLineShower) {
            ((IRangeLineShower) te).showRangeLines();
        }
    }

    @Override
    public void handleServerSide(PacketRenderRangeLines message, EntityPlayer player) {
    }

}
