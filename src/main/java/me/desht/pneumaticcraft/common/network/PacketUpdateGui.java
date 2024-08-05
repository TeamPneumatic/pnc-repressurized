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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * The primary mechanism for sync'ing BE fields to an open GUI.  BE fields annotated with @GuiSynced will be synced
 * in this packet, via {@link AbstractPneumaticCraftMenu#broadcastChanges()}.
 */
public record PacketUpdateGui(int syncId, Object fieldValue, SyncedField.FieldType fieldType) implements CustomPacketPayload {
    public static final Type<PacketUpdateGui> TYPE = new Type<>(RL("update_gui"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketUpdateGui> STREAM_CODEC = StreamCodec.of(
            PacketUpdateGui::write, PacketUpdateGui::read
    );

    public static PacketUpdateGui create(int syncId, SyncedField<?> syncField) {
        return new PacketUpdateGui(syncId, syncField.getValue(), syncField.getFieldType());
    }

    private static PacketUpdateGui read(RegistryFriendlyByteBuf buf) {
        int syncId = buf.readVarInt();
        SyncedField.FieldType type = buf.readEnum(SyncedField.FieldType.class);
        Object value = SyncedField.fromBytes(buf, type);

        return new PacketUpdateGui(syncId, value, type);
    }

    private static void write(RegistryFriendlyByteBuf buf, PacketUpdateGui packet) {
        buf.writeVarInt(packet.syncId);
        buf.writeEnum(packet.fieldType);
        SyncedField.toBytes(buf, packet.fieldValue, packet.fieldType);
    }

    @Override
    public Type<PacketUpdateGui> type() {
        return TYPE;
    }

    public static void handle(PacketUpdateGui message, IPayloadContext ctx) {
        ClientUtils.syncViaOpenContainerScreen(message.syncId(), message.fieldValue());
    }
}
