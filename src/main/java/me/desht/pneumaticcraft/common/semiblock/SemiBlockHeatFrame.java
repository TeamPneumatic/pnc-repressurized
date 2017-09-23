package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.recipes.PneumaticRecipeRegistry;
import me.desht.pneumaticcraft.common.tileentity.TileEntityCompressedIronBlock;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class SemiBlockHeatFrame extends SemiBlockBasic implements IHeatExchanger {
    public static final String ID = "heat_frame";

    private final IHeatExchangerLogic logic = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();
    private int lastValidSlot;//Performance increaser
    private int cookingProgress;
    private int coolingProgress;
    @DescSynced
    private int heatLevel = 10;

    @Override
    public boolean canPlace() {
        return getTileEntity() != null && getTileEntity().hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    }

    public int getHeatLevel() {
        return heatLevel;
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote) {
            heatLevel = TileEntityCompressedIronBlock.getHeatLevelForTemperature(logic.getTemperature());
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
            for (Pair<Object, ItemStack> recipe : PneumaticRecipeRegistry.getInstance().heatFrameCoolingRecipes) {
                if (PneumaticRecipeRegistry.isItemEqual(recipe.getKey(), stack)) {
                    int amount = PneumaticRecipeRegistry.getItemAmount(recipe.getKey());
                    if (stack.getCount() >= amount) {
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
                                if (s.isItemEqual(recipe.getRight()) && ItemStack.areItemStackTagsEqual(s, recipe.getRight()) && s.getMaxStackSize() >= s.getCount() + recipe.getRight().getCount()) {
                                    canStoreOutput = true;
                                }
                                if (!containerItem.isEmpty() && s.isItemEqual(containerItem) && ItemStack.areItemStackTagsEqual(s, containerItem) && s.getMaxStackSize() >= s.getCount() + containerItem.getCount()) {
                                    canStoreContainerItem = true;
                                }
                            }
                        }
                        if (canStoreOutput && (containerItem.isEmpty() || canStoreContainerItem)) {
                            handler.extractItem(slot, 1, false);
                            IOHelper.insert(getTileEntity(), recipe.getValue().copy(), false);
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
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        logic.readFromNBT(tag);
    }

    @Override
    public void onPlaced(EntityPlayer player, ItemStack stack) {
        super.onPlaced(player, stack);
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
    public void addWailaTooltip(List<String> curInfo, NBTTagCompound tag) {
        super.addWailaTooltip(curInfo, tag);
        curInfo.add(I18n.format("waila.temperature", tag.getInteger("temp") - 273));
    }
}
