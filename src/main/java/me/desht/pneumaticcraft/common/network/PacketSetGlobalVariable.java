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
import me.desht.pneumaticcraft.common.variables.GlobalVariableHelper;
import me.desht.pneumaticcraft.common.variables.GlobalVariableManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: BOTH
 * Sync's global variable data between server and client
 */
public record PacketSetGlobalVariable(String varName, Either<BlockPos, ItemStack> value) implements CustomPacketPayload {
    public static final ResourceLocation ID = RL("set_global_variable");

    public static PacketSetGlobalVariable forPos(String varName, @Nullable BlockPos value) {
        if (!GlobalVariableHelper.hasPrefix(varName)) varName = "#" + varName;
        varName = varName.startsWith("#") ? varName.substring(1) : varName;

        return new PacketSetGlobalVariable(varName, Either.left(value));
    }

    public static PacketSetGlobalVariable forItem(String varName, @Nonnull ItemStack stack) {
        if (!GlobalVariableHelper.hasPrefix(varName)) varName = "#" + varName;

        return new PacketSetGlobalVariable(varName, Either.right(stack));
    }

    public static PacketSetGlobalVariable forInt(String varName, int value) {
        return forPos(varName, new BlockPos(value, 0, 0));
    }

    public static PacketSetGlobalVariable forBool(String varName, boolean value) {
        return forInt(varName, value ? 1 : 0);
    }

    public static PacketSetGlobalVariable fromNetwork(FriendlyByteBuf buf) {
        String varName = buf.readUtf(GlobalVariableManager.MAX_VARIABLE_LEN);
        if (buf.readBoolean()) {
            return new PacketSetGlobalVariable(varName, Either.left(buf.readNullable(FriendlyByteBuf::readBlockPos)));
        } else {
            return new PacketSetGlobalVariable(varName, Either.right(buf.readItem()));
        }
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        value.ifLeft(pos -> {
            buf.writeBoolean(true);
            buf.writeNullable(pos, FriendlyByteBuf::writeBlockPos);
        }).ifRight(stack -> {
            buf.writeBoolean(false);
            buf.writeItem(stack);
        });
        buf.writeUtf(varName);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(PacketSetGlobalVariable message, PlayPayloadContext ctx) {
        ctx.player().ifPresent(player -> ctx.workHandler().submitAsync(() -> {
            message.value()
                    .ifLeft(pos -> GlobalVariableHelper.setPos(player.getUUID(), message.varName(), pos))
                    .ifRight(stack -> GlobalVariableHelper.setStack(player.getUUID(), message.varName(), stack));
            if (ctx.flow().isClientbound()) {
                RemoteScreen.maybeHandleVariableChange(message.varName());
                AreaRenderManager.getInstance().clearPosProviderCache();
            }
        }));
    }
}
