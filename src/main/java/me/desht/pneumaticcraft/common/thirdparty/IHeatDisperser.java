package me.desht.pneumaticcraft.common.thirdparty;

import me.desht.pneumaticcraft.common.util.TileEntityCache;
import net.minecraft.tileentity.TileEntity;

public interface IHeatDisperser {
    /**
     * Disperse heat to adjacent modded machines which can accept it
     *
     * @param te the PneumaticCraft tile entity trying to disperse heat
     * @param tileCache cache of adjacent tile entities
     */
    void disperseHeat(TileEntity te, TileEntityCache[] tileCache);
}
