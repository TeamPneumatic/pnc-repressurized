package me.desht.pneumaticcraft.common.thirdparty.mcmultipart;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipartTile;
import net.minecraft.tileentity.TileEntity;

public class PartPressureTubeTile implements IMultipartTile {
    private final TileEntity tile;
    private IPartInfo info;

    PartPressureTubeTile(TileEntity tile) {
        this.tile = tile;
    }

    @Override
    public void setPartInfo(IPartInfo info) {
        this.info = info;
    }

    @Override
    public TileEntity getTileEntity() {
        return tile;
    }

    public IPartInfo getInfo() {
        return info;
    }

}
