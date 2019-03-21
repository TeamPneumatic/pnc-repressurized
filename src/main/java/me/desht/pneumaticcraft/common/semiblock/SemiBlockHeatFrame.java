package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.recipes.HeatFrameCoolingRecipe;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public class SemiBlockHeatFrame extends SemiBlockBasic<TileEntity> implements IHeatExchanger {
    public static final String ID = "heat_frame";

    private final IHeatExchangerLogic logic = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();
    private int lastValidSlot; // cache the current cooking slot for performance boost
    private int cookingProgress;
    private int coolingProgress;
    @DescSynced
    private int heatLevel = 10;

    public SemiBlockHeatFrame(Class<TileEntity> tileClass){
        super(tileClass);
    }

    public SemiBlockHeatFrame() {
        super(TileEntity.class);
    }

    @Override
    public boolean canPlace(EnumFacing facing) {
        return getTileEntity() != null && getTileEntity().hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    }

    public int getHeatLevel() {
        return heatLevel;
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            heatLevel = HeatUtil.getHeatLevelForTemperature(logic.getTemperature());
            if (logic.getTemperature() > 374) {
                if (cookingProgress < 100) {
                    int progress = Math.max(0, ((int) logic.getTemperature() - 343) / 30);
                    progress = Math.min(5, progress);
                    logic.addHeat(-progress);
                    cookingProgress += progress;
                }
                if (cookingProgress >= 100) {
                    IItemHandler handler = IOHelper.getInventoryForTE(getTileEntity());
                    if (handler != null) {
                        if (!tryCookSlot(handler, lastValidSlot)) {
                            for (int i = 0; i < handler.getSlots(); i++) {
                                if (tryCookSlot(handler, i)) {
                                    cookingProgress -= 100;
                                    break;
                                }
                            }
                        } else {
                            cookingProgress -= 100;
                        }
                    }
                }
            } else if (logic.getTemperature() < 273) {
                if (coolingProgress < 100) {
                    int progress = Math.max(0, ((int) logic.getTemperature() - 243) / 30);
                    progress = 6 - Math.min(5, progress);
                    logic.addHeat(progress);
                    coolingProgress += progress;
                }
                if (coolingProgress >= 100) {
                    IItemHandler handler = IOHelper.getInventoryForTE(getTileEntity());
                    if (handler != null) {
                        if (!tryCoolSlot(handler, lastValidSlot)) {
                            for (int i = 0; i < handler.getSlots(); i++) {
                                if (tryCoolSlot(handler, i)) {
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

    private boolean tryCookSlot(IItemHandler handler, int slot) {
        ItemStack stack = handler.getStackInSlot(slot);
        if (!stack.isEmpty()) {
            ItemStack result = FurnaceRecipes.instance().getSmeltingResult(stack);
            if (!result.isEmpty()) {
                ItemStack remainder = IOHelper.insert(getTileEntity(), result, true);
                if (remainder.isEmpty()) {
                    IOHelper.insert(getTileEntity(), result, false);
                    handler.extractItem(slot, 1, false);
                    lastValidSlot = slot;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean tryCoolSlot(IItemHandler handler, int slot) {
        ItemStack stack = handler.getStackInSlot(slot);
        if (!stack.isEmpty()) {
            for (HeatFrameCoolingRecipe recipe : HeatFrameCoolingRecipe.recipes) {
//                if (PneumaticRecipeRegistry.isItemEqual(recipe.input, stack)) {
                if (recipe.input.isItemEqual(stack)) {
//                    int amount = PneumaticRecipeRegistry.getItemAmount(recipe.input);
                    if (stack.getCount() >= recipe.input.getItemAmount()) {
                        ItemStack containerItem = stack.getItem().getContainerItem(stack);
                        boolean canStoreContainerItem = false;
                        boolean canStoreOutput = false;
                        for (int i = 0; i < handler.getSlots(); i++) {
                            ItemStack s = handler.getStackInSlot(i);
                            if (s.isEmpty()) {
                                if (canStoreOutput) {
                                    canStoreContainerItem = true;
                                } else {
                                    canStoreOutput = true;
                                }
                            } else {
                                if (s.isItemEqual(recipe.output) && ItemStack.areItemStackTagsEqual(s, recipe.output) && s.getMaxStackSize() >= s.getCount() + recipe.output.getCount()) {
                                    canStoreOutput = true;
                                }
                                if (!containerItem.isEmpty() && s.isItemEqual(containerItem) && ItemStack.areItemStackTagsEqual(s, containerItem) && s.getMaxStackSize() >= s.getCount() + containerItem.getCount()) {
                                    canStoreContainerItem = true;
                                }
                            }
                        }
                        if (canStoreOutput && (containerItem.isEmpty() || canStoreContainerItem)) {
                            handler.extractItem(slot, 1, false);
                            IOHelper.insert(getTileEntity(), recipe.output.copy(), false);
                            if (!containerItem.isEmpty()) {
                                IOHelper.insert(getTileEntity(), containerItem.copy(), false);
                            }
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        logic.writeToNBT(tag);
        tag.setInteger("cookingProgress", cookingProgress);
        tag.setInteger("coolingProgress", coolingProgress);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        logic.readFromNBT(tag);
        cookingProgress = tag.getInteger("cookingProgress");
        coolingProgress = tag.getInteger("coolingProgress");
    }

    @Override
    public void onPlaced(EntityPlayer player, ItemStack stack, EnumFacing facing) {
        super.onPlaced(player, stack, facing);
        getWorld().notifyNeighborsOfStateChange(getPos(), getBlockState().getBlock(), true);
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(EnumFacing side) {
        return logic;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        getWorld().notifyNeighborsOfStateChange(getPos(), getBlockState().getBlock(), true);
    }

    @Override
    public void addWailaInfoToTag(NBTTagCompound tag) {
        super.addWailaInfoToTag(tag);
        tag.setInteger("temp", (int) logic.getTemperature());
    }

    @Override
    public void addTooltip(List<String> curInfo, NBTTagCompound tag, boolean extended) {
        super.addTooltip(curInfo, tag, extended);
        // WAILA sync's the temperature via NBT, TOP runs serverside and gets it here
        int temp = tag != null && tag.hasKey("temp") ? tag.getInteger("temp") : (int) logic.getTemperature();
        curInfo.add(HeatUtil.formatHeatString(temp));
    }
}
