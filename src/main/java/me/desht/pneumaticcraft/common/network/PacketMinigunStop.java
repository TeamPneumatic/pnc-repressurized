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

import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.core.ModSounds;
import me.desht.pneumaticcraft.common.item.ItemMinigun;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 * Sent by server when player switches away from a held minigun which is active (i.e. spinning)
 */
public class PacketMinigunStop {
    private final ItemStack stack;

    public PacketMinigunStop(ItemStack stack) {
        this.stack = stack;
    }

    public PacketMinigunStop(FriendlyByteBuf buf) {
        this.stack = buf.readItem();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeItem(stack);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = ClientUtils.getClientPlayer();
            Minigun minigun = ((ItemMinigun) stack.getItem()).getMinigun(stack, player);
            minigun.setMinigunSpeed(0);
            minigun.setMinigunActivated(false);
            minigun.setMinigunTriggerTimeOut(0);
            player.playSound(ModSounds.MINIGUN_STOP.get(), 1f, 1f);
        });
        ctx.get().setPacketHandled(true);
    }
}
