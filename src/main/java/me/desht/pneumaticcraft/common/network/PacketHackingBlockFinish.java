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

import com.jcraft.jorbis.Block;
import me.desht.pneumaticcraft.api.pneumatic_armor.hacking.IHackableBlock;
import me.desht.pneumaticcraft.common.hacking.HackManager;
import me.desht.pneumaticcraft.common.hacking.HackTickTracker;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonUpgradeHandlers;
import me.desht.pneumaticcraft.common.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server when a block hack has completed.
 */
public record PacketHackingBlockFinish(BlockPos pos) implements CustomPacketPayload {
    public static final Type<PacketHackingBlockFinish> TYPE = new Type<>(RL("hack_block_finish"));

    public static final StreamCodec<FriendlyByteBuf, PacketHackingBlockFinish> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PacketHackingBlockFinish::pos,
            PacketHackingBlockFinish::new
    );

    public static PacketHackingBlockFinish create(BlockPos gPos) {
        return new PacketHackingBlockFinish(gPos);
    }

    @Override
    public Type<PacketHackingBlockFinish> type() {
        return TYPE;
    }

    public static void handle(PacketHackingBlockFinish message, IPayloadContext ctx) {
        Player player = ctx.player();
        IHackableBlock hackableBlock = HackManager.getHackableForBlock(player.level(), message.pos(), player);
            if (hackableBlock != null) {
                hackableBlock.onHackComplete(player.level(), message.pos(), player);
                HackTickTracker.getInstance(player.level()).trackBlock(message.pos(), hackableBlock);
                CommonArmorHandler.getHandlerForPlayer(player).getExtensionData(CommonUpgradeHandlers.hackHandler).setHackedBlockPos(null);
                player.playSound(ModSounds.HELMET_HACK_FINISH.get(), 1.0F, 1.0F);
            }
    }
}
