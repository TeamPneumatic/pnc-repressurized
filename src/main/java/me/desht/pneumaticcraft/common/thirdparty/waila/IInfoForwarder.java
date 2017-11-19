package me.desht.pneumaticcraft.common.thirdparty.waila;

import javax.annotation.Nullable;

import net.minecraft.tileentity.TileEntity;

/**
 * A TileEntity implementing this interface will forward an info display mod (Waila/TOP) to a different TileEntity
 * @author Maarten
 *
 */
public interface IInfoForwarder{
    @Nullable TileEntity getInfoTileEntity();
}
