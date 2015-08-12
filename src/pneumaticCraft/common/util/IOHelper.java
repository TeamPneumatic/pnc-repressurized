package pneumaticCraft.common.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/*
 * This file is part of Blue Power.
 *
 *     Blue Power is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Blue Power is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Blue Power.  If not, see <http://www.gnu.org/licenses/>
 */

/**
 * @author MineMaarten
 * @author Dynious
 */
public class IOHelper{

    public static IInventory getInventoryForTE(TileEntity te){

        if(te instanceof IInventory) {
            IInventory inv = (IInventory)te;
            Block block = te.getBlockType();
            if(block instanceof BlockChest) {
                inv = ((BlockChest)block).func_149951_m(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord);
            }
            return inv;
        } else {
            return null;
        }
    }

    public static TileEntity getNeighbor(TileEntity te, ForgeDirection dir){
        return te.getWorldObj().getTileEntity(te.xCoord + dir.offsetX, te.yCoord + dir.offsetY, te.zCoord + dir.offsetZ);
    }

    /**
     * Extracts an exact amount on all sides
     * @param tile
     * @param itemStack
     * @param simulate
     * @return
     */
    public static ItemStack extract(TileEntity tile, ItemStack itemStack, boolean simulate){
        ItemStack extracted = null;
        for(ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
            extracted = extract(tile, d, itemStack, true, simulate);
            if(extracted != null) return extracted;
        }
        return null;
    }

    public static ItemStack extract(TileEntity inventory, ForgeDirection direction, boolean simulate){

        IInventory inv = getInventoryForTE(inventory);
        if(inv != null) return extract(inv, direction, simulate);
        return null;
    }

    public static ItemStack extract(IInventory inventory, ForgeDirection direction, boolean simulate){

        if(inventory instanceof ISidedInventory) {
            ISidedInventory isidedinventory = (ISidedInventory)inventory;
            int[] accessibleSlotsFromSide = isidedinventory.getAccessibleSlotsFromSide(direction.ordinal());

            for(int anAccessibleSlotsFromSide : accessibleSlotsFromSide) {
                ItemStack stack = extract(inventory, direction, anAccessibleSlotsFromSide, simulate);
                if(stack != null) return stack;
            }
        } else {
            int j = inventory.getSizeInventory();

            for(int k = 0; k < j; ++k) {
                ItemStack stack = extract(inventory, direction, k, simulate);
                if(stack != null) return stack;
            }
        }
        return null;
    }

    public static ItemStack extract(IInventory inventory, ForgeDirection direction, int slot, boolean simulate){

        ItemStack itemstack = inventory.getStackInSlot(slot);

        if(itemstack != null && canExtractItemFromInventory(inventory, itemstack, slot, direction.ordinal())) {
            if(!simulate) inventory.setInventorySlotContents(slot, null);
            return itemstack;
        }
        return null;
    }

    public static ItemStack extract(TileEntity tile, ForgeDirection direction, ItemStack requestedStack, boolean useItemCount, boolean simulate){

        return extract(tile, direction, requestedStack, useItemCount, simulate, 0);
    }

    public static int[] getAccessibleSlotsForInventory(IInventory inv, ForgeDirection side){

        int[] accessibleSlots;
        if(inv != null) {
            if(inv instanceof ISidedInventory) {
                accessibleSlots = ((ISidedInventory)inv).getAccessibleSlotsFromSide(side.ordinal());
            } else {
                accessibleSlots = new int[inv.getSizeInventory()];
                for(int i = 0; i < accessibleSlots.length; i++)
                    accessibleSlots[i] = i;
            }
            return accessibleSlots;
        } else {
            return new int[0];
        }
    }

    /**
     * Retrieves an item from the specified inventory. This item can be specified.
     * 
     * @param tile
     * @param direction
     * @param requestedStack
     * @param useItemCount
     *            if true, it'll only retrieve the stack of the exact item count given. it'll look in multiple slots of the inventory. if false, the
     *            first matching stack, ignoring item count, will be returned.
     * @param simulate
     * @param fuzzySetting
     *            ,
     * @return
     */
    public static ItemStack extract(TileEntity tile, ForgeDirection direction, ItemStack requestedStack, boolean useItemCount, boolean simulate, int fuzzySetting){

        if(requestedStack == null) return requestedStack;
        IInventory inv = getInventoryForTE(tile);
        if(inv != null) {
            int[] accessibleSlots;
            if(inv instanceof ISidedInventory) {
                accessibleSlots = ((ISidedInventory)inv).getAccessibleSlotsFromSide(direction.ordinal());
            } else {
                accessibleSlots = new int[inv.getSizeInventory()];
                for(int i = 0; i < accessibleSlots.length; i++)
                    accessibleSlots[i] = i;
            }
            int itemsFound = 0;
            for(int slot : accessibleSlots) {
                ItemStack stack = inv.getStackInSlot(slot);
                if(stack != null && stack.stackSize > 0 && stack.isItemEqual(requestedStack) && IOHelper.canExtractItemFromInventory(inv, stack, slot, direction.ordinal())) {
                    if(!useItemCount) {
                        if(!simulate) {
                            inv.setInventorySlotContents(slot, null);
                        }
                        return stack;
                    }
                    itemsFound += stack.stackSize;
                }
            }
            if(itemsFound >= requestedStack.stackSize) {
                ItemStack exportedStack = null;
                int itemsNeeded = requestedStack.stackSize;
                for(int slot : accessibleSlots) {
                    ItemStack stack = inv.getStackInSlot(slot);
                    if(stack != null && stack.isItemEqual(requestedStack) && IOHelper.canExtractItemFromInventory(inv, stack, slot, direction.ordinal())) {
                        int itemsSubstracted = Math.min(itemsNeeded, stack.stackSize);
                        if(itemsSubstracted > 0) exportedStack = stack;
                        itemsNeeded -= itemsSubstracted;
                        if(!simulate) {
                            stack.stackSize -= itemsSubstracted;
                            if(stack.stackSize == 0) {
                                inv.setInventorySlotContents(slot, null);
                            }
                            tile.markDirty();
                        }
                    }
                }
                exportedStack = exportedStack.copy();
                exportedStack.stackSize = requestedStack.stackSize;
                return exportedStack;
            }
        }
        return null;

    }

    public static ItemStack extractOneItem(TileEntity tile, ForgeDirection dir, boolean simulate){

        IInventory inv = getInventoryForTE(tile);
        if(inv != null) {
            int[] accessibleSlots;
            if(inv instanceof ISidedInventory) {
                accessibleSlots = ((ISidedInventory)inv).getAccessibleSlotsFromSide(dir.ordinal());
            } else {
                accessibleSlots = new int[inv.getSizeInventory()];
                for(int i = 0; i < accessibleSlots.length; i++)
                    accessibleSlots[i] = i;
            }
            for(int slot : accessibleSlots) {
                ItemStack stack = inv.getStackInSlot(slot);
                if(stack != null && stack.stackSize > 0 && IOHelper.canExtractItemFromInventory(inv, stack, slot, dir.ordinal())) {
                    if(simulate) {
                        ItemStack ret = stack.copy();
                        ret.stackSize = 1;
                        return ret;
                    }
                    ItemStack ret = stack.splitStack(1);
                    if(stack.stackSize == 0) inv.setInventorySlotContents(slot, null);
                    tile.markDirty();
                    return ret;
                }
            }
        }
        return null;
    }

    /**
     * Inserts on all sides
     * @param tile
     * @param itemStack
     * @param simulate
     * @return
     */
    public static ItemStack insert(TileEntity tile, ItemStack itemStack, boolean simulate){
        IInventory inv = getInventoryForTE(tile);
        ItemStack insertingStack = itemStack.copy();
        for(int i = 0; i < 6; i++) {
            insertingStack = insert(inv, insertingStack, i, simulate);
            if(insertingStack == null || insertingStack.stackSize != itemStack.stackSize) return insertingStack;
        }
        return insertingStack;
    }

    public static ItemStack insert(TileEntity tile, ItemStack itemStack, ForgeDirection direction, boolean simulate){

        IInventory inv = getInventoryForTE(tile);
        if(inv != null) return insert(inv, itemStack, direction.ordinal(), simulate);
        return itemStack;
    }

    public static ItemStack insert(IInventory inventory, ItemStack itemStack, int side, boolean simulate){

        if(inventory instanceof ISidedInventory && side > -1) {
            ISidedInventory isidedinventory = (ISidedInventory)inventory;
            int[] aint = isidedinventory.getAccessibleSlotsFromSide(side);

            for(int j = 0; j < aint.length && itemStack != null && itemStack.stackSize > 0; ++j) {
                itemStack = insert(inventory, itemStack, aint[j], side, simulate);
            }
        } else if(inventory != null) {
            int k = inventory.getSizeInventory();

            for(int l = 0; l < k && itemStack != null && itemStack.stackSize > 0; ++l) {
                itemStack = insert(inventory, itemStack, l, side, simulate);
            }
        }

        if(itemStack != null && itemStack.stackSize == 0) {
            itemStack = null;
        }

        return itemStack;
    }

    public static ItemStack insert(IInventory inventory, ItemStack itemStack, int slot, int side, boolean simulate){

        ItemStack itemstack1 = inventory.getStackInSlot(slot);

        if(canInsertItemToInventory(inventory, itemStack, slot, side)) {
            boolean flag = false;

            if(itemstack1 == null) {
                int max = Math.min(itemStack.getMaxStackSize(), inventory.getInventoryStackLimit());
                if(max >= itemStack.stackSize) {
                    if(!simulate) {
                        inventory.setInventorySlotContents(slot, itemStack);
                        flag = true;
                    }
                    itemStack = null;
                } else {
                    if(!simulate) {
                        inventory.setInventorySlotContents(slot, itemStack.splitStack(max));
                        flag = true;
                    } else {
                        itemStack.splitStack(max);
                    }
                }
            } else if(itemstack1.isItemEqual(itemStack) && ItemStack.areItemStackTagsEqual(itemstack1, itemStack)) {
                int max = Math.min(itemStack.getMaxStackSize(), inventory.getInventoryStackLimit());
                if(max > itemstack1.stackSize) {
                    int l = Math.min(itemStack.stackSize, max - itemstack1.stackSize);
                    itemStack.stackSize -= l;
                    if(!simulate) {
                        itemstack1.stackSize += l;
                        flag = l > 0;
                    }
                }
            }
            if(flag) {
                inventory.markDirty();
            }
        }

        return itemStack;
    }

    public static boolean canInsertItemToInventory(IInventory inventory, ItemStack itemStack, int slot, boolean[] sides){
        for(int i = 0; i < sides.length; i++) {
            if(sides[i] && canInsertItemToInventory(inventory, itemStack, slot, i)) return true;
        }
        return false;
    }

    public static boolean canExtractItemFromInventory(IInventory inventory, ItemStack itemStack, int slot, boolean[] sides){
        for(int i = 0; i < sides.length; i++) {
            if(sides[i] && canExtractItemFromInventory(inventory, itemStack, slot, i)) return true;
        }
        return false;
    }

    public static boolean canInsertItemToInventory(IInventory inventory, ItemStack itemStack, int slot, int side){

        return inventory.isItemValidForSlot(slot, itemStack) && (!(inventory instanceof ISidedInventory) || ((ISidedInventory)inventory).canInsertItem(slot, itemStack, side));
    }

    public static boolean canExtractItemFromInventory(IInventory inventory, ItemStack itemStack, int slot, int side){

        return !(inventory instanceof ISidedInventory) || ((ISidedInventory)inventory).canExtractItem(slot, itemStack, side);
    }

    public static void dropInventory(World world, int x, int y, int z){

        TileEntity tileEntity = world.getTileEntity(x, y, z);

        if(!(tileEntity instanceof IInventory)) {
            return;
        }

        IInventory inventory = (IInventory)tileEntity;

        for(int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack itemStack = inventory.getStackInSlot(i);

            if(itemStack != null && itemStack.stackSize > 0) {
                spawnItemInWorld(world, itemStack, x, y, z);
            }
        }
    }

    public static void spawnItemInWorld(World world, ItemStack itemStack, double x, double y, double z){

        if(world.isRemote) return;
        float dX = world.rand.nextFloat() * 0.8F + 0.1F;
        float dY = world.rand.nextFloat() * 0.8F + 0.1F;
        float dZ = world.rand.nextFloat() * 0.8F + 0.1F;

        EntityItem entityItem = new EntityItem(world, x + dX, y + dY, z + dZ, new ItemStack(itemStack.getItem(), itemStack.stackSize, itemStack.getItemDamage()));

        if(itemStack.hasTagCompound()) {
            entityItem.getEntityItem().setTagCompound((NBTTagCompound)itemStack.getTagCompound().copy());
        }

        float factor = 0.05F;
        entityItem.motionX = world.rand.nextGaussian() * factor;
        entityItem.motionY = world.rand.nextGaussian() * factor + 0.2F;
        entityItem.motionZ = world.rand.nextGaussian() * factor;
        world.spawnEntityInWorld(entityItem);
        itemStack.stackSize = 0;
    }

    public static boolean canInterfaceWith(TileEntity tile, ForgeDirection direction){
        if(tile instanceof IInventory) {
            return !(tile instanceof ISidedInventory) || ((ISidedInventory)tile).getAccessibleSlotsFromSide(direction.ordinal()).length > 0;
        }
        return false;
    }
}
