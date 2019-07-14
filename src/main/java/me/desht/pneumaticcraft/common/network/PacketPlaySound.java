package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server to play a sound at a specific location
 */
public class PacketPlaySound extends LocationDoublePacket {
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
        this.soundName = soundEvent.getRegistryName();
        this.category = category;
        this.volume = volume;
        this.pitch = pitch;
        this.distanceDelay = distanceDelay;
    }

    public PacketPlaySound(SoundEvent soundEvent, SoundCategory category, BlockPos pos, float volume, float pitch, boolean distanceDelay) {
        this(soundEvent, category, pos.getX(), pos.getY(), pos.getZ(), volume, pitch, distanceDelay);
    }

    public PacketPlaySound(PacketBuffer buffer) {
        super(buffer);
        soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(PacketUtil.readUTF8String(buffer)));
        category = SoundCategory.values()[buffer.readInt()];
        volume = buffer.readFloat();
        pitch = buffer.readFloat();
        distanceDelay = buffer.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        super.toBytes(buffer);
        PacketUtil.writeUTF8String(buffer, soundName.toString());
        buffer.writeInt(category.ordinal());
        buffer.writeFloat(volume);
        buffer.writeFloat(pitch);
        buffer.writeBoolean(distanceDelay);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ctx.get().getSender().world.playSound(x, y, z, soundEvent, category, volume, pitch, distanceDelay));
        ctx.get().setPacketHandled(true);
    }
}
