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
import me.desht.pneumaticcraft.common.inventory.AbstractPneumaticCraftMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * The primary mechanism for sync'ing BE fields to an open GUI.  BE fields annotated with @GuiSynced will be synced
 * in this packet, via {@link AbstractPneumaticCraftMenu#broadcastChanges()}.
 */
public record PacketUpdateGui(int syncId, Object value, byte type) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("update_gui");

    public static PacketUpdateGui create(int syncId, SyncedField<?> syncField) {
        return new PacketUpdateGui(syncId, syncField.getValue(), SyncedField.getType(syncField));
    }

    public static PacketUpdateGui fromNetwork(FriendlyByteBuf buf) {
        int syncId = buf.readVarInt();
        byte type = buf.readByte();
        Object value = SyncedField.fromBytes(buf, type);

        return new PacketUpdateGui(syncId, value, type);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(syncId);
        buf.writeByte(type);
        SyncedField.toBytes(buf, value, type);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketUpdateGui message, PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> ClientUtils.syncViaOpenContainerScreen(message.syncId(), message.value()));
    }
}
