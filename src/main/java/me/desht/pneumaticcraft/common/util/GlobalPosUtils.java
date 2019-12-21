package me.desht.pneumaticcraft.common.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class GlobalPosUtils {
    public static CompoundNBT serializeGlobalPos(GlobalPos globalPos) {
        CompoundNBT tag = new CompoundNBT();
        tag.put("pos", net.minecraft.nbt.NBTUtil.writeBlockPos(globalPos.getPos()));
        tag.putString("dim", DimensionType.getKey(globalPos.getDimension()).toString());
        return tag;
    }

    public static GlobalPos deserializeGlobalPos(CompoundNBT tag) {
        return GlobalPos.of(
                DimensionType.byName(new ResourceLocation(tag.getString("dim"))),
                NBTUtil.readBlockPos(tag.getCompound("pos"))
        );
    }

    /**
     * Get a world object for the given global pos.  This will not reset the dimension unload delay and will not
     * force-load the dimension.
     * @param pos the global pos
     * @return the world
     */
    public static World getWorldForGlobalPos(GlobalPos pos) {
        return DimensionManager.getWorld(ServerLifecycleHooks.getCurrentServer(), pos.getDimension(), false, false);
    }

    public static String prettyPrint(GlobalPos pos) {
        BlockPos p = pos.getPos();
        return String.format("%s [%d,%d,%d]", pos.getDimension().toString(), p.getX(), p.getY(), p.getZ());
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
