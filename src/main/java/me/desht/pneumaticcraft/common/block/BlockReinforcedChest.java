package me.desht.pneumaticcraft.common.block;

import me.desht.pneumaticcraft.api.item.IInventoryItem;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.core.ModItems;
import me.desht.pneumaticcraft.common.tileentity.TileEntityReinforcedChest;
import me.desht.pneumaticcraft.lib.NBTKeys;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

import static me.desht.pneumaticcraft.common.tileentity.TileEntityReinforcedChest.NBT_ITEMS;

public class BlockReinforcedChest extends BlockPneumaticCraft {
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

    public static class ItemBlockReinforcedChest extends BlockItem implements IInventoryItem {
        public ItemBlockReinforcedChest(BlockReinforcedChest block) {
            super(block, ModItems.defaultProps());
        }

        @Override
        public void getStacksInItem(ItemStack stack, List<ItemStack> curStacks) {
            CompoundNBT sub = stack.getChildTag(NBTKeys.BLOCK_ENTITY_TAG);
            if (sub != null && sub.contains(NBT_ITEMS, Constants.NBT.TAG_COMPOUND)) {
                ItemStackHandler handler = new ItemStackHandler();
                handler.deserializeNBT(sub.getCompound(NBT_ITEMS));
                for (int i = 0; i < handler.getSlots(); i++) {
                    if (!handler.getStackInSlot(i).isEmpty()) curStacks.add(handler.getStackInSlot(i));
                }
            }
        }

        @Override
        public String getTooltipPrefix(ItemStack stack) {
            return TextFormatting.GREEN.toString();
        }
    }
}
