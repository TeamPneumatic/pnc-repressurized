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

import me.desht.pneumaticcraft.client.gui.GuiPneumaticContainerBase;
import me.desht.pneumaticcraft.common.inventory.ContainerPneumaticBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: CLIENT
 *
 * The primary mechanism for sync'ing TE fields to an open GUI.  TE fields annotated with @GuiSynced will be synced
 * in this packet, via {@link ContainerPneumaticBase#broadcastChanges()}.
 */
public class PacketUpdateGui {
    private final int syncId;
    private final Object value;
    private final byte type;

    public PacketUpdateGui(int syncId, SyncedField<?> syncField) {
        this.syncId = syncId;
        value = syncField.getValue();
        type = SyncedField.getType(syncField);
    }

    public PacketUpdateGui(PacketBuffer buf) {
        syncId = buf.readVarInt();
        type = buf.readByte();
        value = SyncedField.fromBytes(buf, type);
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeVarInt(syncId);
        buf.writeByte(type);
        SyncedField.toBytes(buf, value, type);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof ContainerScreen) {
                Container container = ((ContainerScreen<?>) Minecraft.getInstance().screen).getMenu();
                if (container instanceof ContainerPneumaticBase) {
                    ((ContainerPneumaticBase<?>) container).updateField(syncId, value);
                }
                if (Minecraft.getInstance().screen instanceof GuiPneumaticContainerBase) {
                    ((GuiPneumaticContainerBase<?,?>) Minecraft.getInstance().screen).onGuiUpdate();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
