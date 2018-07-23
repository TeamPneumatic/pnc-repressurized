package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.itemBlock.ItemBlockLiquidHopper;
import me.desht.pneumaticcraft.common.tileentity.TileEntityLiquidHopper;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockLiquidHopper extends BlockOmnidirectionalHopper implements ICustomItemBlock {

    BlockLiquidHopper() {
        super("liquid_hopper");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityLiquidHopper.class;
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.LIQUID_HOPPER;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public ItemBlock getCustomItemBlock() {
        return new ItemBlockLiquidHopper(this);
    }
}
