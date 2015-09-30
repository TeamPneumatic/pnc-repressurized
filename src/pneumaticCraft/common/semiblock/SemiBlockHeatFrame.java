package pneumaticCraft.common.semiblock;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.Pair;

import pneumaticCraft.api.IHeatExchangerLogic;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.api.tileentity.IHeatExchanger;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.recipes.PneumaticRecipeRegistry;
import pneumaticCraft.common.tileentity.TileEntityCompressedIronBlock;
import pneumaticCraft.common.util.IOHelper;

public class SemiBlockHeatFrame extends SemiBlockBasic implements IHeatExchanger{
    private final IHeatExchangerLogic logic = PneumaticRegistry.getInstance().getHeatExchangerLogic();
    private int lastValidSlot;//Performance increaser
    private int cookingProgress;
    private int coolingProgress;
    @DescSynced
    private int heatLevel = 10;

    @Override
    public boolean canPlace(){
        return getTileEntity() instanceof IInventory;
    }

    public int getHeatLevel(){
        return heatLevel;
    }

    @Override
    public void update(){
        super.update();
        if(!getWorld().isRemote) {
            heatLevel = TileEntityCompressedIronBlock.getHeatLevelForTemperature(logic.getTemperature());
            if(logic.getTemperature() > 374) {
                if(cookingProgress < 100) {
                    int progress = Math.max(0, ((int)logic.getTemperature() - 343) / 30);
                    progress = Math.min(5, progress);
                    logic.addHeat(-progress * 1);
                    cookingProgress += progress;
                }
                if(cookingProgress >= 100) {
                    IInventory inv = IOHelper.getInventoryForTE(getTileEntity());
                    if(inv != null) {
                        if(!tryCookSlot(inv, lastValidSlot)) {
                            for(int i = 0; i < inv.getSizeInventory(); i++) {
                                if(tryCookSlot(inv, i)) {
                                    cookingProgress -= 100;
                                    break;
                                }
                            }
                        } else {
                            cookingProgress -= 100;
                        }
                    }
                }
            } else if(logic.getTemperature() < 273) {
                if(coolingProgress < 100) {
                    int progress = Math.max(0, ((int)logic.getTemperature() - 243) / 30);
                    progress = 6 - Math.min(5, progress);
                    logic.addHeat(progress * 1);
                    coolingProgress += progress;
                }
                if(coolingProgress >= 100) {
                    IInventory inv = IOHelper.getInventoryForTE(getTileEntity());
                    if(inv != null) {
                        if(!tryCoolSlot(inv, lastValidSlot)) {
                            for(int i = 0; i < inv.getSizeInventory(); i++) {
                                if(tryCoolSlot(inv, i)) {
                                    coolingProgress -= 100;
                                    break;
                                }
                            }
                        } else {
                            coolingProgress -= 100;
                        }
                    }
                }
            }
        }
    }

    private boolean tryCookSlot(IInventory inv, int slot){
        ItemStack stack = inv.getStackInSlot(slot);
        if(stack != null) {
            ItemStack result = FurnaceRecipes.smelting().getSmeltingResult(stack);
            if(result != null) {
                ItemStack remainder = IOHelper.insert(getTileEntity(), result, true);
                if(remainder == null) {
                    IOHelper.insert(getTileEntity(), result, false);
                    inv.decrStackSize(slot, 1);
                    lastValidSlot = slot;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean tryCoolSlot(IInventory inv, int slot){
        ItemStack stack = inv.getStackInSlot(slot);
        if(stack != null) {
            for(Pair<Object, ItemStack> recipe : PneumaticRecipeRegistry.getInstance().heatFrameCoolingRecipes) {
                if(PneumaticRecipeRegistry.isItemEqual(recipe.getKey(), stack)) {
                    int amount = PneumaticRecipeRegistry.getItemAmount(recipe.getKey());
                    if(stack.stackSize >= amount) {
                        ItemStack containerItem = stack.getItem().getContainerItem(stack);
                        boolean canStoreContainerItem = false;
                        boolean canStoreOutput = false;
                        for(int i = 0; i < inv.getSizeInventory(); i++) {
                            ItemStack s = inv.getStackInSlot(i);
                            if(s == null) {
                                if(canStoreOutput) {
                                    canStoreContainerItem = true;
                                } else {
                                    canStoreOutput = true;
                                }
                            } else {
                                if(s.isItemEqual(recipe.getRight()) && ItemStack.areItemStackTagsEqual(s, recipe.getRight()) && s.getMaxStackSize() >= s.stackSize + recipe.getRight().stackSize) {
                                    canStoreOutput = true;
                                }
                                if(containerItem != null && s.isItemEqual(containerItem) && ItemStack.areItemStackTagsEqual(s, containerItem) && s.getMaxStackSize() >= s.stackSize + containerItem.stackSize) {
                                    canStoreContainerItem = true;
                                }
                            }
                        }
                        if(canStoreOutput && (containerItem == null || canStoreContainerItem)) {
                            inv.decrStackSize(slot, amount);
                            IOHelper.insert(getTileEntity(), recipe.getValue().copy(), false);
                            if(containerItem != null) IOHelper.insert(getTileEntity(), containerItem.copy(), false);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        logic.writeToNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        logic.readFromNBT(tag);
    }

    @Override
    public void onPlaced(EntityPlayer player, ItemStack stack){
        super.onPlaced(player, stack);
        getWorld().notifyBlocksOfNeighborChange(getX(), getY(), getZ(), getBlock());
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(ForgeDirection side){
        return logic;
    }

    @Override
    public void invalidate(){
        super.invalidate();
        getWorld().notifyBlocksOfNeighborChange(getX(), getY(), getZ(), getBlock());
    }

    @Override
    public void addWailaInfoToTag(NBTTagCompound tag){
        super.addWailaInfoToTag(tag);
        tag.setInteger("temp", (int)logic.getTemperature());
    }

    @Override
    public void addWailaTooltip(List<String> curInfo, NBTTagCompound tag){
        super.addWailaTooltip(curInfo, tag);
        curInfo.add(StatCollector.translateToLocalFormatted("waila.temperature", tag.getInteger("temp") - 273));
    }
}
