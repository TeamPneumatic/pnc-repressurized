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
import me.desht.pneumaticcraft.common.item.MicromissilesItem;
import me.desht.pneumaticcraft.common.item.MicromissilesItem.FireMode;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.Objects;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by client GUI to update (and maybe save as default) the configuration settings of a Micromissile item.
 */
public record PacketUpdateMicromissileSettings(float topSpeed, float accel, float damage, PointXY point, String entityFilter,
                                               FireMode fireMode, boolean saveDefault, InteractionHand hand)
        implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("packetupdatemicromissilesettings");

    public static PacketUpdateMicromissileSettings fromNetwork(FriendlyByteBuf buffer) {
        return new PacketUpdateMicromissileSettings(
            buffer.readFloat(),
            buffer.readFloat(),
            buffer.readFloat(),
            new PointXY(buffer.readInt(), buffer.readInt()),
            buffer.readUtf(),
            buffer.readEnum(FireMode.class),
            buffer.readBoolean(),
            buffer.readEnum(InteractionHand.class)
        );
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeFloat(topSpeed);
        buf.writeFloat(accel);
        buf.writeFloat(damage);
        buf.writeInt(point.x());
        buf.writeInt(point.y());
        buf.writeUtf(entityFilter);
        buf.writeEnum(fireMode);
        buf.writeBoolean(saveDefault);
        buf.writeEnum(hand);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketUpdateMicromissileSettings message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            ItemStack stack = player.getItemInHand(message.hand());
            if (!stack.isEmpty()) {
                message.applySettings(player, stack);
            } else {
                Log.warning("Received PacketUpdateMicromissileSettings but player does not hold a Micromissile? " + player.getName());
            }
        }));
    }

    private void applySettings(Player player, ItemStack stack) {
        if (!stack.hasTag()) stack.setTag(new CompoundTag());

        CompoundTag tag = Objects.requireNonNull(stack.getTag());
        tag.putFloat(MicromissilesItem.NBT_TURN_SPEED, accel);
        tag.putFloat(MicromissilesItem.NBT_TOP_SPEED, topSpeed);
        tag.putFloat(MicromissilesItem.NBT_DAMAGE, damage);
        tag.putInt(MicromissilesItem.NBT_PX, point.x());
        tag.putInt(MicromissilesItem.NBT_PY, point.y());
        tag.putString(MicromissilesItem.NBT_FILTER, entityFilter);
        tag.putString(MicromissilesItem.NBT_FIRE_MODE, fireMode.toString());

        if (saveDefault) {
            MicromissileDefaults.INSTANCE.setDefaults(player,
                    new MicromissileDefaults.Entry(topSpeed, accel, damage, point, entityFilter, fireMode)
            );
            MicromissileDefaults.INSTANCE.tryWriteToFile();
            player.level().playSound(null, player.blockPosition(), ModSounds.CHIRP.get(), SoundSource.PLAYERS, 1f, 1f);
        }
    }
}
