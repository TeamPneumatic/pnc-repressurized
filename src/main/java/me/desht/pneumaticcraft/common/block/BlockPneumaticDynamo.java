package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.tileentity.TileEntityPneumaticDynamo;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockPneumaticDynamo extends BlockPneumaticCraftModeled {
    private static final PropertyBool ACTIVE = PropertyBool.create("active");

    public BlockPneumaticDynamo() {
        super(Material.IRON, "pneumatic_dynamo");
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityPneumaticDynamo.class;
    }

    @Override
    public EnumGuiId getGuiID(){
        return EnumGuiId.PNEUMATIC_DYNAMO;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ROTATION, ACTIVE);
    }

    @Override
    public BlockState getActualState(BlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntity te = PneumaticCraftUtils.getTileEntitySafely(worldIn, pos);
        if (te instanceof TileEntityPneumaticDynamo) {
            return state.withProperty(ACTIVE, ((TileEntityPneumaticDynamo) te).isEnabled);
        }
        return state;
    }

    @Override
    public boolean isRotatable(){
        return true;
    }

    @Override
    protected boolean canRotateToTopOrBottom(){
        return true;
    }
}
