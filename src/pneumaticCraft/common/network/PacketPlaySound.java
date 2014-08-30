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

public class PacketPlaySound extends LocationDoublePacket<PacketPlaySound>{
    private String sound;
    private float volume, pitch;
    private boolean bool;

    public PacketPlaySound(){}

    public PacketPlaySound(String sound, double x, double y, double z, float volume, float pitch, boolean bool){
        super(x, y, z);
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public void toBytes(ByteBuf buffer){
        super.toBytes(buffer);
        ByteBufUtils.writeUTF8String(buffer, sound);
        buffer.writeFloat(volume);
        buffer.writeFloat(pitch);
        buffer.writeBoolean(bool);
    }

    @Override
    public void fromBytes(ByteBuf buffer){
        super.fromBytes(buffer);
        sound = ByteBufUtils.readUTF8String(buffer);
        volume = buffer.readFloat();
        pitch = buffer.readFloat();
        bool = buffer.readBoolean();
    }

    @Override
    public void handleClientSide(PacketPlaySound message, EntityPlayer player){
        player.worldObj.playSound(message.x, message.y, message.z, message.sound, message.volume, message.pitch, message.bool);
    }

    @Override
    public void handleServerSide(PacketPlaySound message, EntityPlayer player){}

}
