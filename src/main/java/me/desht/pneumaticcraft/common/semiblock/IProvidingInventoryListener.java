package me.desht.pneumaticcraft.common.semiblock;

import net.minecraft.tileentity.TileEntity;

public interface IProvidingInventoryListener {
    void notify(TileEntity te);
}
