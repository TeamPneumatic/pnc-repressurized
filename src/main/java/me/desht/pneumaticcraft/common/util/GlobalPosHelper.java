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
        tag.put("pos", net.minecraft.nbt.NBTUtil.writeBlockPos(globalPos.getPos()));
        tag.putString("dim", globalPos.func_239646_a_().func_240901_a_().toString());
        return tag;
    }

    public static GlobalPos fromNBT(CompoundNBT tag) {
        RegistryKey<World> worldKey = RegistryKey.func_240903_a_(Registry.WORLD_KEY, new ResourceLocation(tag.getString("dim")));
        return GlobalPos.func_239648_a_(worldKey, NBTUtil.readBlockPos(tag.getCompound("pos")));
    }

    public static JsonElement toJson(GlobalPos pos) {
        JsonObject posObj = new JsonObject();
        posObj.addProperty("x", pos.getPos().getX());
        posObj.addProperty("y", pos.getPos().getY());
        posObj.addProperty("z", pos.getPos().getZ());

        JsonObject obj = new JsonObject();
        obj.addProperty("dimension", pos.func_239646_a_().func_240901_a_().toString());
        obj.add("pos", posObj);
        return obj;
    }

    public static GlobalPos fromJson(JsonObject json) {
        RegistryKey<World> worldKey = RegistryKey.func_240903_a_(Registry.WORLD_KEY, new ResourceLocation(JSONUtils.getString(json, "dimension")));
        JsonObject posObj = json.get("pos").getAsJsonObject();
        BlockPos pos = new BlockPos(
                JSONUtils.getInt(posObj, "x"),
                JSONUtils.getInt(posObj, "y"),
                JSONUtils.getInt(posObj, "z")
        );
        return GlobalPos.func_239648_a_(worldKey, pos);
    }

    public static ServerWorld getWorldForGlobalPos(GlobalPos pos) {
        return ServerLifecycleHooks.getCurrentServer().getWorld(pos.func_239646_a_());
    }

    public static GlobalPos makeGlobalPos(World w, BlockPos pos) {
        return GlobalPos.func_239648_a_(w.func_234923_W_(), pos);
    }

    public static boolean isSameWorld(GlobalPos pos, World world) {
        return pos.func_239646_a_().compareTo(world.func_234923_W_()) == 0;
    }

    public static String prettyPrint(GlobalPos pos) {
        BlockPos p = pos.getPos();
        String dim = pos.func_239646_a_().func_240901_a_().toString();
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
        if (world != null && world.isAreaLoaded(globalPos.getPos(), 1)) {
            return world.getTileEntity(globalPos.getPos());
        }
        return null;
    }
}
