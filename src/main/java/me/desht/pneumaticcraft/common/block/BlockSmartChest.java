package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.api.item.IInventoryItem;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.tileentity.TileEntitySmartChest;
import me.desht.pneumaticcraft.lib.NBTKeys;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.xlate;

public class BlockSmartChest extends BlockPneumaticCraft {
    private static final VoxelShape SHAPE = makeCuboidShape(1, 0, 1, 15, 15, 15);

    public BlockSmartChest() {
        super(ModBlocks.reinforcedStoneProps());
    }

    @Override
    protected Class<? extends TileEntity> getTileEntityClass() {
        return TileEntitySmartChest.class;
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

    public static class ItemBlockBlockSmartChest extends BlockItem implements IInventoryItem {
        public ItemBlockBlockSmartChest(Block block) {
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

        @Override
        public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
            super.addInformation(stack, worldIn, tooltip, flagIn);

            CompoundNBT tag = stack.getChildTag(NBTKeys.BLOCK_ENTITY_TAG);
            if (tag != null && tag.contains("Items")) {
                CompoundNBT subTag = tag.getCompound("Items");
                ListNBT l = subTag.getList("Filter", Constants.NBT.TAG_COMPOUND);
                if (!l.isEmpty()) {
                    tooltip.add(xlate("gui.tooltip.smartChest.filter", l.size()));
                }
                int lastSlot = subTag.getInt("LastSlot");
                if (lastSlot < TileEntitySmartChest.CHEST_SIZE) {
                    tooltip.add(xlate("gui.tooltip.smartChest.slotsClosed", TileEntitySmartChest.CHEST_SIZE - lastSlot));
                }
            }
        }
    }
}
