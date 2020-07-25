package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

/**
 * MineChess
 *
 * @author MineMaarten
 *         www.minemaarten.com
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 */

public abstract class LocationIntPacket {

    protected BlockPos pos;

    public LocationIntPacket() {
    }

    public LocationIntPacket(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
    }

    public LocationIntPacket(BlockPos pos) {
        this.pos = pos;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
    }

    PacketDistributor.TargetPoint getTargetPoint(World world) {
        return getTargetPoint(world, TileEntityConstants.PACKET_UPDATE_DISTANCE);
    }

    PacketDistributor.TargetPoint getTargetPoint(World world, double updateDistance) {
        return new PacketDistributor.TargetPoint(pos.getX(), pos.getY(), pos.getZ(), updateDistance, world.func_234923_W_());
    }
}
