package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.thirdparty.ModdedWrenchUtils;
import me.desht.pneumaticcraft.common.tileentity.TileEntityDisplayTable;
import me.desht.pneumaticcraft.common.util.PneumaticCraftUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockDisplayTable extends BlockPneumaticCraft {
    private static final VoxelShape TOP = makeCuboidShape(0, 14, 0, 16, 16, 16);
    private static final VoxelShape LEG1 = makeCuboidShape(1, 0, 1, 3, 14, 3);
    private static final VoxelShape LEG2 = makeCuboidShape(1, 0, 13, 3, 14, 15);
    private static final VoxelShape LEG3 = makeCuboidShape(13, 0, 1, 15, 14, 3);
    private static final VoxelShape LEG4 = makeCuboidShape(13, 0, 13, 15, 14, 15);
    private static final VoxelShape SHAPE = VoxelShapes.or(TOP, LEG1, LEG2, LEG3, LEG4).simplify();

    public BlockDisplayTable() {
        super(ModBlocks.defaultProps());
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityDisplayTable.class;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public boolean isRotatable() {
        return true;
    }

    @Override
    protected boolean reversePlacementRotation() {
        return true;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult brtr) {
        TileEntity te = world.getTileEntity(pos);
        ItemStack heldStack = player.getHeldItem(hand);
        if (player.isSneaking() || te instanceof INamedContainerProvider
                || heldStack.getItem() == ModItems.PNEUMATIC_WRENCH.get() || ModdedWrenchUtils.getInstance().isModdedWrench(heldStack))
        {
            return super.onBlockActivated(state, world, pos, player, hand, brtr);
        } else if (te instanceof TileEntityDisplayTable) {
            if (!world.isRemote) {
                TileEntityDisplayTable teDT = (TileEntityDisplayTable) te;
                if (teDT.getPrimaryInventory().getStackInSlot(0).isEmpty()) {
                    // try to put the player's held item onto the table
                    ItemStack excess = teDT.getPrimaryInventory().insertItem(0, player.getHeldItem(hand), false);
                    player.setHeldItem(hand, excess);
                } else {
                    // try to remove whatever is on the table
                    ItemStack stack = teDT.getPrimaryInventory().extractItem(0, 64, false);
                    PneumaticCraftUtils.dropItemOnGroundPrecisely(stack, world, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5);
                }
            }
            return true;
        }
        return false;
    }
}
