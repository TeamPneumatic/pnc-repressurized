package me.desht.pneumaticcraft.common.semiblock;

import me.desht.pneumaticcraft.api.PneumaticRegistry;
import me.desht.pneumaticcraft.api.crafting.PneumaticCraftRecipes;
import me.desht.pneumaticcraft.api.crafting.recipe.IHeatFrameCoolingRecipe;
import me.desht.pneumaticcraft.api.heat.IHeatExchangerLogic;
import me.desht.pneumaticcraft.api.tileentity.IHeatExchanger;
import me.desht.pneumaticcraft.common.heat.HeatUtil;
import me.desht.pneumaticcraft.common.network.DescSynced;
import me.desht.pneumaticcraft.common.util.IOHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static me.desht.pneumaticcraft.common.util.PneumaticCraftUtils.RL;

public class SemiBlockHeatFrame extends SemiBlockBasic<TileEntity> implements IHeatExchanger {
    public static final ResourceLocation ID = RL("heat_frame");

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
        if (stack.isEmpty()) return false;

        IHeatFrameCoolingRecipe recipe = PneumaticCraftRecipes.heatFrameCoolingRecipes.values().stream()
                .filter(r -> r.matches(stack))
                .findFirst()
                .orElse(null);

        if (recipe != null) {
            ItemStack output = recipe.getOutput();
            if (stack.getCount() >= recipe.getInputAmount()) {
                ItemStack containerItem = stack.getItem().getContainerItem(stack);
                Pair<Integer,Integer> slots = findOutputSpace(handler, output, containerItem);
                if (slots.getLeft() >= 0 && (slots.getRight() >= 0 || containerItem.isEmpty())) {
                    handler.extractItem(slot, recipe.getInputAmount(), false);
                    handler.insertItem(slots.getLeft(), output, false);
                    if (!containerItem.isEmpty()) {
                        handler.insertItem(slots.getRight(), containerItem, false);
                    }
                    lastValidSlot = slot;
                    return true;
                }
            }
        }
        return false;
    }

    Pair<Integer,Integer> findOutputSpace(IItemHandler handler, ItemStack output, ItemStack containerItem) {
        int outSlot = -1;
        int containerSlot = -1;

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack s = handler.getStackInSlot(i);
            if (s.isEmpty()) {
                if (outSlot >= 0) {
                    containerSlot = i;
                } else {
                    outSlot = i;
                }
            } else {
                if (ItemHandlerHelper.canItemStacksStack(s, output) && s.getCount() + output.getCount() < s.getMaxStackSize()) {
                    outSlot = i;
                }
                if (!containerItem.isEmpty()
                        && ItemHandlerHelper.canItemStacksStack(s, containerItem)
                        && s.getCount() + containerItem.getCount() < s.getMaxStackSize()) {
                    containerSlot = i;
                }
            }
        }

        return Pair.of(outSlot, containerSlot);
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void writeToNBT(CompoundNBT tag) {
        super.writeToNBT(tag);
        tag.put("heatExchanger", logic.serializeNBT());
        tag.putInt("cookingProgress", cookingProgress);
        tag.putInt("coolingProgress", coolingProgress);
    }

    @Override
    public void readFromNBT(CompoundNBT tag) {
        super.readFromNBT(tag);
        logic.deserializeNBT(tag.getCompound("heatExchanger"));
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
