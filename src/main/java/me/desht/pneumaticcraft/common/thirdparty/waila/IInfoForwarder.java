package me.desht.pneumaticcraft.common.thirdparty.waila;

import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

/**
 * A TileEntity implementing this interface will forward an info display mod (Waila/TOP) to a different TileEntity
 * @author Maarten
 *
 */
public interface IInfoForwarder{
    @Nullable TileEntity getInfoTileEntity();
}
