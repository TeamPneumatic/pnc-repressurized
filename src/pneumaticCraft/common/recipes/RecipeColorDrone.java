package pneumaticCraft.common.recipes;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import pneumaticCraft.common.item.Itemss;
import pneumaticCraft.common.tileentity.TileEntityPlasticMixer;

public class RecipeColorDrone implements IRecipe{

    @Override
    public boolean matches(InventoryCrafting inventoryCrafting, World world){
        boolean hasDrone = false, hasDye = false;
        for(int i = 0; i < inventoryCrafting.getSizeInventory(); i++) {
            ItemStack stack = inventoryCrafting.getStackInSlot(i);
            if(stack != null) {
                if(stack.getItem() == Itemss.drone) {
                    if(!hasDrone) hasDrone = true;
                    else return false;
                } else if(TileEntityPlasticMixer.getDyeIndex(stack) >= 0) {
                    if(!hasDye) hasDye = true;
                    else return false;
                }
            }
        }
        return hasDrone && hasDye;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventoryCrafting){
        ItemStack drone = null;
        int dyeIndex = -1;
        for(int i = 0; i < inventoryCrafting.getSizeInventory(); i++) {
            ItemStack stack = inventoryCrafting.getStackInSlot(i);
            if(stack != null) {
                if(stack.getItem() == Itemss.drone) {
                    drone = stack.copy();
                } else if(dyeIndex == -1) {
                    dyeIndex = TileEntityPlasticMixer.getDyeIndex(stack);
                }
            }
        }
        NBTTagCompound droneTag = drone.getTagCompound();
        if(droneTag == null) {
            droneTag = new NBTTagCompound();
            drone.setTagCompound(droneTag);
        }
        droneTag.setInteger("color", ItemDye.field_150922_c[dyeIndex]);
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
