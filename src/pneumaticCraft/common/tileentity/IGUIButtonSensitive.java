package pneumaticCraft.common.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import pneumaticCraft.common.network.IDescSynced;

public interface IGUIButtonSensitive{
    public void handleGUIButtonPress(int guiID, EntityPlayer player);

    public IDescSynced.Type getSyncType();

    public int getX();

    public int getY();

    public int getZ();
}