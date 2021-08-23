package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.lib.TileEntityConstants;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

/**
 * MineChess
 *
 * @author MineMaarten
 *         www.minemaarten.com
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 */

public abstract class LocationDoublePacket {

    protected double x, y, z;

    LocationDoublePacket() {
    }

    LocationDoublePacket(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    LocationDoublePacket(Vector3d v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    LocationDoublePacket(PacketBuffer buffer) {
        x = buffer.readDouble();
        y = buffer.readDouble();
        z = buffer.readDouble();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
    }

    PacketDistributor.TargetPoint getTargetPoint(World world) {
        return new PacketDistributor.TargetPoint(x, y, z, TileEntityConstants.PACKET_UPDATE_DISTANCE, world.dimension());
    }
}
