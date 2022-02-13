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

import me.desht.pneumaticcraft.common.block.entity.IGUITextFieldSensitive;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: SERVER
 * Sent by client GUI's to update a IGUITextFieldSensitive tile entity server-side
 */
public class PacketUpdateTextfield {
    private final int textFieldID;
    private final String text;

    public PacketUpdateTextfield(BlockEntity te, int textfieldID) {
        textFieldID = textfieldID;
        text = ((IGUITextFieldSensitive) te).getText(textfieldID);
    }

    public PacketUpdateTextfield(FriendlyByteBuf buffer) {
        textFieldID = buffer.readInt();
        text = buffer.readUtf(32767);
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(textFieldID);
        buffer.writeUtf(text);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PacketUtil.getTE(ctx.get().getSender(), BlockEntity.class).ifPresent(te -> {
                if (te instanceof IGUITextFieldSensitive) {
                    ((IGUITextFieldSensitive) te).setText(textFieldID, text);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
