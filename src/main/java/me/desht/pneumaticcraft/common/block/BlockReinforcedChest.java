package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.api.item.IInventoryItem;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.tileentity.TileEntityReinforcedChest;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;

import java.util.List;

public class BlockReinforcedChest extends BlockPneumaticCraft {
    private static final VoxelShape SHAPE = makeCuboidShape(1, 0, 1, 15, 15, 15);

    public BlockReinforcedChest() {
        super(ModBlocks.reinforcedStoneProps());
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntityReinforcedChest.class;
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
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    public static class ItemBlockReinforcedChest extends BlockItem implements IInventoryItem {
        public ItemBlockReinforcedChest(BlockReinforcedChest block) {
            super(block, ModItems.defaultProps());
        }

        @Override
        public void getStacksInItem(ItemStack stack, List<ItemStack> curStacks) {
            IInventoryItem.getStacks(stack, curStacks);
        }

        @Override
        public String getTooltipPrefix(ItemStack stack) {
            return TextFormatting.GREEN.toString();
        }

    }
}
