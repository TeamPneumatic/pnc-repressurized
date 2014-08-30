package pneumaticCraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.ByteBufUtils;

/**
 * MineChess
 * @author MineMaarten
 * www.minemaarten.com
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 */

public class PacketSpawnParticle extends LocationDoublePacket<PacketSpawnParticle>{

    private double dx, dy, dz;
    private String particleName;

    public PacketSpawnParticle(){}

    public PacketSpawnParticle(String particleName, double x, double y, double z, double dx, double dy, double dz){
        super(x, y, z);
        this.particleName = particleName;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    @Override
    public void toBytes(ByteBuf buffer){
        super.toBytes(buffer);
        ByteBufUtils.writeUTF8String(buffer, particleName);
        buffer.writeDouble(dx);
        buffer.writeDouble(dy);
        buffer.writeDouble(dz);
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        super.fromBytes(buffer);
        particleName = ByteBufUtils.readUTF8String(buffer);
        dx = buffer.readDouble();
        dy = buffer.readDouble();
        dz = buffer.readDouble();
    }

    @Override
    public void handleClientSide(PacketSpawnParticle message, EntityPlayer player){
        player.worldObj.spawnParticle(message.particleName, message.x, message.y, message.z, message.dx, message.dy, message.dz);
    }

    @Override
    public void handleServerSide(PacketSpawnParticle message, EntityPlayer player){}

}
