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

import me.desht.pneumaticcraft.common.recipes.PneumaticCraftRecipeType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static me.desht.pneumaticcraft.api.PneumaticRegistry.RL;

/**
 * Received on: CLIENT
 * Sent by server when recipes are reloaded; clear our local cache of machine recipes
 */
public enum PacketClearRecipeCache implements CustomPacketPayload {
    INSTANCE;

    public static final Type<PacketClearRecipeCache> TYPE = new Type<>(RL("clear_recipe_cache"));

    public static final StreamCodec<FriendlyByteBuf, PacketClearRecipeCache> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<PacketClearRecipeCache> type() {
        return TYPE;
    }

    @SuppressWarnings("unused")
    public static void handle(PacketClearRecipeCache message, IPayloadContext ctx) {
        PneumaticCraftRecipeType.clearCachedRecipes();
    }
}
