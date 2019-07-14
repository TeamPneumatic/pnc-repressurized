package me.desht.pneumaticcraft.common.debug;

import me.desht.pneumaticcraft.lib.Log;
import net.minecraft.nbt.CompoundNBT;

public class DebugUtils {
    public static void printNBT(CompoundNBT tag) {
        Log.info(tag.toString());
    }
}
