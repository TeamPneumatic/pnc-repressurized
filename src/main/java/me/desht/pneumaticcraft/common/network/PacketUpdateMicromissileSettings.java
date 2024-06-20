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

import me.desht.pneumaticcraft.common.config.subconfig.MicromissileDefaults;
import me.desht.pneumaticcraft.common.item.MicromissilesItem;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: SERVER
 * Sent by client GUI to update (and maybe save as default) the configuration settings of a Micromissile item.
 */
public record PacketUpdateMicromissileSettings(MicromissilesItem.Settings settings, boolean saveDefault, InteractionHand hand)
        implements CustomPacketPayload {
    public static final Type<PacketUpdateMicromissileSettings> TYPE = new Type<>(RL("packetupdatemicromissilesettings"));

    public static final StreamCodec<FriendlyByteBuf, PacketUpdateMicromissileSettings> STREAM_CODEC = StreamCodec.composite(
            MicromissilesItem.Settings.STREAM_CODEC, PacketUpdateMicromissileSettings::settings,
            ByteBufCodecs.BOOL, PacketUpdateMicromissileSettings::saveDefault,
            NeoForgeStreamCodecs.enumCodec(InteractionHand.class), PacketUpdateMicromissileSettings::hand,
            PacketUpdateMicromissileSettings::new
    );

    @Override
    public Type<PacketUpdateMicromissileSettings> type() {
        return TYPE;
    }

    public static void handle(PacketUpdateMicromissileSettings message, IPayloadContext ctx) {
        Player player = ctx.player();
        ItemStack stack = player.getItemInHand(message.hand());
        if (!stack.isEmpty()) {
            message.applySettings(player, stack);
        } else {
            Log.warning("Received PacketUpdateMicromissileSettings but player does not hold a Micromissile? " + player.getName());
        }
    }

    private void applySettings(Player player, ItemStack stack) {
        stack.set(ModDataComponents.MICROMISSILE_SETTINGS, settings);

        if (saveDefault) {
            MicromissileDefaults.INSTANCE.setDefaults(player, settings);
            MicromissileDefaults.INSTANCE.tryWriteToFile();
            player.level().playSound(null, player.blockPosition(), ModSounds.CHIRP.get(), SoundSource.PLAYERS, 1f, 1f);
        }
    }
}
