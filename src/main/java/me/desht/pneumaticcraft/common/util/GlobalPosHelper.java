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
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class GlobalPosHelper {
    public static CompoundNBT toNBT(GlobalPos globalPos) {
        CompoundNBT tag = new CompoundNBT();
        tag.put("pos", net.minecraft.nbt.NBTUtil.writeBlockPos(globalPos.pos()));
        tag.putString("dim", globalPos.dimension().location().toString());
        return tag;
    }

    public static GlobalPos fromNBT(CompoundNBT tag) {
        RegistryKey<World> worldKey = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(tag.getString("dim")));
        return GlobalPos.of(worldKey, NBTUtil.readBlockPos(tag.getCompound("pos")));
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
        RegistryKey<World> worldKey = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(JSONUtils.getAsString(json, "dimension")));
        JsonObject posObj = json.get("pos").getAsJsonObject();
        BlockPos pos = new BlockPos(
                JSONUtils.getAsInt(posObj, "x"),
                JSONUtils.getAsInt(posObj, "y"),
                JSONUtils.getAsInt(posObj, "z")
        );
        return GlobalPos.of(worldKey, pos);
    }

    public static ServerWorld getWorldForGlobalPos(GlobalPos pos) {
        return ServerLifecycleHooks.getCurrentServer().getLevel(pos.dimension());
    }

    public static GlobalPos makeGlobalPos(World w, BlockPos pos) {
        return GlobalPos.of(w.dimension(), pos);
    }

    public static boolean isSameWorld(GlobalPos pos, World world) {
        return pos.dimension().compareTo(world.dimension()) == 0;
    }

    public static String prettyPrint(GlobalPos pos) {
        BlockPos p = pos.pos();
        String dim = pos.dimension().location().toString();
        return String.format("%s [%d,%d,%d]", dim, p.getX(), p.getY(), p.getZ());
    }

    /**
     * Get the tile entity at the given global pos.  This will not force-load the dimension or chunks.
     *
     * @param globalPos the global pos
     * @return the tile entity, if any
     */
    public static TileEntity getTileEntity(GlobalPos globalPos) {
        World world = getWorldForGlobalPos(globalPos);
        if (world != null && world.isAreaLoaded(globalPos.pos(), 1)) {
            return world.getBlockEntity(globalPos.pos());
        }
        return null;
    }
}
