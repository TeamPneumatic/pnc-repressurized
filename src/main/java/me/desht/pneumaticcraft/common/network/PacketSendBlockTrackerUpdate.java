package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.api.client.pneumaticHelmet.IBlockTrackProvider.IBlockTrackHandler;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.BlockTrackUpgradeHandler;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.HUDHandler;
import me.desht.pneumaticcraft.client.render.pneumaticArmor.blockTracker.TrackerBlacklistManager;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class PacketSendBlockTrackerUpdate extends LocationIntPacket<PacketSendBlockTrackerUpdate> {

    private IBlockTrackHandler handler;
    private ByteBuf buffer;

    public PacketSendBlockTrackerUpdate() {
    }

    public PacketSendBlockTrackerUpdate(BlockPos pos, IBlockTrackHandler handler) {
        super(pos);
        this.handler = handler;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        super.toBytes(buffer);
        handler.toBytes(buffer);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        super.fromBytes(buffer);
        this.buffer = buffer.copy();
    }

    @Override
    public void handleClientSide(PacketSendBlockTrackerUpdate message, EntityPlayer player) {
        IBlockTrackHandler handler = HUDHandler.instance().getSpecificRenderer(BlockTrackUpgradeHandler.class).getBlockTrackHandler(message.pos);
        if (handler != null) {
            handler.fromBytes(message.buffer);
        }
    }

    @Override
    public void handleServerSide(PacketSendBlockTrackerUpdate message, EntityPlayer player) {
    }

}
