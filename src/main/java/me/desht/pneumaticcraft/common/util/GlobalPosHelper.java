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

package me.desht.pneumaticcraft.common.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class GlobalPosHelper {
    public static Tag toNBT(GlobalPos globalPos) {
        return GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, globalPos).result().orElseThrow();
    }

    public static GlobalPos fromNBT(CompoundTag tag) {
        return GlobalPos.CODEC.parse(NbtOps.INSTANCE, tag).result().orElseThrow();
    }

    public static JsonElement toJson(GlobalPos pos) {
        return GlobalPos.CODEC.encodeStart(JsonOps.INSTANCE, pos).result().orElseThrow();
    }

    public static GlobalPos fromJson(JsonObject json) {
        return GlobalPos.CODEC.parse(JsonOps.INSTANCE, json).result().orElseThrow();
    }

    public static ServerLevel getWorldForGlobalPos(GlobalPos pos) {
        return ServerLifecycleHooks.getCurrentServer().getLevel(pos.dimension());
    }

    public static ServerLevel getWorldForGlobalPos(MinecraftServer server, GlobalPos pos) {
        return server.getLevel(pos.dimension());
    }

    public static GlobalPos makeGlobalPos(Level w, BlockPos pos) {
        return GlobalPos.of(w.dimension(), pos);
    }

    public static boolean isSameWorld(GlobalPos pos, Level world) {
        return pos.dimension().compareTo(world.dimension()) == 0;
    }

    public static String prettyPrint(GlobalPos pos) {
        BlockPos p = pos.pos();
        String dim = pos.dimension().location().toString();
        return String.format("%s [%d,%d,%d]", dim, p.getX(), p.getY(), p.getZ());
    }

    /**
     * Get the block entity at the given global pos.  This will not force-load the dimension or chunks.
     *
     * @param globalPos the global pos
     * @return the block entity, if any
     */
    public static BlockEntity getTileEntity(GlobalPos globalPos) {
        Level world = getWorldForGlobalPos(globalPos);
        if (world != null && world.isLoaded(globalPos.pos())) {
            return world.getBlockEntity(globalPos.pos());
        }
        return null;
    }
}
