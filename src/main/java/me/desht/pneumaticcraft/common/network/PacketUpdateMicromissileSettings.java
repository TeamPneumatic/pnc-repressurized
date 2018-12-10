package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import me.desht.pneumaticcraft.common.config.MicromissileDefaults;
import me.desht.pneumaticcraft.common.item.ItemMicromissiles;
import me.desht.pneumaticcraft.common.item.ItemMicromissiles.FireMode;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.Sounds;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.awt.*;
import java.io.IOException;

public class PacketUpdateMicromissileSettings extends AbstractPacket<PacketUpdateMicromissileSettings> {
    private float topSpeed;
    private float accel;
    private float damage;
    private Point point;
    private String entityFilter;
    private FireMode fireMode;
    private boolean saveDefault;

    public PacketUpdateMicromissileSettings() {
    }

    public PacketUpdateMicromissileSettings(float topSpeed, float accel, float damage, Point point, String entityFilter, FireMode fireMode, boolean saveDefault) {
        this.topSpeed = topSpeed;
        this.accel = accel;
        this.damage = damage;
        this.point = point;
        this.entityFilter = entityFilter;
        this.fireMode = fireMode;
        this.saveDefault = saveDefault;
    }

    @Override
    public void handleClientSide(PacketUpdateMicromissileSettings message, EntityPlayer player) {
    }

    @Override
    public void handleServerSide(PacketUpdateMicromissileSettings message, EntityPlayer player) {
        ItemStack stack = ItemMicromissiles.getHeldMicroMissile(player);
        if (!stack.isEmpty()) {
            applySettings(message, player, stack);
        } else {
            Log.warning("Received PacketUpdateMicromissileSettings but player does not hold a Micromissile? " + player.getName());
        }
    }

    private void applySettings(PacketUpdateMicromissileSettings message, EntityPlayer player, ItemStack stack) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());

        NBTTagCompound tag = stack.getTagCompound();
        tag.setFloat(ItemMicromissiles.NBT_TURN_SPEED, message.accel);
        tag.setFloat(ItemMicromissiles.NBT_TOP_SPEED, message.topSpeed);
        tag.setFloat(ItemMicromissiles.NBT_DAMAGE, message.damage);
        tag.setInteger(ItemMicromissiles.NBT_PX, message.point.x);
        tag.setInteger(ItemMicromissiles.NBT_PY, message.point.y);
        tag.setString(ItemMicromissiles.NBT_FILTER, message.entityFilter);
        tag.setString(ItemMicromissiles.NBT_FIRE_MODE, message.fireMode.toString());

        if (message.saveDefault) {
            try {
                MicromissileDefaults.INSTANCE.setDefaults(player,
                        new MicromissileDefaults.Entry(message.topSpeed, message.accel, message.damage, message.point, message.entityFilter, message.fireMode)
                );
                MicromissileDefaults.INSTANCE.writeToFile();
                NetworkHandler.sendTo(new PacketPlaySound(Sounds.CHIRP, SoundCategory.PLAYERS, player.posX, player.posY, player.posZ, 1.0f, 1.0f, false), (EntityPlayerMP) player);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        topSpeed = buf.readFloat();
        accel = buf.readFloat();
        damage = buf.readFloat();
        point = new Point(buf.readInt(), buf.readInt());
        entityFilter = ByteBufUtils.readUTF8String(buf);
        fireMode = FireMode.fromString(ByteBufUtils.readUTF8String(buf));
        saveDefault = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeFloat(topSpeed);
        buf.writeFloat(accel);
        buf.writeFloat(damage);
        buf.writeInt(point.x);
        buf.writeInt(point.y);
        ByteBufUtils.writeUTF8String(buf, entityFilter);
        ByteBufUtils.writeUTF8String(buf, fireMode.toString());
        buf.writeBoolean(saveDefault);
    }
}
