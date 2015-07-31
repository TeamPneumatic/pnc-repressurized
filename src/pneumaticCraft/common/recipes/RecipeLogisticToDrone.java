package pneumaticCraft.common.recipes;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import pneumaticCraft.common.item.Itemss;

public class RecipeLogisticToDrone implements IRecipe{

    @Override
    public boolean matches(InventoryCrafting inventoryCrafting, World world){
        boolean hasDrone = false, hasPCB = false;
        for(int i = 0; i < inventoryCrafting.getSizeInventory(); i++) {
            ItemStack stack = inventoryCrafting.getStackInSlot(i);
            if(stack != null) {
                if(stack.getItem() == Itemss.logisticsDrone) {
                    if(!hasDrone) hasDrone = true;
                    else return false;
                } else if(stack.getItem() == Itemss.printedCircuitBoard) {
                    if(!hasPCB) hasPCB = true;
                    else return false;
                }
            }
        }
        return hasDrone && hasPCB;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting){
        ItemStack logisticDrone = null;
        for(int i = 0; i < inventoryCrafting.getSizeInventory(); i++) {
            ItemStack stack = inventoryCrafting.getStackInSlot(i);
            if(stack != null && stack.getItem() == Itemss.logisticsDrone) {
                logisticDrone = stack.copy();
                break;
            }
        }
        ItemStack drone = new ItemStack(Itemss.drone);
        NBTTagCompound droneTag = logisticDrone.getTagCompound();
        if(droneTag == null) {
            droneTag = new NBTTagCompound();
            logisticDrone.setTagCompound(droneTag);
        }
        drone.setTagCompound(droneTag);
        return drone;
    }

    @Override
    public int getRecipeSize(){
        return 2;
    }

    @Override
    public ItemStack getRecipeOutput(){
        return new ItemStack(Itemss.drone);
    }

}
