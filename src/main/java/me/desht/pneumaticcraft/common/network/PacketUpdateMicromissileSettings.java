package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.config.subconfig.MicromissileDefaults;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.item.ItemMicromissiles;
import me.desht.pneumaticcraft.common.item.ItemMicromissiles.FireMode;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.IOException;
import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client GUI to update (and maybe save as default) the configuration settings of a Micromissile item.
 */
public class PacketUpdateMicromissileSettings {
    private float topSpeed;
    private float accel;
    private float damage;
    private PointXY point;
    private String entityFilter;
    private FireMode fireMode;
    private boolean saveDefault;
    private Hand hand;

    public PacketUpdateMicromissileSettings() {
        // empty
    }

    public PacketUpdateMicromissileSettings(float topSpeed, float accel, float damage, PointXY point, String entityFilter, FireMode fireMode, boolean saveDefault, Hand hand) {
        this.topSpeed = topSpeed;
        this.accel = accel;
        this.damage = damage;
        this.point = point;
        this.entityFilter = entityFilter;
        this.fireMode = fireMode;
        this.saveDefault = saveDefault;
        this.hand = hand;
    }

    PacketUpdateMicromissileSettings(PacketBuffer buffer) {
        topSpeed = buffer.readFloat();
        accel = buffer.readFloat();
        damage = buffer.readFloat();
        point = new PointXY(buffer.readInt(), buffer.readInt());
        entityFilter = buffer.readString(32767);
        fireMode = FireMode.values()[buffer.readByte()];
        saveDefault = buffer.readBoolean();
        hand = buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeFloat(topSpeed);
        buf.writeFloat(accel);
        buf.writeFloat(damage);
        buf.writeInt(point.x);
        buf.writeInt(point.y);
        buf.writeString(entityFilter);
        buf.writeByte(fireMode.ordinal());
        buf.writeBoolean(saveDefault);
        buf.writeBoolean(hand == Hand.MAIN_HAND);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            ItemStack stack = player.getHeldItem(hand);
            if (!stack.isEmpty()) {
                applySettings(player, stack);
            } else {
                Log.warning("Received PacketUpdateMicromissileSettings but player does not hold a Micromissile? " + player.getName());
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private void applySettings(PlayerEntity player, ItemStack stack) {
        if (!stack.hasTag()) stack.setTag(new CompoundNBT());

        CompoundNBT tag = stack.getTag();
        tag.putFloat(ItemMicromissiles.NBT_TURN_SPEED, accel);
        tag.putFloat(ItemMicromissiles.NBT_TOP_SPEED, topSpeed);
        tag.putFloat(ItemMicromissiles.NBT_DAMAGE, damage);
        tag.putInt(ItemMicromissiles.NBT_PX, point.x);
        tag.putInt(ItemMicromissiles.NBT_PY, point.y);
        tag.putString(ItemMicromissiles.NBT_FILTER, entityFilter);
        tag.putString(ItemMicromissiles.NBT_FIRE_MODE, fireMode.toString());

        if (saveDefault) {
            try {
                // TODO player capability?
                MicromissileDefaults.INSTANCE.setDefaults(player,
                        new MicromissileDefaults.Entry(topSpeed, accel, damage, point, entityFilter, fireMode)
                );
                MicromissileDefaults.INSTANCE.writeToFile();
                NetworkHandler.sendToPlayer(new PacketPlaySound(ModSounds.CHIRP.get(), SoundCategory.PLAYERS, player.getPosX(), player.getPosY(), player.getPosZ(), 1.0f, 1.0f, false), (ServerPlayerEntity) player);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
