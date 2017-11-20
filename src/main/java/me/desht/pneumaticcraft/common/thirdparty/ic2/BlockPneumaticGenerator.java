package me.desht.pneumaticcraft.common.thirdparty.ic2;

import me.desht.pneumaticcraft.common.block.BlockPneumaticCraftModeled;
import me.desht.pneumaticcraft.proxy.CommonProxy.EnumGuiId;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;

public class BlockPneumaticGenerator extends BlockPneumaticCraftModeled {
    protected BlockPneumaticGenerator() {
        super(Material.IRON, "pneumatic_generator");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPneumaticGenerator.class;
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.PNEUMATIC_GENERATOR;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom() {
        return false;
    }
}
