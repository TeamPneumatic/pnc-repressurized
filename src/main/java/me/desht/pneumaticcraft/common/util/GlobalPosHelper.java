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
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.server.ServerLifecycleHooks;

public class GlobalPosHelper {
    // TODO 1.19 use GlobalPos.CODEC & DynamicOps for NBT & Json serialization

    public static CompoundTag toNBT(GlobalPos globalPos) {
        CompoundTag tag = new CompoundTag();
        tag.put("pos", net.minecraft.nbt.NbtUtils.writeBlockPos(globalPos.pos()));
        tag.putString("dim", globalPos.dimension().location().toString());
        return tag;
    }

    public static GlobalPos fromNBT(CompoundTag tag) {
        ResourceKey<Level> worldKey = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString("dim")));
        return GlobalPos.of(worldKey, NbtUtils.readBlockPos(tag.getCompound("pos")));
    }

    public static JsonElement toJson(GlobalPos pos) {
        JsonObject posObj = new JsonObject();
        posObj.addProperty("x", pos.pos().getX());
        posObj.addProperty("y", pos.pos().getY());
        posObj.addProperty("z", pos.pos().getZ());

        JsonObject obj = new JsonObject();
        obj.addProperty("dimension", pos.dimension().location().toString());
        obj.add("pos", posObj);
        return obj;
    }

    public static GlobalPos fromJson(JsonObject json) {
        ResourceKey<Level> worldKey = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(GsonHelper.getAsString(json, "dimension")));
        JsonObject posObj = json.get("pos").getAsJsonObject();
        BlockPos pos = new BlockPos(
                GsonHelper.getAsInt(posObj, "x"),
                GsonHelper.getAsInt(posObj, "y"),
                GsonHelper.getAsInt(posObj, "z")
        );
        return GlobalPos.of(worldKey, pos);
    }

    public static ServerLevel getWorldForGlobalPos(GlobalPos pos) {
        return ServerLifecycleHooks.getCurrentServer().getLevel(pos.dimension());
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
