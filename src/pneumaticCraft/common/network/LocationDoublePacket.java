package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.World;
import pneumaticCraft.lib.TileEntityConstants;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;

/**
 * MineChess
 * @author MineMaarten
 * www.minemaarten.com
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 */

public abstract class LocationDoublePacket<REQ extends IMessage> extends AbstractPacket<REQ>{

    protected double x, y, z;

    public LocationDoublePacket(){}

    public LocationDoublePacket(double x, double y, double z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void toBytes(ByteBuf buf){
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
    }

    @Override
    public void fromBytes(ByteBuf buf){
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
    }

    public NetworkRegistry.TargetPoint getTargetPoint(World world){
        return getTargetPoint(world, TileEntityConstants.PACKET_UPDATE_DISTANCE);
    }

    public NetworkRegistry.TargetPoint getTargetPoint(World world, double updateDistance){
        return new NetworkRegistry.TargetPoint(world.provider.dimensionId, x, y, z, updateDistance);
    }
}
