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

import com.mojang.datafixers.util.Either;
import me.desht.pneumaticcraft.client.gui.RemoteScreen;
import me.desht.pneumaticcraft.client.render.area.AreaRenderManager;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Received on: BOTH
 * Sync's global variable data between server and client
 */
public class PacketSetGlobalVariable {
    private final String varName;
    private final Either<BlockPos, ItemStack> value;

    public PacketSetGlobalVariable(String varName, BlockPos value) {
        if (!GlobalVariableHelper.hasPrefix(varName)) varName = "#" + varName;
        this.value = Either.left(value);
        this.varName = varName.startsWith("#") ? varName.substring(1) : varName;
    }

    public PacketSetGlobalVariable(String varName, @Nonnull ItemStack stack) {
        if (!GlobalVariableHelper.hasPrefix(varName)) varName = "#" + varName;
        this.value = Either.right(stack);
        this.varName = varName;
    }

    public PacketSetGlobalVariable(String varName, int value) {
        this(varName, new BlockPos(value, 0, 0));
    }

    public PacketSetGlobalVariable(String varName, boolean value) {
        this(varName, value ? 1 : 0);
    }

    public PacketSetGlobalVariable(FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            this.value = Either.left(buf.readBoolean() ? buf.readBlockPos() : null);
        } else {
            this.value = Either.right(buf.readItem());
        }
        this.varName = buf.readUtf(GlobalVariableManager.MAX_VARIABLE_LEN);
    }

    public void toBytes(FriendlyByteBuf buf) {
        value.ifLeft(pos -> {
            buf.writeBoolean(true);
            buf.writeBoolean(pos != null);
            if (pos != null) buf.writeBlockPos(pos);
        }).ifRight(stack -> {
            buf.writeBoolean(false);
            buf.writeItemStack(stack, true);
        });
        buf.writeUtf(varName);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player p = ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT ? ClientUtils.getClientPlayer() : Objects.requireNonNull(ctx.get().getSender());
            value.ifLeft(pos -> GlobalVariableHelper.setPos(p.getUUID(), varName, pos))
                    .ifRight(stack -> GlobalVariableHelper.setStack(p.getUUID(), varName, stack));
            if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                RemoteScreen.maybeHandleVariableChange(varName);
                AreaRenderManager.getInstance().clearPosProviderCache();
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
