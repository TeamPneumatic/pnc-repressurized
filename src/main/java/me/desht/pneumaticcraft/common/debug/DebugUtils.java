package me.desht.pneumaticcraft.common.debug;

import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.nbt.NBTTagCompound;

public class DebugUtils {
    public static void printNBT(NBTTagCompound tag) {
        Log.info(tag.toString());
    }
}
