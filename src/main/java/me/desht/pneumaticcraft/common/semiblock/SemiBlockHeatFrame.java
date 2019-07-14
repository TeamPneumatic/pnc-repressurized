package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.recipes.HeatFrameCoolingRecipe;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
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
    public boolean canPlace(Direction facing) {
        return getTileEntity() != null && getTileEntity().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent();
    }

    public int getHeatLevel() {
        return heatLevel;
    }

    @Override
    public void tick() {
        super.tick();
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
                    IOHelper.getInventoryForTE(getTileEntity()).ifPresent(handler -> {
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
                    });
                }
            } else if (logic.getTemperature() < 273) {
                if (coolingProgress < 100) {
                    int progress = Math.max(0, ((int) logic.getTemperature() - 243) / 30);
                    progress = 6 - Math.min(5, progress);
                    logic.addHeat(progress);
                    coolingProgress += progress;
                }
                if (coolingProgress >= 100) {
                    IOHelper.getInventoryForTE(getTileEntity()).ifPresent(handler -> {
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
                    });
                }
            }
        }
    }

    private boolean tryCookSlot(IItemHandler handler, int slot) {
        ItemStack stack = handler.getStackInSlot(slot);
        if (!stack.isEmpty()) {
            Inventory inv = new Inventory(1);
            inv.setInventorySlotContents(0, stack);
            return world.getRecipeManager().getRecipe(IRecipeType.SMELTING, inv, this.world).map(recipe -> {
                ItemStack result = recipe.getRecipeOutput();
                if (!result.isEmpty()) {
                    ItemStack remainder = IOHelper.insert(getTileEntity(), result, true);
                    if (remainder.isEmpty()) {
                        IOHelper.insert(getTileEntity(), result, false);
                        handler.extractItem(slot, 1, false);
                        lastValidSlot = slot;
                        return true;
                    }
                }
                return false;
            }).orElse(false);
        }
        return false;
    }

    private boolean tryCoolSlot(IItemHandler handler, int slot) {
        ItemStack stack = handler.getStackInSlot(slot);
        if (!stack.isEmpty()) {
            for (HeatFrameCoolingRecipe recipe : HeatFrameCoolingRecipe.recipes) {
                if (recipe.input.isItemEqual(stack)) {
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
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        logic.writeToNBT(tag);
        tag.putInt("cookingProgress", cookingProgress);
        tag.putInt("coolingProgress", coolingProgress);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        logic.readFromNBT(tag);
        cookingProgress = tag.getInt("cookingProgress");
        coolingProgress = tag.getInt("coolingProgress");
    }

    @Override
    public void onPlaced(PlayerEntity player, ItemStack stack, Direction facing) {
        super.onPlaced(player, stack, facing);
        getWorld().notifyNeighborsOfStateChange(getPos(), getBlockState().getBlock());
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(Direction side) {
        return logic;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        getWorld().notifyNeighborsOfStateChange(getPos(), getBlockState().getBlock());
    }

    @Override
    public void addWailaInfoToTag(CompoundNBT tag) {
        super.addWailaInfoToTag(tag);
        tag.putInt("temp", (int) logic.getTemperature());
    }

    @Override
    public void addTooltip(List<ITextComponent> curInfo, CompoundNBT tag, boolean extended) {
        super.addTooltip(curInfo, tag, extended);
        // WAILA sync's the temperature via NBT, TOP runs serverside and gets it here
        int temp = tag != null && tag.contains("temp") ? tag.getInt("temp") : (int) logic.getTemperature();
        curInfo.add(HeatUtil.formatHeatString(temp));
    }
}
