package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.GuiHandler.EnumGuiId;
import me.desht.pneumaticcraft.common.tileentity.TileEntityElevatorBase;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;

public class BlockElevatorBase extends BlockPneumaticCraftCamo {

    public BlockElevatorBase() {
        super(Material.IRON, "elevator_base");
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        TileEntityElevatorBase elevatorBase = getCoreTileEntity(world, pos);
        if (elevatorBase != null) {
            elevatorBase.updateMaxElevatorHeight();
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this,
                new IProperty[] { BlockPneumaticCraft.NORTH, BlockPneumaticCraft.SOUTH, BlockPneumaticCraft.WEST, BlockPneumaticCraft.EAST },
                EXTENDED_PROPS);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        state = super.getActualState(state, worldIn, pos);
        TileEntityElevatorBase te = (TileEntityElevatorBase) PneumaticCraftUtils.getTileEntitySafely(worldIn, pos);
        for (int i = 2; i < 6; i++) {  // 2..5 = horizontal directions
            state = state.withProperty(BlockPneumaticCraft.CONNECTION_PROPERTIES[i], te.sidesConnected[i]);
        }
        return state;
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityElevatorBase.class;
    }

    @Override
    public EnumGuiId getGuiID() {
        return EnumGuiId.ELEVATOR;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float par7, float par8, float par9) {
        return super.onBlockActivated(world, getCoreElevatorPos(world, pos), state, player, hand, side, par7, par8, par9);
    }

    public static BlockPos getCoreElevatorPos(World world, BlockPos pos) {
        if (world.getBlockState(pos.offset(EnumFacing.UP)).getBlock() == Blockss.ELEVATOR_BASE) {
            return getCoreElevatorPos(world, pos.offset(EnumFacing.UP));
        } else {
            return pos;
        }
    }

    public static TileEntityElevatorBase getCoreTileEntity(World world, BlockPos pos) {
        return (TileEntityElevatorBase) world.getTileEntity(getCoreElevatorPos(world, pos));
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (world.getBlockState(pos.offset(EnumFacing.DOWN)).getBlock() == Blockss.ELEVATOR_BASE) {
            TileEntity te = world.getTileEntity(pos.offset(EnumFacing.DOWN));
            ((TileEntityElevatorBase) te).moveInventoryToThis();
        }
        TileEntityElevatorBase elevatorBase = getCoreTileEntity(world, pos);
        if (elevatorBase != null) {
            elevatorBase.updateMaxElevatorHeight();
        }
        super.breakBlock(world, pos, state);
    }
}
