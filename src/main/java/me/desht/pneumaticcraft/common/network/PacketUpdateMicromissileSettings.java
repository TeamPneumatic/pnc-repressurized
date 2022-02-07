/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.network;

import me.desht.pneumaticcraft.client.util.PointXY;
import me.desht.pneumaticcraft.common.config.subconfig.MicromissileDefaults;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.item.ItemMicromissiles;
import me.desht.pneumaticcraft.common.item.ItemMicromissiles.FireMode;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client GUI to update (and maybe save as default) the configuration settings of a Micromissile item.
 */
public class PacketUpdateMicromissileSettings {
    private final float topSpeed;
    private final float accel;
    private final float damage;
    private final PointXY point;
    private final String entityFilter;
    private final FireMode fireMode;
    private final boolean saveDefault;
    private final InteractionHand hand;

    public PacketUpdateMicromissileSettings(float topSpeed, float accel, float damage, PointXY point, String entityFilter, FireMode fireMode, boolean saveDefault, InteractionHand hand) {
        this.topSpeed = topSpeed;
        this.accel = accel;
        this.damage = damage;
        this.point = point;
        this.entityFilter = entityFilter;
        this.fireMode = fireMode;
        this.saveDefault = saveDefault;
        this.hand = hand;
    }

    PacketUpdateMicromissileSettings(FriendlyByteBuf buffer) {
        topSpeed = buffer.readFloat();
        accel = buffer.readFloat();
        damage = buffer.readFloat();
        point = new PointXY(buffer.readInt(), buffer.readInt());
        entityFilter = buffer.readUtf(32767);
        fireMode = FireMode.values()[buffer.readByte()];
        saveDefault = buffer.readBoolean();
        hand = buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeFloat(topSpeed);
        buf.writeFloat(accel);
        buf.writeFloat(damage);
        buf.writeInt(point.x());
        buf.writeInt(point.y());
        buf.writeUtf(entityFilter);
        buf.writeByte(fireMode.ordinal());
        buf.writeBoolean(saveDefault);
        buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = Objects.requireNonNull(ctx.get().getSender());
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.isEmpty()) {
                applySettings(player, stack);
            } else {
                Log.warning("Received PacketUpdateMicromissileSettings but player does not hold a Micromissile? " + player.getName());
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private void applySettings(Player player, ItemStack stack) {
        if (!stack.hasTag()) stack.setTag(new CompoundTag());

        CompoundTag tag = Objects.requireNonNull(stack.getTag());
        tag.putFloat(ItemMicromissiles.NBT_TURN_SPEED, accel);
        tag.putFloat(ItemMicromissiles.NBT_TOP_SPEED, topSpeed);
        tag.putFloat(ItemMicromissiles.NBT_DAMAGE, damage);
        tag.putInt(ItemMicromissiles.NBT_PX, point.x());
        tag.putInt(ItemMicromissiles.NBT_PY, point.y());
        tag.putString(ItemMicromissiles.NBT_FILTER, entityFilter);
        tag.putString(ItemMicromissiles.NBT_FIRE_MODE, fireMode.toString());

        if (saveDefault) {
            // TODO 1.17 player capability would be a better way to handle this
            MicromissileDefaults.INSTANCE.setDefaults(player,
                    new MicromissileDefaults.Entry(topSpeed, accel, damage, point, entityFilter, fireMode)
            );
            MicromissileDefaults.INSTANCE.tryWriteToFile();
            NetworkHandler.sendToPlayer(new PacketPlaySound(ModSounds.CHIRP.get(), SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1.0f, 1.0f, false), (ServerPlayer) player);
        }
    }
}
