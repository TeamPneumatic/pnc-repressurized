package pneumaticCraft.api.tileentity;

import net.minecraft.entity.player.EntityPlayer;

public interface IManoMeasurable{
    /**
     * This method is invoked by the Manometer when a player right-clicks a TE or Entity with this interface implemented.
     * @param player that rightclicks the measurable TE, and therefore needs to get the message
     * @return true when information succesfully has printed. return false to don't use air of the Manometer.
     */
    public boolean printManometerMessage(EntityPlayer player);
}
