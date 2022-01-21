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

import me.desht.pneumaticcraft.api.client.pneumatic_helmet.IHackableBlock;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.event.HackTickHandler;
import me.desht.pneumaticcraft.common.hacking.HackManager;
import me.desht.pneumaticcraft.common.hacking.WorldAndCoord;
import me.desht.pneumaticcraft.common.pneumatic_armor.ArmorUpgradeRegistry;
import me.desht.pneumaticcraft.common.pneumatic_armor.CommonArmorHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server when a block hack has completed.
 */
public class PacketHackingBlockFinish extends LocationIntPacket {
    public PacketHackingBlockFinish(WorldAndCoord gPos) {
        super(gPos.pos);
    }

    public PacketHackingBlockFinish(FriendlyByteBuf buffer) {
        super(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = ClientUtils.getClientPlayer();
            IHackableBlock hackableBlock = HackManager.getHackableForBlock(player.level, pos, player);
            if (hackableBlock != null) {
                hackableBlock.onHackComplete(player.level, pos, player);
                HackTickHandler.instance().trackBlock(player.level, pos, hackableBlock);
                CommonArmorHandler.getHandlerForPlayer(player).getExtensionData(ArmorUpgradeRegistry.getInstance().hackHandler).setHackedBlockPos(null);
                player.playSound(ModSounds.HELMET_HACK_FINISH.get(), 1.0F, 1.0F);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
