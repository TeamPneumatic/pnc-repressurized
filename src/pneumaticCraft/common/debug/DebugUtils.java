package pneumaticCraft.common.debug;

import net.minecraft.nbt.NBTTagCompound;
import pneumaticCraft.lib.Log;

public class DebugUtils{
    public static void printNBT(NBTTagCompound tag){
        Log.info(tag.toString());
    }
}
