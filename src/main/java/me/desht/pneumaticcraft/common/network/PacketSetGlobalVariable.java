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
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: BOTH
 * Sync's global variable data between server and client
 */
public record PacketSetGlobalVariable(String varName, Either<BlockPos, ItemStack> value) implements CustomPacketPayload {
    public static final Type<PacketSetGlobalVariable> TYPE = new Type<>(RL("set_global_variable"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSetGlobalVariable> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PacketSetGlobalVariable::varName,
            ByteBufCodecs.either(BlockPos.STREAM_CODEC, ItemStack.OPTIONAL_STREAM_CODEC), PacketSetGlobalVariable::value,
            PacketSetGlobalVariable::new
    );

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

    @Override
    public Type<PacketSetGlobalVariable> type() {
        return TYPE;
    }

    public static void handle(PacketSetGlobalVariable message, IPayloadContext ctx) {
        Player player = ctx.player();

        message.value()
                .ifLeft(pos -> GlobalVariableHelper.setPos(player.getUUID(), message.varName(), pos))
                .ifRight(stack -> GlobalVariableHelper.setStack(player.getUUID(), message.varName(), stack));

        if (ctx.flow().isClientbound()) {
            RemoteScreen.handleVariableChangeIfOpen(message.varName());
            AreaRenderManager.getInstance().clearPosProviderCache();
        }
    }
}
