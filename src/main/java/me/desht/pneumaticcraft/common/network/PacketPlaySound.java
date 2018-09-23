package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

/**
 * MineChess
 *
 * @author MineMaarten
 *         www.minemaarten.com
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 */

public class PacketPlaySound extends LocationDoublePacket<PacketPlaySound> {
    private SoundEvent soundEvent;
    private SoundCategory category;
    private float volume;
    private float pitch;
    private boolean distanceDelay;
    private ResourceLocation soundName;

    public PacketPlaySound() {
    }

    public PacketPlaySound(SoundEvent soundEvent, SoundCategory category, double x, double y, double z, float volume, float pitch, boolean distanceDelay) {
        super(x, y, z);
        this.soundEvent = soundEvent;
        this.soundName = soundEvent.soundName;
        this.category = category;
        this.volume = volume;
        this.pitch = pitch;
        this.distanceDelay = distanceDelay;
    }

    public PacketPlaySound(SoundEvent soundEvent, SoundCategory category, BlockPos pos, float volume, float pitch, boolean distanceDelay) {
        this(soundEvent, category, pos.getX(), pos.getY(), pos.getZ(), volume, pitch, distanceDelay);
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        super.toBytes(buffer);
        ByteBufUtils.writeUTF8String(buffer, soundName.toString());
        buffer.writeInt(category.ordinal());
        buffer.writeFloat(volume);
        buffer.writeFloat(pitch);
        buffer.writeBoolean(distanceDelay);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        super.fromBytes(buffer);
        soundEvent = new SoundEvent(new ResourceLocation(ByteBufUtils.readUTF8String(buffer)));
        category = SoundCategory.values()[buffer.readInt()];
        volume = buffer.readFloat();
        pitch = buffer.readFloat();
        distanceDelay = buffer.readBoolean();
    }

    @Override
    public void handleClientSide(PacketPlaySound message, EntityPlayer player) {
        player.world.playSound(message.x, message.y, message.z, message.soundEvent, message.category, message.volume, message.pitch, message.distanceDelay);
    }

    @Override
    public void handleServerSide(PacketPlaySound message, EntityPlayer player) {
    }

}
