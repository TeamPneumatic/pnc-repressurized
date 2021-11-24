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

import me.desht.pneumaticcraft.client.gui.GuiRemote;
import me.desht.pneumaticcraft.client.render.area.AreaRenderManager;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Received on: BOTH
 * Sync's global variable data between server and client
 */
public class PacketSetGlobalVariable extends LocationIntPacket {
    private final String varName;

    public PacketSetGlobalVariable(String varName, BlockPos value) {
        super(value);
        this.varName = varName.startsWith("#") ? varName.substring(1) : varName;
    }

    public PacketSetGlobalVariable(String varName, int value) {
        this(varName, new BlockPos(value, 0, 0));
    }

    public PacketSetGlobalVariable(String varName, boolean value) {
        this(varName, value ? 1 : 0);
    }

    public PacketSetGlobalVariable(PacketBuffer buf) {
        super(buf);
        this.varName = buf.readUtf(32767);
    }

    public void toBytes(PacketBuffer buf) {
        super.toBytes(buf);
        buf.writeUtf(varName);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            GlobalVariableManager.getInstance().set(varName, pos);
            if (ctx.get().getSender() == null) {
                GuiRemote.maybeHandleVariableChange(varName);
                AreaRenderManager.getInstance().clearPosProviderCache();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
