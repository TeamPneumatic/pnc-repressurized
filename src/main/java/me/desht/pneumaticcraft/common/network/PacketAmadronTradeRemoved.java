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
import me.desht.pneumaticcraft.common.config.ConfigHelper;
import me.desht.pneumaticcraft.common.recipes.amadron.AmadronPlayerOffer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class PacketAmadronTradeRemoved extends PacketAbstractAmadronTrade {
    public PacketAmadronTradeRemoved(AmadronPlayerOffer offer) {
        super(offer);
    }

    public PacketAmadronTradeRemoved(PacketBuffer buffer) {
        super(buffer);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ConfigHelper.common().amadron.notifyOfTradeRemoval.get())
                ClientUtils.getClientPlayer().displayClientMessage(
                        xlate("pneumaticcraft.message.amadron.playerRemovedTrade",
                                getOffer().getVendorName(),
                                getOffer().getOutput().toString(),
                                getOffer().getInput().toString()
                        ), false);
        });
        ctx.get().setPacketHandled(true);
    }
}
